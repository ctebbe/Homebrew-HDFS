package cs555.tebbe.node;
import cs555.tebbe.transport.*;
import cs555.tebbe.wireformats.*;
import cs555.tebbe.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkNode implements Node {

    public static final int MAJOR_HB_SECONDS = 60*3;
    public static final int MINOR_HB_SECONDS = MAJOR_HB_SECONDS/10;

    public static final int DEFAULT_SERVER_PORT = 18080;
    public static final String BASE_DIR = "./";

    private NodeConnection _Controller = null;
    private TCPServerThread serverThread = null;                                // listens for incoming client nodes
    private HashMap<String, ChunkStorage> storedChunksMap = new HashMap<>();
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

        }
    }

    private void processStoreChunk(StoreChunk event) {
        System.out.println("** Store chunk event received");
        System.out.println("File name: " + event.getFileName());
        System.out.println("Byte size: " + event.getBytesToStore().length);

        ChunkStorage record = new ChunkStorage(event.getFileName(), "0.1", event.getChunkSequenceID(), new Date().getTime());
        BufferedOutputStream writer = null;
        try {
            writer = new BufferedOutputStream(new FileOutputStream(
                    new File(BASE_DIR+record.getChunkStorageName())));
            saveChunk(writer, event);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException e) {}
            }
        }

        System.out.println(event.getNextHost());
        storedChunksMap.put(record.getFileName(), record);
    }

    private void saveChunk(BufferedOutputStream writer, StoreChunk event) throws IOException {
        writer.write(event.getBytesToStore());
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
            if(numHeartbeats.getAndIncrement() % ratioMinorToMajor == 0) {
                System.out.println("major heartbeat");
            } else
                System.out.println("minor heartbeat");
            //_Controller.sendEvent(null);
        }
    }
}
