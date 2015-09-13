package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct
 */
public class StoreFileRequest implements Event {

    private Header header;
    private int size;

    public StoreFileRequest(int protocol, NodeConnection connection, int size) {
        header = new Header(protocol, connection);
        this.size = size;
    }

    public StoreFileRequest(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // file size
        this.size = din.readInt();

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        byte[] headerBytes = header.getBytes();
        dout.write(headerBytes);

        // file size
        dout.writeInt(size);

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    public int getType() {
        return this.header.getType();
    }

    public String getSenderKey() {
        return this.header.getSenderKey();
    }

    public int getFileSizeKB() {
        return this.size / 1024; // file size in bytes
    }
}
