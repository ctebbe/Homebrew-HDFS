package cs555.tebbe.node;
import cs555.tebbe.transport.*;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkNode implements Node {

    public static final int MAJOR_HB_SECONDS = 60*3;
    public static final int MINOR_HB_SECONDS = MAJOR_HB_SECONDS/10;

    public static final int DEFAULT_SERVER_PORT = 18080;
    public static final String BASE_SAVE_DIR = "./";

    private NodeConnection _Controller = null;
    private TCPServerThread serverThread = null;                                // listens for incoming client nodes
    private ConcurrentHashMap<String, ChunkStorage> storedChunksMap = new ConcurrentHashMap<>();
    private List<ChunkStorage> newChunksStored = new ArrayList<>();             // holds data items for minor heartbeats

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
        new Timer().schedule(new HeartbeatTaskManager(), 0, MINOR_HB_SECONDS*1000);
    }

    public synchronized void onEvent(Event event){
        switch(event.getType()) {
            case Protocol.STORE_CHUNK:
                processStoreChunk((StoreChunk) event);
                break;
            case Protocol.CHUNK_ROUTE:
                System.out.println("chunk route received");
                break;
            case Protocol.CHUNK_REQ:
                processChunkRequest((RequestChunk) event);
                break;

        }
    }

    private void processChunkRequest(RequestChunk event) {
        System.out.println(event.getHeader().getSenderKey() + " requesting chunk " + event.getChunkStorageName());
    }

    private void processStoreChunk(StoreChunk event) {
        System.out.println("** Storing new chunk for file: " + event.getFileName());

        ChunkStorage record = new ChunkStorage(event.getFileName(), event.getVersion(), event.getChunkSequenceID(), new Date().getTime(), Util.getCheckSum(event.getBytesToStore()));
        System.out.println("Checksum:" + record.getChecksum());
        storeChunk(record.getChunkStorageName(), event.getBytesToStore());
        storeNewRecord(record);
        if(event.getNextHost() != null) { // forward replicas to other nodes
            try {
                NodeConnection nc = new NodeConnection(this, new Socket(event.getNextHost(), ChunkNode.DEFAULT_SERVER_PORT));
                nc.sendEvent(EventFactory.buildStoreChunkEvent(nc, event));
            } catch (IOException e) {
                System.out.println("Error forwarding process store chunk");
                e.printStackTrace();
            }
        }
    }

    private void storeNewRecord(ChunkStorage record) {
        synchronized (newChunksStored) {
            newChunksStored.add(record);
        }
        storedChunksMap.put(record.getChunkStorageName(), record);
    }

    private void storeChunk(String storeFileName, byte[] toStore) {
        BufferedOutputStream writer = null;
        try {
            writer = new BufferedOutputStream(new FileOutputStream(new File(BASE_SAVE_DIR +storeFileName)));
            writer.write(toStore);
        } catch (IOException e) {
            System.out.println("Error saving file...");
            e.printStackTrace();
        } finally {
            if (writer != null) { try { writer.close(); } catch (IOException e) {} }
        }
    }

    public void registerConnection(NodeConnection connection) {
        System.out.println("New connection: " + connection.getRemoteKey());
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

    /*
        sends heartbeats to the _Controller
        every ratioMinorToMajor heartbeats, a major heartbeat is sent
        otherwise a minor heartbeat is sent
     */
    private class HeartbeatTaskManager extends TimerTask {

        private final int ratioMinorToMajor = 10;
        private AtomicInteger numHeartbeats = new AtomicInteger(0);

        @Override public void run() {
            if(numHeartbeats.incrementAndGet() % ratioMinorToMajor == 0) {
                try {
                    _Controller.sendEvent(EventFactory.buildMajorHeartbeat(_Controller, storedChunksMap.values().toArray(new ChunkStorage[]{})));
                } catch (IOException e) { System.out.println("Error sending major heartbeat"); }
                //System.out.println("major heartbeat");
            } else
                try {
                    synchronized (newChunksStored) {
                        _Controller.sendEvent(EventFactory.buildMinorHeartbeat(_Controller, newChunksStored.toArray(new ChunkStorage[]{})));
                        newChunksStored.clear();
                    }
                } catch (IOException e) { System.out.println("Error sending minor heartbeat"); }
                //System.out.println("minor heartbeat");
        }
    }
}
