package cs555.tebbe.wireformats;

import cs555.tebbe.node.ChunkStorage;
import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct.
 */
public class Heartbeat implements Event {

    private Header header;
    private ChunkStorage[] storageRecords;

    public Heartbeat(int protocol, NodeConnection connection, ChunkStorage[] records) {
        header = new Header(protocol, connection);
        storageRecords = records;
    }

    public Heartbeat(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // chunk storage records
        int numRecords = din.readInt();
        storageRecords = new ChunkStorage[numRecords];
        for(int i=0; i < storageRecords.length; i++) {
            // file name
            int nameLen = din.readInt();
            byte[] receiverBytes = new byte[nameLen];
            din.readFully(receiverBytes);
            String fileName = new String(receiverBytes);

            // version number
            int versionLen = din.readInt();
            byte[] versionBytes = new byte[versionLen];
            din.readFully(versionBytes);
            String version = new String(versionBytes);

            // chunk sequence
            int chunkSequence = din.readInt();

            // time stamp
            Long timestamp = din.readLong();

            storageRecords[i] = new ChunkStorage(fileName, version, chunkSequence, 0, timestamp, null);
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

        // chunk storage records
        dout.writeInt(storageRecords.length);
        for(ChunkStorage record : storageRecords) {
            // file name
            byte[] nameBytes = record.getFileName().getBytes();
            dout.writeInt(nameBytes.length);
            dout.write(nameBytes);

            // version number
            byte[] versionBytes = record.getVersion().getBytes();
            dout.writeInt(versionBytes.length);
            dout.write(versionBytes);

            // chunk sequence
            dout.writeInt(record.getSequence());

            // timestamp
            dout.writeLong(record.getTimestamp());
        }

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    public ChunkStorage[] getRecords() {
        return storageRecords;
    }

    public Header getHeader() {
        return header;
    }

    @Override public int getType() {
        return header.getType();
    }

}
