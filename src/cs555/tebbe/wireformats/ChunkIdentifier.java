package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct.
 */
public class ChunkIdentifier implements Event {

    private Header header;
    private String filename;
    private int sequence;
    private int fragment;

    public ChunkIdentifier(int protocol, NodeConnection connection, String name, int sequence) {
        header = new Header(protocol, connection);
        this.filename = name;
        this.sequence = sequence;
        this.fragment = 999;
    }

    public ChunkIdentifier(int protocol, NodeConnection connection, String name, int sequence, int fragment) {
        header = new Header(protocol, connection);
        this.filename = name;
        this.sequence = sequence;
        this.fragment = fragment;
    }

    public ChunkIdentifier(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // file name
        byte[] nameBytes = new byte[din.readInt()];
        din.readFully(nameBytes);
        this.filename = new String(nameBytes);

        // chunk sequence number
        this.sequence = din.readInt();

        // fragment
        this.fragment = din.readInt();

        bais.close();
        din.close();
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        dout.write(header.getBytes());

        // file name
        byte[] nameBytes = filename.getBytes();
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

        // chunk sequence number
        dout.writeInt(sequence);

        // fragment
        dout.writeInt(fragment);

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    public String getFilename() {
        return filename;
    }

    public Header getHeader() {
        return header;
    }

    public int getSequence() {
        return sequence;
    }

    public int getFragment() {
        return fragment;
    }

    public String getChunkStorageName() {
        return filename + "_chunk" + sequence;
    }

    public String getErasureStorageName() {
        return getChunkStorageName() + "_erasure" + fragment;
    }

    @Override public int getType() {
        return header.getType();
    }
}
