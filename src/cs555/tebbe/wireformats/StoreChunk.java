package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.util.Util;

import java.io.*;

/**
 * Created by ct.
 */
public class StoreChunk implements Event {

    private Header header;

    private String name;
    private String version;
    private int chunk_sequence;
    private byte[] bytesToStore;
    private ChunkReplicaInformation replicaInformation;

    public StoreChunk(int protocol, NodeConnection connection, String name, String version, int chunk_sequence,
                      byte[] bytes, ChunkReplicaInformation replicaInformation) {
        header = new Header(protocol, connection);
        this.name = name;
        this.version = version;
        this.chunk_sequence = chunk_sequence;
        this.bytesToStore = bytes;
        this.replicaInformation = replicaInformation;
    }

    public StoreChunk(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // file name
        byte[] nameBytes = new byte[din.readInt()];
        din.readFully(nameBytes);
        this.name = new String(nameBytes);

        //version
        byte[] versionBytes = new byte[din.readInt()];
        din.readFully(versionBytes);
        this.version = new String(versionBytes);

        // chunk sequence number
        this.chunk_sequence = din.readInt();

        // bytes to store
        bytesToStore = new byte[din.readInt()];
        din.readFully(bytesToStore);

        // replica routing info
        replicaInformation = ChunkReplicaInformation.parseChunkReplicaInformation(din);

        bais.close();
        din.close();
    }

    @Override
    public int getType() {
        return header.getType();
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        dout.write(header.getBytes());

        // file name
        byte[] nameBytes = name.getBytes();
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

        // version
        byte[] versionBytes = version.getBytes();
        dout.writeInt(versionBytes.length);
        dout.write(versionBytes);

        // chunk sequence number
        dout.writeInt(chunk_sequence);

        // bytes to store
        dout.writeInt(bytesToStore.length);
        dout.write(bytesToStore);

        // replica routing info
        dout.write(replicaInformation.getBytes());

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    public String getNextHost() {
        boolean replicaFound = false;
        for(String replica : replicaInformation.getReplicaChunkNodes()) {
            if(replicaFound)
                return replica;
            replicaFound = replica.equals(Util.removePort(header.getReceiverKey()));
        }
        return null;
    }

    public Header getHeader() {
        return header;
    }

    public String getVersion() {
        return version;
    }

    public String getFileName() {
        return name;
    }

    public int getChunkSequenceID() {
        return chunk_sequence;
    }

    public ChunkReplicaInformation getChunkReplicaInformation() {
        return replicaInformation;
    }

    public byte[] getBytesToStore() {
        return bytesToStore;
    }
}
