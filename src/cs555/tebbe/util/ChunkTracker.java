package cs555.tebbe.util;

import cs555.tebbe.node.ChunkNodeAllocationInfo;
import cs555.tebbe.wireformats.ChunkReplicaInformation;
import cs555.tebbe.wireformats.Protocol;
import cs555.tebbe.wireformats.StoreFileRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ct.
 */
public class ChunkTracker {

    private final ConcurrentHashMap<String, ChunkNodeAllocationInfo> chunkNodeMap;
    private final ConcurrentHashMap<String, ChunkReplicaInformation[]> fileTrackerMap;

    public ChunkTracker(ConcurrentHashMap<String, ChunkNodeAllocationInfo> nodeMap) {
        this.chunkNodeMap = nodeMap;
        fileTrackerMap = new ConcurrentHashMap<>();
    }

    /*
        takes the number of needed allocated chunks and routes them
        and their replicas among active chunk nodes
     */
    public ChunkReplicaInformation[] allocateChunks(StoreFileRequest reqEvent) {
        System.out.println("Allocating chunks...");
        int fSizeKB = reqEvent.getFileSizeKB();
        int numChunksToAllocate = getNumChunksToAllocate(fSizeKB);

        ChunkReplicaInformation[] chunkReplicas = new ChunkReplicaInformation[numChunksToAllocate];
        for(int i=0; i < chunkReplicas.length; i++)
            chunkReplicas[i] = generateUniqueChunkRoute();
        fileTrackerMap.put(reqEvent.getFileName(), chunkReplicas);

        System.out.println();
        System.out.println("File store request");
        System.out.println("File size KB:" + fSizeKB);
        System.out.println("Replicas assigned:");
        System.out.println("File name: " + reqEvent.getFileName());
        for(ChunkReplicaInformation info : chunkReplicas) {
            System.out.println("-- new Chunk");
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
    private ChunkReplicaInformation generateUniqueChunkRoute() {
        List<ChunkNodeAllocationInfo> nodeInfos = new ArrayList<>(chunkNodeMap.values()); // a snapshot of the current connections
        boolean allSame = false;
        int compareNum = nodeInfos.get(0).getChunks();
        for(ChunkNodeAllocationInfo info : nodeInfos) {
            allSame = compareNum == info.getChunks() ? true : false;
            if(!allSame) break;
        }

        if(allSame)
            Collections.shuffle(nodeInfos); // if chunks are equally distributed, randomly assign replicas
        else
            Collections.sort(nodeInfos, new Comparator<ChunkNodeAllocationInfo>() {
                @Override
                public int compare(ChunkNodeAllocationInfo o1, ChunkNodeAllocationInfo o2) {
                    return new Integer(o1.getChunks()).compareTo(o2.getChunks());
                }
            });

        List<String> replicaList = new ArrayList<>();
        for(int i=0; i < Protocol.NUM_REPLICAS_PER_CHUNK; i++) {
            String nodeKey = nodeInfos.get(i).getNodeKey();
            chunkNodeMap.get(nodeKey).incrementChunks(1);
            replicaList.add(Util.removePort(nodeKey));
        }
        return new ChunkReplicaInformation(replicaList.toArray(new String[]{}));
    }
}
