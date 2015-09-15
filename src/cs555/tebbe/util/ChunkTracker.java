package cs555.tebbe.util;

import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.wireformats.ChunkRoute;
import cs555.tebbe.wireformats.Protocol;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ct.
 */
public class ChunkTracker {

    private final ConcurrentHashMap<String, NodeConnection> chunkNodeMap;
    private final HashMap<String, Integer> allocatedChunkMap;

    public ChunkTracker(ConcurrentHashMap<String, NodeConnection> nodeMap) {
        this.chunkNodeMap = nodeMap;
        allocatedChunkMap = new HashMap<>();
    }

    public ChunkRoute[] allocateChunks(int numChunksNeeded) {
        ChunkRoute[] chunkRoutes = new ChunkRoute[numChunksNeeded];
        for(int i=0; i < chunkRoutes.length; i++) {
            chunkRoutes[i] = getLeastCapacityChunkNodes(Protocol.NUM_REPLICAS_PER_CHUNK);
        }
        return chunkRoutes;
    }

    private ChunkRoute getLeastCapacityChunkNodes(int numChunkNodesNeeded) {
        synchronized (allocatedChunkMap) {
        }
    }
}
