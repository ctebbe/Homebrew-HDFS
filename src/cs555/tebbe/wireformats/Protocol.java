package cs555.tebbe.wireformats;
public class Protocol {


    public static final double CHUNK_SIZE_KB = 64.0;
    public static final int NUM_REPLICAS_PER_CHUNK = 3;

    // message types
    public static final int NOTYPE                  = -101;
    public static final int REGISTER                = 100;
    public static final int STORE_FILE_REQ          = 101;
    public static final int STORE_FILE_ROUTE        = 102;

    // status codes
    public static final byte NOSTATUS               = (byte) 0x00;
    public static final byte SUCCESS                = (byte) 0x01;
    public static final byte FAILURE                = (byte) 0x02;

    public static String getProtocolString(int protocol) {
        switch(protocol) {
            case NOTYPE:                return "NOTYPE";
            case REGISTER:              return "REGISTER";
            default:                    return "UNKNOWN";
        }
    }
}
