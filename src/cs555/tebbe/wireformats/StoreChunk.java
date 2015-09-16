package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct.
 */
public class StoreChunk implements Event {

    private Header header;

    private String name;
    private int chunk_sequence;
    private byte[] bytesToStore;
    private ChunkReplicaInformation replicaInformation;

    public StoreChunk(int protocol, NodeConnection connection, String name, int chunk_sequence, byte[] bytes, ChunkReplicaInformation replicaInformation) {
        header = new Header(protocol, connection);
        this.name = name;
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
        name = new String(nameBytes);

        // chunk sequence number
        chunk_sequence = din.readInt();

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
        return Protocol.STORE_CHUNK;
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
        for(String replica : replicaInformation.getReplicaChunkNodes()) {
        }
        return null;
    }

    public Header getHeader() {
        return header;
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
