package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct.
 */
public class ChunkRoute implements Event {

    private Header header;
    private ChunkReplicaInformation[] chunks;

    public ChunkRoute(int protocol, NodeConnection connection, ChunkReplicaInformation[] chunks) {
        header = new Header(protocol, connection);
        this.chunks = chunks;
    }

    public ChunkRoute(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // chunk routes
        chunks = new ChunkReplicaInformation[din.readInt()];
        for(int i=0; i < chunks.length; i++) {
            chunks[i] = ChunkReplicaInformation.parseChunkRoute(din);
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

    @Override
    public int getType() {
        return this.header.getType();
    }
}
