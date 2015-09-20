package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct.
 */
public class ChunkRoute implements Event {

    private Header header;
    private String fileName;
    private int sequence;
    private ChunkReplicaInformation[] chunks;

    public ChunkRoute(int protocol, NodeConnection connection, String filename, ChunkReplicaInformation[] chunks) {
        header = new Header(protocol, connection);
        this.fileName = filename;
        this.chunks = chunks;
    }

    public ChunkRoute(int protocol, NodeConnection connection, String filename, int sequence, ChunkReplicaInformation[] chunks) {
        header = new Header(protocol, connection);
        this.fileName = filename;
        this.sequence = sequence;
        this.chunks = chunks;
    }

    public ChunkRoute(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);


        // chunk name
        int nameLen = din.readInt();
        byte[] receiverBytes = new byte[nameLen];
        din.readFully(receiverBytes);
        fileName = new String(receiverBytes);

        // sequence number
        sequence = din.readInt();

        // chunk routes
        chunks = new ChunkReplicaInformation[din.readInt()];
        for(int i=0; i < chunks.length; i++) {
            chunks[i] = ChunkReplicaInformation.parseChunkReplicaInformation(din);
        }

        bais.close();
        din.close();
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        byte[] headerBytes = header.getBytes();
        dout.write(headerBytes);

        // chunk name
        byte[] nameBytes = fileName.getBytes();
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

        // sequence number
        dout.writeInt(sequence);

        // chunk routes
        dout.writeInt(chunks.length);
        for(ChunkReplicaInformation route : chunks) {
            dout.write(route.getBytes());
        }

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    public ChunkReplicaInformation[] getChunksInformation() {
        return chunks;
    }

    public int getSequence() {
        return sequence;
    }

    @Override
    public int getType() {
        return this.header.getType();
    }

    public String getFileName() {
        return fileName;
    }
}
