package cs555.tebbe.node;
import cs555.tebbe.transport.*;
import cs555.tebbe.wireformats.*;
import cs555.tebbe.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChunkNode implements Node {

    public static final int DEFAULT_SERVER_PORT = 18080;

    private NodeConnection _Controller = null;
    private TCPServerThread serverThread = null; // listens for incoming client nodes
    private HashMap<String, NodeConnection> clients = new HashMap<>();

    public ChunkNode(String host, int port) {
        try {
            serverThread = new TCPServerThread(this, new ServerSocket(DEFAULT_SERVER_PORT));
            serverThread.start();
        } catch(IOException ioe) {
            System.out.println("IOException thrown opening server thread:"+ioe.getMessage());
            System.exit(0);
        }

        try {
            _Controller = ConnectionFactory.getInstance().buildConnection(this, new Socket(host, port));
            _Controller.sendEvent(EventFactory.buildRegisterEvent(_Controller));
        } catch(IOException ioe) {
            System.out.println("IOException thrown contacting ControllerNode:"+ioe.getMessage());
            System.exit(0);
        }
    }

    public synchronized void onEvent(Event event){
        switch(event.getType()) {
            case Protocol.STORE_CHUNK:
                processStoreChunk((StoreChunk) event);
                break;
            case Protocol.CHUNK_ROUTE:
                System.out.println("chunk route received");

        }
    }

    private void processStoreChunk(StoreChunk event) {
        System.out.println("** Store chunk event received");
        System.out.println("File name: " + event.getFileName());
        System.out.println("Byte size: " + event.getBytesToStore().length);
        System.out.println("Bytes: " );
        try {
            System.out.write(event.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    public void registerConnection(NodeConnection connection) {
        clients.put(connection.getRemoteKey(), connection);
        System.out.println("New client: " + connection.getRemoteKey());
    }

    public static void main(String args[]) {
        if(args.length > 0) {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            new ChunkNode(host,port);
        } else {
            System.out.println("Usage: java ChunkNode controller_host controller_port");
        }
    }
}
