package cs555.tebbe.wireformats;

import java.io.IOException;

/**
 * Created by ct.
 */
public class StoreChunk implements Event {

    private ChunkReplicaInformation replicaInformation;
    private String version;
    private int id;
    private byte[] bytesToStore;

    @Override
    public int getType() {
        return Protocol.STORE_CHUNK;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return new byte[0];
    }

    public String getVersion() {
        return version;
    }

    public int getChunkID() {
        return id;
    }

    public ChunkReplicaInformation getChunkReplicaInformation() {
        return replicaInformation;
    }

    public byte[] getBytesToStore() {
        return bytesToStore;
    }
}
