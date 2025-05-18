package lab6.server;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import lab6.shared.io.connection.Mark;
import lab6.shared.io.connection.Response;
import lab6.shared.io.console.StandartConsole;
import lab6.shared.io.connection.Request;

public class SharedConsoleServer extends StandartConsole {
    private final NetworkServer network;
    private static final Logger logger = Logger.getLogger(SharedConsoleServer.class.getName());
    private InetSocketAddress clientAddress;
    //private BlockingQueue<ServerRequest> requestQueue;

    public SharedConsoleServer(NetworkServer network) {
        this.network = network;
    }

    public void setClientAddress(InetSocketAddress address) {
        clientAddress = address;
        logger.info("Set client address: " + address);
    }

    public void offerRequest(ServerRequest request) {
        if (request == null || request.getRequest() == null) 
            return;

        if (request.getRequest().getMark() == null || 
                    (request.getRequest().getMark() != Mark.INPUT_RESPONCE && request.getRequest().getMark() != Mark.WAIT_NEXT)) {
            
            logger.warning("[SHARED CONSOLE] Invalid mark for requestQueue: " + request.getRequest().toString());
            return;
        }

        if (request.getClientAddress() == null) {
            logger.warning("[SHARED CONSOLE] Invalid clientAddress for request: " + request.getRequest().toString());
            return;
        }

        Server.getClientRequestQueue(request.getClientAddress()).offer(request); // положили нужному клиенту
        
        // if (clientAddress.equals()) {
            
        //     Server.getClientRequestQueue(clientAddress).offer(request);
        //     logger.info("Added to responseQueue: " + request.getRequest() + " from " + request.getClientAddress());
        // } else {
        //     logger.warning("Invalid request for responseQueue: " + (request != null && request.getRequest() != null ? request.getRequest() : "null"));
        // }
    }

    @Override
    public void write(String message) {
        if (clientAddress != null) {
            logger.info("Writing message to " + clientAddress + ": " + message);
            network.send(new Response(message), clientAddress);
        } else {
            logger.warning("No client address available for sending message");
        }
    }

    @Override
    public void writeln(String message) {
        write(message + "\n");
    }

