package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct
 */
public class StoreFileRequest implements Event {

    private Header header;
    private String fName;
    private int size;

    public StoreFileRequest(int protocol, NodeConnection connection, String filename, int size) {
        header = new Header(protocol, connection);
        this.fName = filename;
        this.size = size;
    }

    public StoreFileRequest(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // file name
        int nameLen = din.readInt();
        byte[] nameBytes = new byte[nameLen];
        din.readFully(nameBytes);
        this.fName = new String(nameBytes);

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

        // file name
        byte[] nameBytes = fName.getBytes();
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

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

    public String getFileName() {
        return this.fName;
    }

    public int getFileSizeKB() {
        return this.size / 1024; // file size in bytes
    }
}
