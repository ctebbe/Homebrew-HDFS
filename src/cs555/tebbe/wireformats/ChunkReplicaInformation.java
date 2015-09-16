package cs555.tebbe.wireformats;

import java.io.*;

/**
 * Created by ct.
 */
public class ChunkReplicaInformation implements Event {

    private final String[] replicaChunkNodes;

    public ChunkReplicaInformation(String[] replicas) {
        this.replicaChunkNodes = replicas;
    }

    public static ChunkReplicaInformation parseChunkReplicaInformation(DataInputStream din) throws IOException {
        // replicas
        String[] replicas = new String[din.readInt()];
        for(int i=0; i < replicas.length; i++) {
            byte[] replicaBytes = new byte[din.readInt()];
            din.readFully(replicaBytes);
            replicas[i] = new String(replicaBytes);
        }
        return new ChunkReplicaInformation(replicas);
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

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

    public String[] getReplicaChunkNodes() {
        return replicaChunkNodes;
    }

    @Override
    public int getType() {
        return Protocol.NOTYPE;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for(String str : replicaChunkNodes)
            sb.append("\t" + str + "\n");
        return sb.toString();
    }
}
