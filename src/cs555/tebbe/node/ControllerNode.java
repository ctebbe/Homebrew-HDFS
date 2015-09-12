package cs555.tebbe.node;
import cs555.tebbe.transport.*;
import cs555.tebbe.wireformats.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerNode implements Node {

    private TCPServerThread serverThread = null;                            // listens for incoming connections
    private ConcurrentHashMap<String, NodeConnection> bufferMap    = null;  // buffers incoming unregistered connections
    private ConcurrentHashMap<String, NodeConnection> chunkNodeMap = null;  // holds registered chunk nodes

    public ControllerNode(int port) {
        try {
            bufferMap = new ConcurrentHashMap<>();
            chunkNodeMap = new ConcurrentHashMap<>();

            serverThread = new TCPServerThread(this, new ServerSocket(port));
            serverThread.start();
        } catch(IOException ioe) {
            display("IOException on ControllerNode:"+ioe.toString());
        }
    }

    public synchronized void onEvent(Event event) {
        switch(event.getType()) {
            case Protocol.REGISTER: // chunk node registration
                registerChunkNode((Register) event);
                break;
            default:
                display("unknown event type.");
        }
    }

    private void registerChunkNode(Register event) {
        String key = event.getSenderKey();
        chunkNodeMap.put(key, bufferMap.remove(key));
    }

    public void registerConnection(NodeConnection connection) {
        bufferMap.put(connection.getRemoteKey(), connection);
    }

    public void display(String str) {
        System.out.println(str);
    }

    public static void main(String args[]) {
        int port = 8080; // default listening port
        if(args.length > 0) port = Integer.parseInt(args[0]);
        new ControllerNode(port);
    }
}
