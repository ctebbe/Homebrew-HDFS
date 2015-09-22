package cs555.tebbe.util;

import erasure.ReedSolomon;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by ctebbe
 */
public class ErasureUtil {

    public static final int BYTES_IN_INT    = 4;

    public static final int DATA_SHARDS     = 4;  // 6
    public static final int PARITY_SHARDS   = 2;  // 3
    public static final int TOTAL_SHARDS    = DATA_SHARDS+PARITY_SHARDS;

    public static byte[][] encodeReedSolomon(byte[] toEncode) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));
        int sizePayload = toEncode.length;
        int sizeStored = BYTES_IN_INT + sizePayload;                    // total stored data payload_size + payload
        int sizeShard = (sizeStored + DATA_SHARDS - 1) / DATA_SHARDS;   // round shard size up
        int sizeBuffer = sizeShard * DATA_SHARDS;                       // payload_size + payload + padding

        System.out.println("payload size:"+sizePayload);
        System.out.println("stored size:"+sizeStored);
        System.out.println("shard size:" + sizeShard);
        System.out.println("total buffer size:" + sizeBuffer);

        // create a new buffer to hold stored data
        dout.write(sizePayload);                                        // payload_size
        dout.write(toEncode, 0, sizePayload);                           // payload
        dout.write(new byte[sizeBuffer-dout.size()+1], 0, sizeBuffer-dout.size()+1);              // padding
        byte[] allBytes = baos.toByteArray();
        System.out.println("allBytes array size:"+allBytes.length);

        // store shards
        byte[][] shards = new byte[TOTAL_SHARDS][sizeShard];            // buffers to hold the shards
        for(int i=0; i < DATA_SHARDS; i++) {                            // fill in shards
            System.out.println("index:"+i*sizeShard);
            System.arraycopy(allBytes, i*sizeShard, shards[i], 0, sizeShard);
        }

        ReedSolomon reedSolomon = new ReedSolomon(DATA_SHARDS, PARITY_SHARDS);
        reedSolomon.encodeParity(shards, 0, sizeShard);                 // parity codes stored in last 2 positions
        return shards;
    }

    public static byte[][] decodeReedSolomon(byte[][] shards) {
        int sizeShard=0; 
        boolean[] presentShards = new boolean[TOTAL_SHARDS];
        for(int i=0; i < TOTAL_SHARDS; i++) {                           // find shard size and mark available shards
            if(shards[i] != null) {
                sizeShard = shards[i].length;
                presentShards[i] = true;
            }
        }

        for(int i=0; i < TOTAL_SHARDS; i++) {                           // buffer missing shards
            if(!presentShards[i]) {
                shards[i] = new byte[sizeShard]; 
            }
        }

        ReedSolomon reedSolomon = new ReedSolomon(DATA_SHARDS, PARITY_SHARDS);
        reedSolomon.decodeMissing(shards, presentShards, 0, sizeShard);                 // parity codes stored in last 2 positions
        return shards;
    }

    public static void main(String[] args) throws IOException {
        byte[] randBytes = new byte[128 * 1024];
        new Random().nextBytes(randBytes);
        System.out.println(Util.getCheckSumSHA1(randBytes));
        encodeReedSolomon(randBytes);
    }
}
