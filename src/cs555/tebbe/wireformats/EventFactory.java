package cs555.tebbe.wireformats;
import cs555.tebbe.transport.*;

import java.io.*;

public class EventFactory {

    protected EventFactory() {}
    private static EventFactory factory = null;
    public static EventFactory getInstance() {
        if(factory == null) factory = new EventFactory();
        return factory;
    }

    // REGISTER
    public static Event buildRegisterEvent(NodeConnection connection) throws IOException {
        return new Register(Protocol.REGISTER, connection);
    }

    // STORE FILE REQ
    public static Event buildStoreFileRequestEvent(NodeConnection connection, String file_name, int file_size) throws IOException {
        return new StoreFileRequest(Protocol.STORE_FILE_REQ, connection, file_name, file_size);
    }

    // STORE FILE ROUTE
    public static Event buildChunkRouteEvent(NodeConnection connection, ChunkReplicaInformation[] chunkReplicases) throws IOException {
        return new ChunkRoute(Protocol.CHUNK_ROUTE, connection, chunkReplicases);
    }

    public static Event buildEvent(byte[] marshalledBytes) throws IOException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
            DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

            switch(din.readInt()) { // read protocol type byte
                case Protocol.REGISTER:
                    return new Register(marshalledBytes);
                case Protocol.STORE_FILE_REQ:
                    return new StoreFileRequest(marshalledBytes);
                case Protocol.CHUNK_ROUTE:
                    return new ChunkRoute(marshalledBytes);
                default: return null;
            }
        } catch(IOException ioe) { 
            System.out.println(ioe.toString()); 
        }
        return null;
    }
}
