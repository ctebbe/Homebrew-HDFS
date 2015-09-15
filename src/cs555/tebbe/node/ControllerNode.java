package cs555.tebbe.node;
import cs555.tebbe.transport.*;
import cs555.tebbe.util.ChunkTracker;
import cs555.tebbe.wireformats.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerNode implements Node {


    private TCPServerThread serverThread = null;                            // listens for incoming connections
    private ConcurrentHashMap<String, NodeConnection> bufferMap    = null;  // buffers incoming unregistered connections
    private ConcurrentHashMap<String, ChunkInfo> chunkNodeMap = null;       // holds registered chunk nodes
    private ChunkTracker chunkTracker = null;                               // manage chunk distribution information

    public ControllerNode(int port) {
        try {
            bufferMap = new ConcurrentHashMap<>();
            chunkNodeMap = new ConcurrentHashMap<>();

            chunkTracker = new ChunkTracker(chunkNodeMap);

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
            case Protocol.STORE_FILE_REQ:
                try {
                    processStoreFileRequest((StoreFileRequest) event);
                } catch (IOException e) {
                    display("Error sending File Store Route Event");
                    e.printStackTrace();
                }
                break;
            default:
                display("unknown event type.");
        }
    }

    private synchronized void processStoreFileRequest(StoreFileRequest event) throws IOException {
        Event routeEvent = EventFactory.buildStoreFileRouteEvent(bufferMap.get(event.getSenderKey()),
                chunkTracker.allocateChunks(event));
        bufferMap.get(event.getSenderKey()).sendEvent(routeEvent);
    }

    private synchronized void registerChunkNode(Register event) {
        String key = event.getSenderKey();
        chunkNodeMap.put(key, new ChunkInfo(bufferMap.get(key)));
    }

    public synchronized void registerConnection(NodeConnection connection) {
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
