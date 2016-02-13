package SensorEngine;

/** <p>Title: SocketServer</p>
 * <p>Description: Create a new SocketServer to handle TCP/IP communications</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Rich Mulvey
 * @version 1.0
 */

public class SocketServer extends Thread {
    
    /** The ServerSocket controlling the communications
     */
    protected java.net.ServerSocket serverSocket;
    /** The socket controlling Multicasts ( not used )
     */
   // protected java.net.MulticastSocket multiSocket;
    /** The socket input stream
     */
    protected java.io.InputStream inputStream;
    /** The socket output stream
     */
    protected java.io.OutputStream outputStream;
    /** A list of all the client streams connected to this server
     */
    protected java.util.ArrayList outputStreams = new java.util.ArrayList();
    /** The port number for this server
     */
    protected int port;
    
    /**
     * Flag to indicate if the server should be stopped
     */
    protected boolean stopThread = false;
   
    
    /** Create a new SocketServer with the given port
     * @param port Port number for the server
     */
    public SocketServer( int port ) {
        this.port = port;
    }
    
    public void setStopThread( boolean stopThread ) {
        this.stopThread = stopThread;
    }
    
    /** Run the socketserver thread
     */
    public void run() {
        java.net.Socket socket = null;
        try {
            
            serverSocket = new java.net.ServerSocket(port);
            //multiSocket = new java.net.MulticastSocket();
            
            serverSocket.setSoTimeout(1000);
            
            while( !stopThread ) {
                
                try {
                    socket = serverSocket.accept();
                    
                    socket.getOutputStream().write("Connected to UnkaMimmy's Weather Server\n".getBytes());
                    outputStreams.add( socket );
                    
                } catch ( java.io.InterruptedIOException interrupted ) {
                    // Do nothing - this is just so we can check isStopThread
                }
                
            }
            
            
            System.out.println( "Stopping the streams");
            closeStreams();
            System.out.println( "Streams have been stopped");
            
            
        } catch (Exception e) {
            System.err.println("An exception occurred when opening the socket!" + e);
        }
        
        System.out.println( "Thread is exiting");
        
    }
    
    
    /**
     * Close the output streams
     */
    public void closeStreams() {
        
        java.net.Socket socket = null;
        
        sendMessage( "The weather server is shutting down");
        while( outputStreams.isEmpty() == false ) {
            try {
                
                System.out.println( "Closing the next socket");
                socket = ( java.net.Socket ) outputStreams.remove(0);
                socket.close();
                
            } catch ( Exception e ) {
                System.err.println( "Error trying to shut down a socket :" + e );
            }
        }
        
        
    }
    
    /** Send a message to the clients
     * @param message The message to send
     *
     */
    public void sendMessage( String message ) {
        
        int count = 0;
        java.net.Socket socket = null;
        long sendMessageTimeOut = System.currentTimeMillis();
        //message += "\n";
        
        
        while( count < outputStreams.size() ) {
            
            try {
                socket = ( java.net.Socket ) outputStreams.get( count );
                socket.getOutputStream().write( message.getBytes());
                if(System.currentTimeMillis() >= sendMessageTimeOut + 20000  ){
                socket.close();
                System.err.println("Got a sendMessage timeout in socketServer method");
                }
            } catch ( Exception e ) {
                try {
                    socket.close();
                } catch ( java.io.IOException io ) {
                
                }
                outputStreams.remove( socket );
            } finally {
                count++;
            }
        }
        
    }
    
}
