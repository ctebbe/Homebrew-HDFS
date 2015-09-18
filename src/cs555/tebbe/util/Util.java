package cs555.tebbe.util;

import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Util {

    public static Long getCheckSum(byte[] bytes) {
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        return new Long(checksum.getValue());
    }

    // strips away the IP in the key format
    public static String removePort(String key) {
        return key.substring(0, key.indexOf(":"));
    }
    public static int removeIPAddress(String key) {
        return Integer.parseInt(key.substring(key.indexOf(":")+1));
    }

    public static int generateRandomNumber(int min, int max) {
        return (int)(Math.random() * ((max-min) + 1) + min);
    }

    public static int generateRandomNumber() {
        return new Random().nextInt();
    }
}
