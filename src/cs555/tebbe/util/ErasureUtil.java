package cs555.tebbe.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * Created by ctebbe on 9/20/15.
 */
public class ErasureUtil {

    public static final int DATA_SHARDS = 4;    // 6
    public static final int PARITY_SHARDS = 2;  // 3
    public static final int TOTAL_SHARDS = DATA_SHARDS+PARITY_SHARDS;

    public static final int BYTES_IN_INT = 4;

    public static byte[] encodeReedSolomon(byte[] toEncode) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        int sizePayload = toEncode.length;
        int sizeStored = BYTES_IN_INT + sizePayload; // total stored data length
        int sizeShard = (sizeStored + DATA_SHARDS - 1) / DATA_SHARDS;

        // create a new buffer to hold stored data
        int sizeBuffer = sizeShard * DATA_SHARDS;
        byte[] allBytes = new byte[sizeBuffer];
        return null;
    }
}
