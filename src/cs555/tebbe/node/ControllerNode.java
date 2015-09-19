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
    private ConcurrentHashMap<String, LiveChunkNodeData> chunkNodeMap = null;       // holds registered chunk nodes
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
            case Protocol.MINOR_HEARTBEAT:
                processMinorHeartbeat((Heartbeat) event);
                break;
            case Protocol.MAJOR_HEARTBEAT:
                processMajorHeartbeat((Heartbeat) event);
                break;
            case Protocol.READ_FILE_REQ:
                try {
                    processReadFileRequest((RequestChunk) event);
                } catch (IOException e) {
                    System.out.println("error sending read request");
                    e.printStackTrace();
                }
                break;
            default:
                display("unknown event type.");
        }
    }

    private void processReadFileRequest(RequestChunk event) throws IOException {
        NodeConnection client = bufferMap.get(event.getHeader().getSenderKey());
        client.sendEvent(EventFactory.buildFileRouteEvent(client, event.getFilename(), chunkTracker.getFileChunkLocations(event.getFilename())));
    }

    private void processMajorHeartbeat(Heartbeat event) {
        //System.out.println("major heartbeat from:" + event.getHeader().getSenderKey());
        chunkNodeMap.get(event.getHeader().getSenderKey()).replaceAllRecords(event.getRecords());
    }

    private void processMinorHeartbeat(Heartbeat event) {
        //System.out.println("minor heartbeat from:" + event.getHeader().getSenderKey());
        chunkNodeMap.get(event.getHeader().getSenderKey()).appendNewRecords(event.getRecords());
    }

    private synchronized void processStoreFileRequest(StoreFileRequest event) throws IOException {
        Event routeEvent = EventFactory.buildChunkRouteEvent(bufferMap.get(event.getSenderKey()),
                event.getFileName(), chunkTracker.allocateChunks(event));
        bufferMap.get(event.getSenderKey()).sendEvent(routeEvent);
    }

    private synchronized void registerChunkNode(Register event) {
        String key = event.getSenderKey();
        System.out.println("Registering Chunk Node: " + key);
        chunkNodeMap.put(key, new LiveChunkNodeData(bufferMap.get(key)));
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
