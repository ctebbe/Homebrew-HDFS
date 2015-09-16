package cs555.tebbe.util;

import cs555.tebbe.node.ChunkInfo;
import cs555.tebbe.wireformats.ChunkReplicaInformation;
import cs555.tebbe.wireformats.ChunkRoute;
import cs555.tebbe.wireformats.Protocol;
import cs555.tebbe.wireformats.StoreFileRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ct.
 */
public class ChunkTracker {

    private final ConcurrentHashMap<String, ChunkInfo> chunkNodeMap;
    private final ConcurrentHashMap<String, ChunkReplicaInformation[]> fileTrackerMap;

    public ChunkTracker(ConcurrentHashMap<String, ChunkInfo> nodeMap) {
        this.chunkNodeMap = nodeMap;
        fileTrackerMap = new ConcurrentHashMap<>();
    }

    /*
        takes the number of needed allocated chunks and routes them
        and their replicas among active chunk nodes
     */
    public ChunkReplicaInformation[] allocateChunks(StoreFileRequest reqEvent) {
        int fSizeKB = reqEvent.getFileSizeKB();
        int numChunksToAllocate = getNumChunksToAllocate(fSizeKB);

        List<ChunkInfo> chunkInfos = new ArrayList<>(chunkNodeMap.values()); // a snapshot of the current connections

        ChunkReplicaInformation[] chunkReplicas = new ChunkReplicaInformation[numChunksToAllocate];
        for(int i=0; i < chunkReplicas.length; i++)
            chunkReplicas[i] = generateUniqueChunkRoute(reqEvent.getFileName(), chunkInfos);
        fileTrackerMap.put(reqEvent.getFileName(), chunkReplicas);

        System.out.println();
        System.out.println("File store request");
        System.out.println("File size KB:" + fSizeKB);
        System.out.println("Replicas assigned:");
        for(ChunkReplicaInformation info : chunkReplicas) {
            System.out.println(info.getChunkName());
            for(String s : info.getReplicaChunkNodes()) {
                System.out.println("\t" + s);
            }
        }
        System.out.println();
        return chunkReplicas;
    }

    public static int getNumChunksToAllocate(int fSizeKB) {
        return (int) Math.ceil(fSizeKB/Protocol.CHUNK_SIZE_KB);
    }

    /*
        creates a chunk route with unique machines to disperse replicas
     */
    private ChunkReplicaInformation generateUniqueChunkRoute(String fileName, List<ChunkInfo> nodeInfos) {
        Collections.sort(nodeInfos, new Comparator<ChunkInfo>() {
            @Override public int compare(ChunkInfo o1, ChunkInfo o2) {
                return new Integer(o1.getChunks()).compareTo(new Integer(o2.getChunks()));
            }
        });

        List<String> replicaList = new ArrayList<>();
        for(int i=0; i < Protocol.NUM_REPLICAS_PER_CHUNK; i++)
            replicaList.add(nodeInfos.get(i).getNodeKey());
        return new ChunkReplicaInformation(fileName, replicaList.toArray(new String[]{}));
    }
}
