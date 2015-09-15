package cs555.tebbe.util;

import cs555.tebbe.node.ChunkInfo;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.wireformats.ChunkRoute;
import cs555.tebbe.wireformats.Protocol;
import cs555.tebbe.wireformats.StoreFileRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ct.
 */
public class ChunkTracker {

    private final ConcurrentHashMap<String, ChunkInfo> chunkNodeMap;

    public ChunkTracker(ConcurrentHashMap<String, ChunkInfo> nodeMap) {
        this.chunkNodeMap = nodeMap;
    }

    /*
        takes the number of needed allocated chunks and routes them
        and their replicas among active chunk nodes
     */
    public ChunkRoute[] allocateChunks(StoreFileRequest reqEvent) {
        int fSizeKB = reqEvent.getFileSizeKB();
        int numChunksToAllocate = (int) Math.ceil(fSizeKB/Protocol.CHUNK_SIZE_KB);

        ChunkRoute[] chunkRoutes = new ChunkRoute[numChunksToAllocate];
        List<ChunkInfo> chunkInfos = new ArrayList<>(chunkNodeMap.values()); // a snapshot of the current connections
        for(int i=0; i < chunkRoutes.length; i++)
            chunkRoutes[i] = generateUniqueChunkRoute(chunkInfos);
        return chunkRoutes;
    }

    /*
        creates a chunk route with unique machines to disperse replicas
     */
    private ChunkRoute generateUniqueChunkRoute(List<ChunkInfo> chunkInfos) {
        return null;
    }
}