    public void writeln(String message, boolean next) {
        if (next) {
            if (clientAddress == null) {
                logger.warning("No client address set for WAIT_NEXT request");
                return;
            }
            logger.info("Sending WAIT_NEXT with message: " + message + " to " + clientAddress);
            network.send(new Response(Mark.WAIT_NEXT, message), clientAddress);

            long startTime = System.currentTimeMillis();
            long timeoutMillis = 30000;

            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                try {
                    logger.info("Polling responseQueue for WAIT_NEXT from client: " + clientAddress);
                    ServerRequest request = Server.getClientRequestQueue(clientAddress).poll(100, TimeUnit.MILLISECONDS);
                    // if (request == null) {
                    //     logger.info("No request in queue, checking network.receive() for client: " + clientAddress);
                    //     request = network.receive();
                    //     if (request != null && request.getClientAddress().equals(clientAddress)) {
                    //         offerRequest(request); /// queue!
                    //         request = Server.getClientRequestQueue(clientAddress).poll();
                    //     }
                    // }
                    if (request != null && request.getRequest() != null) {
                        logger.info("Received request in writeln: " + request.getRequest() + " from " + request.getClientAddress());
                        if (request.getClientAddress().equals(clientAddress) && 
                            request.getRequest().getMark() == Mark.WAIT_NEXT) {
                            logger.info("Received WAIT_NEXT response from " + clientAddress);
                            return;
                        } else {
                            logger.warning("Unexpected request in writeln: " + request.getRequest() + " from " + request.getClientAddress());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.severe("Interrupted while waiting for WAIT_NEXT response");
                    return;
                }
            }
            logger.warning("Timeout waiting for WAIT_NEXT response from " + clientAddress);
        } else {
            writeln(message);
        }
    }

    public void writeln(Response response, boolean next) {
        if (clientAddress == null) {
            logger.warning("No client address set for sending response");
            return;
        }
        if (next) {
            response.setMark(Mark.WAIT_NEXT);
            logger.info("Sending WAIT_NEXT response to " + clientAddress + ": " + response+ "Thread  - "+Thread.currentThread().getName());
            network.send(response, clientAddress);

            long startTime = System.currentTimeMillis();
            long timeoutMillis = 30000;

            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                try {
                    logger.info("Polling responseQueue for WAIT_NEXT from client: " + clientAddress);
                    ServerRequest request = Server.getClientRequestQueue(clientAddress).poll(100, TimeUnit.MILLISECONDS);
                    // if (request == null) {
                    //     logger.info("No request in queue, checking network.receive() for client: " + clientAddress);
                    //     request = network.receive();
                    //     if (request != null && request.getClientAddress().equals(clientAddress)) {
                    //         offerResponse(request);
                    //         request = responseQueue.poll();
                    //     }
                    // }
                    if (request != null && request.getRequest() != null) {
                        logger.info("Received request in writeln: " + request.getRequest() + " from " + request.getClientAddress());
                        if (request.getClientAddress().equals(clientAddress) && 
                            request.getRequest().getMark() == Mark.WAIT_NEXT) {
                            logger.info("Received WAIT_NEXT response from " + clientAddress);
                            return;
                        } else {
                            logger.warning("Unexpected request in writeln: " + request.getRequest() + " from " + request.getClientAddress());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.severe("Interrupted while waiting for WAIT_NEXT response");
                    return;
                }
            }
            logger.warning("Timeout waiting for WAIT_NEXT response from " + clientAddress);
        } else {
            logger.info("Sending response to " + clientAddress + ": " + response);
            network.send(response, clientAddress);
        }
    }

    @Override
    public String read(String prompt) {
        if (clientAddress == null) {
            throw new IllegalStateException("No client address set for input request");
        }

        logger.info("Sending INPUT_REQUEST for prompt: " + prompt + " to " + clientAddress);
        network.send(new Response(Mark.INPUT_REQUEST, prompt), clientAddress);
        logger.info("Waiting for client response from " + clientAddress);

        long startTime = System.currentTimeMillis();
        long timeoutMillis = 30000; // Увеличили таймаут

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                logger.info("Polling responseQueue for INPUT_RESPONCE from client: " + clientAddress);
                ServerRequest request = Server.getClientRequestQueue(clientAddress).poll(100, TimeUnit.MILLISECONDS);
                // if (request == null) {
                //     logger.info("No request in queue, checking network.receive() for client: " + clientAddress);
                //     request = network.receive();
                //     if (request != null && request.getClientAddress().equals(clientAddress)) {
                //         offerRequest(request);
                //         request = responseQueue.poll();
                //     }
                // }
                if (request != null && request.getRequest() != null) {
                    logger.info("Received request in read: " + request.getRequest() + " from " + request.getClientAddress());
                    if (request.getClientAddress().equals(clientAddress) && 
                        request.getRequest().getMark() == Mark.INPUT_RESPONCE) {
                        String input = request.getRequest().getArgs().get(0);
                        // if (input == null || input.trim().isEmpty()) {
                        //     logger.warning("Received empty or null INPUT_RESPONCE from " + clientAddress);
                        //     network.send(new Response(Mark.INPUT_REQUEST, prompt + " (Input cannot be empty)"), clientAddress);
                        //     continue;
                        // }
                        logger.info("Received INPUT_RESPONCE from " + clientAddress + ": " + input);
                        return input;
                    } else {
                        logger.warning("Unexpected request in read: " + request.getRequest() + " from " + request.getClientAddress());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Input interrupted", e);
            }
        }

        logger.severe("Timeout waiting for client response from " + clientAddress);
        throw new RuntimeException("Timeout waiting for client response from " + clientAddress);
    }

    public NetworkServer getNetwork() {
        return network;
    }
}

// package lab6.server;

// import java.net.InetSocketAddress;
// import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.LinkedBlockingQueue;
// import java.util.concurrent.TimeUnit;
// import java.util.logging.Logger;
// import lab6.shared.io.connection.Mark;
// import lab6.shared.io.connection.Response;
// import lab6.shared.io.console.StandartConsole;
// import lab6.shared.io.connection.Request;

// public class SharedConsoleServer extends StandartConsole {
//     private final NetworkServer network;
//     private static final Logger logger = Logger.getLogger(SharedConsoleServer.class.getName());
//     private InetSocketAddress clientAddress;
//     private final BlockingQueue<ServerRequest> responseQueue = new LinkedBlockingQueue<>();

//     public SharedConsoleServer(NetworkServer network) {
//         this.network = network;
//     }

//     public void setClientAddress(InetSocketAddress address) {
//         clientAddress = address;
//     }

//     public void offerResponse(ServerRequest request) {
//         if (request != null && request.getRequest() != null && 
//             (request.getRequest().getMark() == Mark.INPUT_RESPONCE || 
//              request.getRequest().getMark() == Mark.WAIT_NEXT)) {
//             responseQueue.offer(request);
//             logger.info("Added to responseQueue: " + request.getRequest() + " from " + request.getClientAddress());
//         }
//     }

//     @Override
//     public void write(String message) {
//         if (clientAddress != null) {
//             network.send(new Response(message), clientAddress);
//         } else {
//             logger.warning("No client address available for sending message");
//         }
//     }

//     @Override
//     public void writeln(String message) {
//         write(message + "\n");
//     }

//     public void writeln(String message, boolean next) {
//         if (next) {
//             if (clientAddress == null) {
//                 logger.warning("No client address set for WAIT_NEXT request");
//                 return;
//             }
//             logger.info("Sending WAIT_NEXT with message: " + message + " to " + clientAddress);
//             network.send(new Response(Mark.WAIT_NEXT, message), clientAddress);

//             long startTime = System.currentTimeMillis();
//             long timeoutMillis = 15000;

//             while (System.currentTimeMillis() - startTime < timeoutMillis) {
//                 try {
//                     ServerRequest request = responseQueue.poll(100, TimeUnit.MILLISECONDS);
//                     if (request == null) {
//                         request = network.receive();
//                         if (request != null) {
//                             offerResponse(request);
//                             request = responseQueue.poll();
//                         }
//                     }
//                     if (request != null && request.getRequest() != null) {
//                         logger.info("Received request: " + request.getRequest() + " from " + request.getClientAddress());
//                         if (request.getClientAddress().equals(clientAddress) && 
//                             request.getRequest().getMark() == Mark.WAIT_NEXT) {
//                             logger.info("Received WAIT_NEXT response from " + clientAddress);
//                             return;
//                         } else {
//                             logger.warning("Unexpected request: " + request.getRequest() + " from " + request.getClientAddress());
//                         }
//                     }
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                     logger.severe("Interrupted while waiting for WAIT_NEXT response");
//                     return;
//                 }
//             }
//             logger.warning("Timeout waiting for WAIT_NEXT response from " + clientAddress);
//         } else {
//             writeln(message);
//         }
//     }

//     public void writeln(Response response, boolean next) {
//         if (clientAddress == null) {
//             logger.warning("No client address set for sending response");
//             return;
//         }
//         if (next) {
//             logger.info("Sending WAIT_NEXT response to " + clientAddress);
//             response.setMark(Mark.WAIT_NEXT);
//             network.send(response, clientAddress);

//             long startTime = System.currentTimeMillis();
//             long timeoutMillis = 15000;

//             while (System.currentTimeMillis() - startTime < timeoutMillis) {
//                 try {
//                     ServerRequest request = responseQueue.poll(100, TimeUnit.MILLISECONDS);
//                     if (request == null) {
//                         request = network.receive();
//                         if (request != null) {
//                             offerResponse(request);
//                             request = responseQueue.poll();
//                         }
//                     }
//                     if (request != null && request.getRequest() != null) {
//                         logger.info("Received request: " + request.getRequest() + " from " + request.getClientAddress());
//                         if (request.getClientAddress().equals(clientAddress) && 
//                             request.getRequest().getMark() == Mark.WAIT_NEXT) {
//                             logger.info("Received WAIT_NEXT response from " + clientAddress);
//                             return;
//                         } else {
//                             logger.warning("Unexpected request: " + request.getRequest() + " from " + request.getClientAddress());
//                         }
//                     }
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                     logger.severe("Interrupted while waiting for WAIT_NEXT response");
//                     return;
//                 }
//             }
//             logger.warning("Timeout waiting for WAIT_NEXT response from " + clientAddress);
//         } else {
//             logger.info("Sending response to " + clientAddress + ": " + response);
//             network.send(response, clientAddress);
//         }
//     }

//     @Override
//     public String read(String prompt) {
//         if (clientAddress == null) {
//             throw new IllegalStateException("No client address set for input request");
//         }

//         logger.info("Sending INPUT_REQUEST for prompt: " + prompt + " to " + clientAddress);
//         network.send(new Response(Mark.INPUT_REQUEST, prompt), clientAddress);
//         logger.info("Waiting for client response from " + clientAddress);

//         long startTime = System.currentTimeMillis();
//         long timeoutMillis = 15000;

//         while (System.currentTimeMillis() - startTime < timeoutMillis) {
//             try {
//                 ServerRequest request = responseQueue.poll(100, TimeUnit.MILLISECONDS);
//                 if (request == null) {
//                     request = network.receive();
//                     if (request != null) {
//                         offerResponse(request);
//                         request = responseQueue.poll();
//                     }
//                 }
//                 if (request != null && request.getRequest() != null) {
//                     logger.info("Received request: " + request.getRequest() + " from " + request.getClientAddress());
//                     if (request.getClientAddress().equals(clientAddress) && 
//                         request.getRequest().getMark() == Mark.INPUT_RESPONCE) {
//                         String input = request.getRequest().getArgs().get(0);
//                         if (input == null || input.trim().isEmpty()) {
//                             logger.warning("Received empty or null INPUT_RESPONCE from " + clientAddress);
//                             network.send(new Response(Mark.INPUT_REQUEST, prompt + " (Input cannot be empty)"), clientAddress);
//                             continue;
//                         }
//                         logger.info("Received INPUT_RESPONCE from " + clientAddress + ": " + input);
//                         return input;
//                     } else {
//                         logger.warning("Unexpected request: " + request.getRequest() + " from " + request.getClientAddress());
//                     }
//                 }
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//                 throw new RuntimeException("Input interrupted", e);
//             }
//         }

//         throw new RuntimeException("Timeout waiting for client response from " + clientAddress);
//     }

//     public NetworkServer getNetwork() {
//         return network;
//     }
// }


// package lab6.server;

// import java.net.InetSocketAddress;
// import java.util.Deque;
// import java.util.LinkedList;
// import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.LinkedBlockingQueue;
// import java.util.concurrent.ThreadFactory;
// import java.util.concurrent.TimeUnit;
// import java.util.logging.Logger;

// import lab6.shared.io.connection.Mark;
// import lab6.shared.io.connection.Response;
// import lab6.shared.io.console.StandartConsole;

// import lab6.shared.io.connection.Request;

// public class SharedConsoleServer extends StandartConsole {
//     private final NetworkServer network;
//     //private final BlockingQueue<String> inputQueue;
//     //private final Deque<String> inputQueue;
//     private static final Logger logger = Logger.getLogger(Router.class.getName());

//    // private ExecutorService readThreadPool;

//     //private static final int NUM_READ_THREAD_POOL = 5;

//     private InetSocketAddress clientAddress;

//     //private static final ThreadLocal<InetSocketAddress> clientAddress = new ThreadLocal<>();

//     public SharedConsoleServer(NetworkServer network) {
//         this.network = network;
//         //this.network.setConsole(this); // Устанавливаем консоль в sharedServer
//         //this.inputQueue = new LinkedBlockingQueue<>();
//         //this.inputQueue = new LinkedList<>();

//         // ThreadFactory priorityThreadFactory = new ThreadFactory() {
//         //     private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
//         //     @Override
//         //     public Thread newThread(Runnable r) {
//         //         Thread t = defaultFactory.newThread(r);
//         //         t.setPriority(Thread.MAX_PRIORITY - 1);
//         //         return t;
//         //     }
//         // };

//         //readThreadPool = Executors.newFixedThreadPool(NUM_READ_THREAD_POOL, priorityThreadFactory);
//     }

//     public void setClientAddress(InetSocketAddress address) {
//         clientAddress = (address);
//     }

//     // public static void setClientAddress(InetSocketAddress address) {
//     //     clientAddress.set(address);
//     // }

//     // public static InetSocketAddress getClientAddress() {
//     //     return clientAddress.get();
//     // }

//     // public static void clearClientAddress() {
//     //     clientAddress.remove();
//     // }

//     @Override
//     public void write(String message) {
//         //InetSocketAddress address = getClientAddress();
//         //if (address != null) {
//             //new Thread(() -> {
//                 network.send(new Response(message), clientAddress);
//             //}).start();
//         //} else {
//         //    logger.warning("No client address available for sending message");
//         //}
//     }

//     @Override
//     public void writeln(String message) {
//         write(message + "\n");
//     }

//     public void writeln(String message, boolean next) {
//         if (next) {
//             if (clientAddress == null) {
//                 logger.warning("No client address set for WAIT_NEXT request");
//                 return;
//             }
//             logger.info("Sending WAIT_NEXT with message: " + message + " to " + clientAddress);
//             network.send(new Response(Mark.WAIT_NEXT, message), clientAddress);
    
//             long startTime = System.currentTimeMillis();
//             long timeoutMillis = 10000;
    
//             while (System.currentTimeMillis() - startTime < timeoutMillis) {
//                 ServerRequest request = network.receive();
//                 if (request != null && request.getRequest() != null && 
//                     request.getClientAddress().equals(clientAddress) && 
//                     request.getRequest().getMark() == Mark.WAIT_NEXT) {
//                     logger.info("Received WAIT_NEXT response from " + clientAddress);
//                     return;
//                 }
//                 try {
//                     Thread.sleep(100);
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                     logger.severe("Interrupted while waiting for WAIT_NEXT response");
//                     return;
//                 }
//             }
//             logger.warning("Timeout waiting for WAIT_NEXT response from " + clientAddress);
//         } else {
//             writeln(message);
//         }
//     }

//     // public void writeln(String message, boolean next) {
//     //     if (next) {
//     //         //InetSocketAddress address = getClientAddress();
//     //         //if (address != null) {
//     //             //new Thread(() -> {
//     //                 network.send(new Response(Mark.WAIT_NEXT, message), clientAddress);
//     //                 handleClientInput();
//     //             //}).start();
//     //         //} else {
//     //             //logger.warning("No client address available for sending message");
//     //        // }
//     //     } else
//     //         writeln(message);
//     // }

//     // public void writeln(Response response, boolean next) {
//     //     // InetSocketAddress address = getClientAddress();
//     //     // if (address == null) {
//     //     //     logger.warning("No client address available for sending message");
//     //     //     return;
//     //     // }
//     //     if (next) {
//     //         //new Thread(() -> {
//     //             response.setMark(Mark.WAIT_NEXT);
//     //             network.send(response, clientAddress);
//     //             handleClientInput();
//     //         //}).start();
//     //     } else 
//     //         network.send(response, clientAddress);
        
//     //     //else {
//     //         //new Thread(() -> {
                
//     //         //}).start();
//     //     //}
//     // }

//     public void writeln(Response response, boolean next) {
//         if (clientAddress == null) {
//             logger.warning("No client address set for sending response");
//             return;
//         }
//         if (next) {
//             logger.info("Sending WAIT_NEXT response to " + clientAddress);
//             response.setMark(Mark.WAIT_NEXT);
//             network.send(response, clientAddress);
    
//             long startTime = System.currentTimeMillis();
//             long timeoutMillis = 10000;
    
//             while (System.currentTimeMillis() - startTime < timeoutMillis) {
//                 ServerRequest request = network.receive();
//                 if (request != null && request.getRequest() != null && 
//                     request.getClientAddress().equals(clientAddress) && 
//                     request.getRequest().getMark() == Mark.WAIT_NEXT) {
//                     logger.info("Received WAIT_NEXT response from " + clientAddress);
//                     return;
//                 }
//                 try {
//                     Thread.sleep(100);
//                 } catch (InterruptedException e) {
//                     Thread.currentThread().interrupt();
//                     logger.severe("Interrupted while waiting for WAIT_NEXT response");
//                     return;
//                 }
//             }
//             logger.warning("Timeout waiting for WAIT_NEXT response from " + clientAddress);
//         } else {
//             logger.info("Sending response to " + clientAddress + ": " + response);
//             network.send(response, clientAddress);
//         }
//     }

//     @Override
//     public String read(String prompt) {
//         if (clientAddress == null) {
//             throw new IllegalStateException("No client address set for input request");
//         }

//         logger.info("Sending INPUT REQUEST for prompt: " + prompt + " to " + clientAddress);
//         network.send(new Response(Mark.INPUT_REQUEST, prompt), clientAddress);
//         logger.info("Waiting for client response from " + clientAddress);

//         long startTime = System.currentTimeMillis();
//         long timeoutMillis = 10000; // 10 секунд таймаута

//         while (System.currentTimeMillis() - startTime < timeoutMillis) {
//             ServerRequest request = network.receive();
//             if (request != null && request.getRequest() != null && 
//                 request.getClientAddress().equals(clientAddress) && // Фильтрация по адресу клиента
//                 request.getRequest().getMark() == Mark.INPUT_RESPONCE) {
//                 String input = request.getRequest().getArgs().get(0);
//                 logger.info("Received INPUT_RESPONCE from " + clientAddress + ": " + input);
//                 return input;
//             }
//             try {
//                 Thread.sleep(100); // Небольшая пауза
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//                 throw new RuntimeException("Input interrupted", e);
//             }
//         }

//         throw new RuntimeException("Timeout waiting for client response from " + clientAddress);
//     }

//     // @Override
//     // public String read(String promt) {
//     //     //InetSocketAddress address = getClientAddress();
//     //     // if (address == null) {
//     //     //     throw new IllegalStateException("[Error] No client connect");
//     //     // }
//     //     // super.writeln("READ ADDRESS!!!!!!! = " + address);

//     //     // super.writeln("Sending INPUT REQUEST");

//     //     // super.writeln("Handling answer");

//     //     //new Thread(() -> {
//     //         super.writeln("Sending INPUT REQUEST");
//     //         network.send(new Response(Mark.INPUT_REQUEST, promt), clientAddress);
//     //         super.writeln("Handling answer");
//     //         handleClientInput();
//     //         //return inputQueue.take();
//     //     //}).start();

//     //     super.writeln("HERE");

//     //    // try {
//     //         //String ans = inputQueue.poll(1, TimeUnit.SECONDS);
//     //         String ans = inputQueue.pop();
//     //         logger.info("ANS_QUEUE = " + ans);
//     //         if (ans == null) {
//     //             throw new RuntimeException("Timeout waiting for client response");
//     //         }
//     //         return ans;
//     //     //} catch (InterruptedException e) {
//     //       //  Thread.currentThread().interrupt();
//     //        // throw new RuntimeException("Input interrupted.", e);
//     //     //}
//     // }

//     // public void handleClientInput(Request request_inp) {
//     //     super.writeln(request_inp.toString());
//     //     if (request_inp.getMark() != null) {
//     //         if (request_inp.getMark().equals(Mark.INPUT_RESPONCE))
//     //             ///inputQueue.offer(request_inp.getArgs().get(0));
//     //             inputQueue.push(request_inp.getArgs().get(0));
//     //             logger.info("ADD_TO_QUEUE = " + inputQueue.peek());
//     //         if (request_inp.getMark().equals(Mark.WAIT_NEXT))
//     //             return;
//     //     } else
//     //         logger.warning("Wrong input responce format");
//     //     // logger.info("Start handle client input reading (in class)");
//     //     // if (request_inp == null) {
//     //     //     logger.warning("Received null request in handleClientInput");
//     //     //     return;
//     //     // }
//     //     // InetSocketAddress addr = request_inp.getClientAddress();
//     //     // setClientAddress(addr);
//     //     // logger.info("handleClientInput called with address: " + addr);
//     //     // super.writeln(request_inp.getRequest().toString());
//     //     // if (request_inp.getRequest().getMark() != null) {
//     //     //     if (request_inp.getRequest().getMark().equals(Mark.INPUT_RESPONCE)) {
//     //     //         logger.info("Offering to inputQueue: " + request_inp.getRequest().getArgs().get(0));
//     //     //         inputQueue.offer(request_inp.getRequest().getArgs().get(0));
//     //     //     }
//     //     //     if (request_inp.getRequest().getMark().equals(Mark.WAIT_NEXT))
//     //     //         return;
//     //     // } else
//     //     //     logger.warning("Wrong input responce format");
//     // }

//     // public void handleClientInput() {
//     //     //readThreadPool.submit(() -> {
//     //         //while (!Thread.currentThread().isInterrupted()) {
//     //             ServerRequest request = network.receive();
//     //             if (request != null) {
//     //                 handleClientInput(request.getRequest());
//     //             }
//     //         //}
//     //    // });
//     // }

//     public NetworkServer getNetwork() {
//         return network;
//     }
// }
