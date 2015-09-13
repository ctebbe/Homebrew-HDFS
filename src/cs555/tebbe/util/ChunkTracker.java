package cs555.tebbe.util;

import cs555.tebbe.transport.NodeConnection;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ct.
 */
public class ChunkTracker {

    private final ConcurrentHashMap<String, NodeConnection> chunkNodeMap;
    private final ConcurrentHashMap<String, Integer> allocatedChunkMap;

    public ChunkTracker(ConcurrentHashMap<String, NodeConnection> nodeMap) {
        this.chunkNodeMap = nodeMap;
        allocatedChunkMap = new ConcurrentHashMap<>();
    }

    public void processStoreFileRequest(String senderKey, int numChunks) {
    }
}
