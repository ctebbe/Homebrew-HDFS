package cs555.tebbe.node;

import cs555.tebbe.transport.NodeConnection;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ctebbe
 */
public class ChunkNodeAllocationInfo {

    public final NodeConnection connection;
    private final AtomicInteger numChunks;

    public ChunkNodeAllocationInfo(NodeConnection connection) {
        this.connection = connection;
        this.numChunks = new AtomicInteger(0);
    }

    public String getNodeKey() {
        return connection.getRemoteKey();
    }

    public void incrementChunks(int numAdded) {
        numChunks.addAndGet(numAdded);
    }

    public void setNumChunks(int chunks) {
        numChunks.getAndSet(chunks);
    }

    public int getChunks() {
        return numChunks.get();
    }
}
