package lab6.server.system.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lab6.shared.ticket.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Scanner;
import java.util.TreeSet;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.Getter;

public class CSVHandler implements AutoCloseable{
    private static final TreeSet<Ticket> EMPTY_TREE_SET_TICKET = new TreeSet<>();

    private Path filePath;
    private final FileHandler fileHandler;

    public CSVHandler(Path filePath) throws IOException {
        this.fileHandler = new FileHandler(filePath);
        this.filePath = filePath;
    }

    public CSVHandler(Path filePath, boolean mode) throws IOException {
        this.fileHandler = new FileHandler(filePath, mode);
        this.filePath = filePath;
    }

    public void close() throws IOException {
        fileHandler.close();
    }

    public TreeSet<Ticket> read() {
        if (!Files.exists(filePath)) {
            System.err.println("[Error] File: " + filePath.getFileName() + " does not exist");
            return EMPTY_TREE_SET_TICKET;
        }
        if (!Files.isRegularFile(filePath)) {
            System.err.println("[Error] File: " + filePath.getFileName() + " not a regular file");
            return EMPTY_TREE_SET_TICKET;
        }
        if (!Files.isReadable(filePath)) {
            System.err.println("[Error] File: " + filePath.getFileName() + " cannot be read");
            return EMPTY_TREE_SET_TICKET;
        }

        CsvMapper csvMapper = new CsvMapper();
        csvMapper.registerModule(new JavaTimeModule());
        csvMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        TreeSet<Ticket> tickets = new TreeSet<>();

        try (Scanner scanner = fileHandler.getScanner()) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line == null)
                    continue;

                String[] parts = line.split(",");
                if (parts.length > csvMapper.schemaFor(Ticket.class).size()) {
                    
                    StringBuilder newLine = new StringBuilder();

                    for (int i = 0; i < csvMapper.schemaFor(Ticket.class).size(); ++i) {
                        newLine.append(parts[i]);
                        if (i < csvMapper.schemaFor(Ticket.class).size() - 1)
                            newLine.append(",");
                    }

                    line = newLine.toString();
                }

                try {
                    

                    try {
                        Ticket ticket = csvMapper.readerFor(Ticket.class)
                                            .with(csvMapper.schemaFor(Ticket.class))
                                            .readValue(line);
                        ticket.validate();
                        tickets.add(ticket);
                    } catch (IllegalArgumentException e) {
                        //IDGenerator.getInstance().deleteLastID();
                        System.err.println("Ticket has invalid data" + e.getMessage());
                    }
                    
                } catch (IllegalArgumentException e) {
                    System.err.println("[Error] Invalid data: " + e.getMessage());
                } catch (JsonProcessingException e) {
                    System.err.println("[Error] Error when working with csv" + e.getMessage());
                }
            }

            return tickets;
        } catch (Exception e) {
            System.err.println("[Error] Something went wrong " + e.getMessage());
            return new TreeSet<>();
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonPropertyOrder({"name", "coordinates", "price", "refundable", "type", "person", "id"})
    @Data
    private class TicketDTO {
        @NonNull
        @JsonProperty("name")
        private String name; // Поле не может быть null, Строка не может быть пустой
        
        @JsonUnwrapped
        @NonNull
        @JsonProperty("coordinates")
        private Coordinates coordinates; // Поле не может быть null
        
        @Getter
        @JsonProperty("price")
        private String price; // Значение поля должно быть больше 0
        
        @Getter
        @JsonProperty("refundable")
        private String refundable;
        
        @NonNull
        @JsonProperty("type")
        private TicketType type; // Поле не может быть null
        
        @JsonUnwrapped
        @NonNull
        @JsonProperty("person")
        private Person person; // Поле не может быть null

        @Setter(AccessLevel.NONE)
        @JsonProperty("id")
        private long id; // Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
        
        @JsonIgnore
        private boolean installedPrice = false;
        @JsonIgnore
        private boolean installedRefundable = false;

        public TicketDTO(Ticket ticket) {
            this.name = ticket.getName();
            this.coordinates = ticket.getCoordinates();
            this.price = (ticket.getInstalledPrice() ? String.valueOf(ticket.getPrice()) : "");
            this.refundable = (ticket.getInstalledRefundable() ? String.valueOf(ticket.getRefundable()) : "");

            this.type = ticket.getType();
            this.person = ticket.getPerson();
            this.id = ticket.getId();
        }
    }

    public void write(TreeSet<Ticket> tickets) {
        if (!Files.isWritable(filePath)) {
            System.err.println("File " + filePath.getFileName() + "is not writable");
            return;
        }

        CsvMapper cvsMapper = new CsvMapper();
        cvsMapper.registerModule(new JavaTimeModule());
        cvsMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            SequenceWriter seq = cvsMapper.writerWithSchemaFor(TicketDTO.class).writeValues(fileHandler.getOutputStreamWriter());
            for (Ticket ticket : tickets) {
                TicketDTO ticketDTO = new TicketDTO(ticket);
                seq.write(ticketDTO);
            }

            seq.close();

        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + filePath.getFileName());
            System.err.println(e.getMessage());
        }
    }
}
