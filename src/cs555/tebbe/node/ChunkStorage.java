package cs555.tebbe.node;

/**
 * Created by ct.
 */
public class ChunkStorage {

    private final String fileName;
    private final String version;
    private final int sequence;
    private final Long timestamp;
    private final String checksum;

    public ChunkStorage(String fileName, String version, int sequence, Long timestamp, String checksum) {
        this.fileName = fileName;
        this.version = version;
        this.sequence = sequence;
        this.timestamp = timestamp;
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getChunkStorageName() {
        return fileName + "_chunk" + sequence;
    }

    public String getFileName() {
        return fileName;
    }

    public String getVersion() {
        return version;
    }

    public int getSequence() {
        return sequence;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
