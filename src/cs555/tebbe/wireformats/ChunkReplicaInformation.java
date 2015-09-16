package cs555.tebbe.wireformats;

import java.io.*;

/**
 * Created by ct.
 */
public class ChunkReplicaInformation implements Event {

    private final String chunkName;
    private final String[] replicaChunkNodes;

    public ChunkReplicaInformation(String id, String[] replicas) {
        this.chunkName = id;
        this.replicaChunkNodes = replicas;
    }

    public static ChunkReplicaInformation parseChunkRoute(DataInputStream din) throws IOException {
        // chunk name
        int nameLen = din.readInt();
        byte[] receiverBytes = new byte[nameLen];
        din.readFully(receiverBytes);
        String chunkName = new String(receiverBytes);

        // replicas
        String[] replicas = new String[din.readInt()];
        for(int i=0; i < replicas.length; i++) {
            byte[] replicaBytes = new byte[din.readInt()];
            din.readFully(replicaBytes);
            replicas[i] = new String(receiverBytes);
        }

        return new ChunkReplicaInformation(chunkName, replicas);
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // chunk name
        byte[] nameBytes = chunkName.getBytes();
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

        // replicas
        dout.writeInt(replicaChunkNodes.length);
        for(String replicaNode : replicaChunkNodes) {
            byte[] replicaBytes = replicaNode.getBytes();
            dout.writeInt(replicaBytes.length);
            dout.write(replicaBytes);
        }

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    public String getChunkName() {
        return chunkName;
    }

    public String[] getReplicaChunkNodes() {
        return replicaChunkNodes;
    }

    @Override
    public int getType() {
        return Protocol.NOTYPE;
    }
}
