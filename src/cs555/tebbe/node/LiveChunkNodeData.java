package cs555.tebbe.node;

import cs555.tebbe.transport.NodeConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ctebbe
 */
public class LiveChunkNodeData {

    private final NodeConnection connection;
    private List<ChunkStorage> storageRecords = new ArrayList<>();

    public LiveChunkNodeData(NodeConnection connection) {
        this.connection = connection;
    }

    public NodeConnection getConnection() {
        return connection;
    }

    public String getNodeKey() {
        return connection.getRemoteKey();
    }

    public ChunkStorage[] getStorageRecords() {
        return storageRecords.toArray(new ChunkStorage[]{});
    }
    public void appendNewRecords(ChunkStorage[] recordsToAdd) {
        synchronized (storageRecords) {
            storageRecords.addAll(Arrays.asList(recordsToAdd));
        }
    }

    public void replaceAllRecords(ChunkStorage[] newRecords) {
        synchronized (storageRecords) {
            storageRecords = new ArrayList<>(Arrays.asList(newRecords));
        }
    }

    public int getChunks() {
        synchronized (storageRecords) {
            return storageRecords.size();
        }
    }
}
