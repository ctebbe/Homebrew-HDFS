package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct.
 */
public class StoreFileRoute implements Event {

    private Header header;
    private ChunkRoute[] chunkRoutes;

    public StoreFileRoute(int protocol, NodeConnection connection, ChunkRoute[] chunkRoutes) {
        header = new Header(protocol, connection);
        this.chunkRoutes = chunkRoutes;
    }

    public StoreFileRoute(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // chunk routes
        chunkRoutes = new ChunkRoute[din.readInt()];
        for(int i=0; i < chunkRoutes.length; i++) {
            chunkRoutes[i] = ChunkRoute.parseChunkRoute(din);
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
        dout.writeInt(chunkRoutes.length);
        for(ChunkRoute route : chunkRoutes) {
            dout.write(route.getBytes());
        }

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    @Override
    public int getType() {
        return this.header.getType();
    }
}
