package cs555.tebbe.util;

import cs555.tebbe.node.ChunkStorage;
import cs555.tebbe.node.LiveChunkNodeData;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.wireformats.ChunkReplicaInformation;
import cs555.tebbe.wireformats.EventFactory;
import cs555.tebbe.wireformats.Protocol;
import cs555.tebbe.wireformats.StoreFileRequest;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ct.
 */
public class ChunkTracker {

    private final ConcurrentHashMap<String, LiveChunkNodeData> chunkNodeMap;
    private final ConcurrentHashMap<String, ChunkReplicaInformation[]> fileTrackerMap;

    public ChunkTracker(ConcurrentHashMap<String, LiveChunkNodeData> nodeMap) {
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
            chunkReplicas[i] = generateUniqueChunkRoute(Protocol.NUM_REPLICAS_PER_CHUNK);
        fileTrackerMap.put(reqEvent.getFileName(), chunkReplicas);

        System.out.println();
        System.out.println("File store request");
        System.out.println("File size KB:" + fSizeKB);
        System.out.println("Replicas assigned:");
        System.out.println("File name: " + reqEvent.getFileName());
        int i=0;
        for(ChunkReplicaInformation info : chunkReplicas) {
            System.out.println("-- Chunk " + i++);
            for(String s : info.getReplicaChunkNodes()) {
                System.out.println("\t" + s);
            }
        }
        System.out.println();
        return chunkReplicas;
    }

    public ChunkReplicaInformation[] getFileChunkLocations(String filename) {
        return fileTrackerMap.get(filename);
    }

    public static int getNumChunksToAllocate(int fSizeKB) {
        int chunks = (int) Math.ceil(fSizeKB/Protocol.CHUNK_SIZE_KB);
        return (chunks == 0) ? 1 : chunks;                              // store files <64KB in a single chunk
    }

    /*
        creates a chunk route with unique machines to disperse replicas
     */
    private ChunkReplicaInformation generateUniqueChunkRoute(int numNodesNeeded) {
        List<LiveChunkNodeData> nodeInfos = new ArrayList<>(chunkNodeMap.values()); // a snapshot of the current connections
        Collections.shuffle(nodeInfos); // randomly shuffle nodes
        /*
        int numSame = 0;
        int compareNum = nodeInfos.get(0).getChunks();
        for(LiveChunkNodeData info : nodeInfos) {
            if(compareNum == info.getChunks()) numSame++;
        }

        if(numSame/nodeInfos.size() > 0.75) {
            System.out.println("shuffle");
            Collections.shuffle(nodeInfos); // randomly shuffle nodes
        }
        else
            Collections.sort(nodeInfos, new Comparator<LiveChunkNodeData>() {
                @Override
                public int compare(LiveChunkNodeData o1, LiveChunkNodeData o2) {
                    return new Integer(o1.getChunks()).compareTo(o2.getChunks());
                }
            });
        */

        List<String> replicaList = new ArrayList<>();
        for(int i=0; i < numNodesNeeded; i++) {
            String nodeKey = nodeInfos.get(i).getNodeKey();
            replicaList.add(Util.removePort(nodeKey));
        }
        return new ChunkReplicaInformation(replicaList.toArray(new String[]{}));
    }

    public void processDeadNode(String disconnectedNodeKey, List<String> keys) throws IOException {
        LiveChunkNodeData removedNodeData = chunkNodeMap.remove(disconnectedNodeKey);
        for(ChunkStorage record : removedNodeData.getStorageRecords()) {
            ChunkReplicaInformation info = fileTrackerMap.get(record.getFileName())[record.getSequence()];
            List<String> currReplicas = Arrays.asList(info.getReplicaChunkNodes());
            String newReplicaHostname = "";
            while(true) {
                newReplicaHostname = generateUniqueChunkRoute(Protocol.NUM_REPLICAS_PER_CHUNK).getReplicaChunkNodes()[0]; // new desired replica node
                if(!currReplicas.contains(newReplicaHostname)) break;
            }
            System.out.println(record.getFileName() + ": new replica for chunk "+ record.getSequence() +": " + newReplicaHostname);
            if(info.replaceReplicaRecord(Util.removePort(disconnectedNodeKey), newReplicaHostname)) {
                for(String key : keys)
                    if(key.contains(newReplicaHostname)) {
                        NodeConnection newConnection = chunkNodeMap.get(key).getConnection();
                        newConnection.sendEvent(EventFactory.buildChunkRouteEvent(newConnection, record.getFileName(), record.getSequence(), fileTrackerMap.get(record.getFileName())));
                    }
            }
        }
    }
}
