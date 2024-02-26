package SSProgramming;

import io.github.pixee.security.BoundedLineReader;
import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A multithreaded chat room server.  When a client connects the
 * server requests a screen name by sending the client the
 * text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received.  After a client submits a unique
 * name, the server acknowledges with "NAMEACCEPTED".  Then
 * all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name.  The
 * broadcast messages are prefixed with "MESSAGE ".
 *
 * Because this is just a teaching example to illustrate a simple
 * chat server, there are a few features that have been left out.
 * Two are very useful and belong in production code:
 *
 *     1. The protocol should be enhanced so that the client can
 *        send clean disconnect messages to the server.
 *
 *     2. The server should do some logging.
 */
public class ChatServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 9001;
    
    
    /*The set of all names of clients and the printwriters of the clients
     * Used to find out the client and the message that needs to be sent 
     * during point to point connection
     * */
    private static HashMap<String,PrintWriter> clientMap  = new HashMap<String,PrintWriter> ();
    

    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
   
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
            	Socket socket  = listener.accept();
                Thread handlerThread = new Thread(new Handler(socket));
                handlerThread.start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    name = BoundedLineReader.readLine(in, 5_000_000);
                    if (name == null) {
                        return;
                    }
                    
                    // TODO: Add code to ensure the thread safety of the
                    // the shared variable 'names'
                    synchronized(names) {
                     if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                   
                 }
                }

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                writers.add(out);
                
                // TODO: You may have to add some code here to broadcast all clients the new
                // client's name for the task 9 on the lab sheet. 
                clientMap.put(name, out);//the name and the message is added to the hashmap
                
                /* Iterate through the hashmap to provide protocol to
                 * find out the names that are selected in the list */
                for(String key : clientMap.keySet()) {
                	if(!key.equals(name)) {
                		clientMap.get(key).println("NEWNAME"+name);
                	}
                	
                }						
                           
                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = BoundedLineReader.readLine(in, 5_000_000);
                   
                    if (input == null) {
                        return;
                    }
                    
                   
                    // TODO: Add code to send a message to a specific client and not
                    // all clients. You may have to use a HashMap to store the sockets along 
                    // with the chat client names
                    
                    
                    /** Checks if the input contains ">>" and if true, user name is obtained by using substring method then the 
                     * message is obtained*/
                    else if(input.contains(">>"))
                    {
                    	 System.out.println("readline");
                    	
                    	String user=input.substring(0,input.indexOf('>'));
                    	int index=input.lastIndexOf(">");
                    	String msg=input.substring((index+1));
                    	System.out.println(user);
                    	for(HashMap.Entry<String,PrintWriter> writer:clientMap.entrySet()) {//Using entrySet to get the entries of the map
                    		if(writer.getKey().matches(user)) {//checking the user name against the hashmap key value
                    			PrintWriter foundWriter = writer.getValue();//getting the value of the key when conditon is true
                    		
                    			foundWriter.println("MESSAGE " + name + ": " + msg);//printing out the protocol to identify with name and message
                    			
                    		}
                    		if(writer.getKey().matches(name)) {//finding out the key value of the name that sent the message
                    			PrintWriter foundWriter = writer.getValue();
                    			foundWriter.println("MESSAGE " + name + ": " + msg);
                    		}
                    		
                    	}
                    }
                    
                    /* Check if the input contains "MULTI" if true then remove the protocol word and then obtain the message and the users 
                     * and add the users to an array list*/
                    else if(input.contains("MULTI")) {
                    		//System.out.println("found");
                    		
                    		String remove = input.replaceAll("MULTI", "");
                    		//System.out.println(remove);
                    		int FindexofChar=remove.indexOf("[");
                    		int LindexofChar=remove.lastIndexOf("]");
                    		String msg = remove.substring(0,FindexofChar);
                    		
                    		
                    		String users=remove.substring(FindexofChar+1,LindexofChar);
                    		String newUsers=users.trim().replaceAll("\\s+","");

//                    		System.out.println("not trimmed: "+users);
//                    		System.out.println("trimmed "+newUsers);
                    		String[] ArrayUsers = newUsers.split(",");
                    		
                    		ArrayList <String> ArrUsers= new ArrayList<>() ;
                    		Collections.addAll(ArrUsers,ArrayUsers);
              	    
//                    		System.out.println(ArrUsers.size());
                    		
                    		for(String keys:ArrUsers){
                    				//System.out.println("twice");
                    				for(HashMap.Entry<String,PrintWriter> writer:clientMap.entrySet()) {
                    			
                    			
                    				//System.out.println("foundwriter 111");
                    				if(writer.getKey().matches(keys)) {
                            			PrintWriter foundWriter = writer.getValue();
                            			//System.out.println("foundwriter");
                            			foundWriter.println("MULTIC " + name + ": " + msg);
                            				}
                    				
                    				if(writer.getKey().matches(name)) {//finding out the key value of the name that sent the message
                            			PrintWriter foundWriter = writer.getValue();
                            			foundWriter.println("MESSAGE " + name + ": " + msg);
                            				}
                    				
                    					}
                    			
                    				}
                    			
                    		}
                    	 
                    else {
                    for (PrintWriter writer : writers) {
                    	 
                        writer.println("MESSAGE " + name + ": " + input);
                    	}
                    }
                }
            }// TODO: Handle the SocketException here to handle a client closing the socket
            catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                	for(String keyVal : clientMap.keySet()) {
                		if(!keyVal.equals(name)) {
                			clientMap.get(keyVal).println("CLOSE"+name);
                		}
                	}
                	clientMap.remove(name);
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
