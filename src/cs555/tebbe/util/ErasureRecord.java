package cs555.tebbe.util;

/**
 * Created by ctebbe on 9/22/15.
 */
public class ErasureRecord {

    public final String host;
    public final String filename;
    public final int chunk;
    public final int fragment;
    public final int numChunks;

    public ErasureRecord(String host, String filename, int chunk, int fragment, int chunks) {
        this.host = host;
        this.filename = filename;
        this.chunk = chunk;
        this.fragment = fragment;
        this.numChunks = chunks;
    }
}
