package cs555.tebbe.wireformats;
import cs555.tebbe.transport.*;
import cs555.tebbe.node.*;
import java.util.*;
import java.io.*;
import java.net.*;
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
    public static Event buildStoreFileRequestEvent(NodeConnection connection, int file_size) throws IOException {
        return new StoreFileRequest(Protocol.STORE_FILE_REQ, connection, file_size);
    }

    // STORE FILE ROUTE
    public static Event buildStoreFileRouteEvent(NodeConnection connection, ChunkRoute[] chunkRoutes) throws IOException {
        return new StoreFileRoute(Protocol.STORE_FILE_ROUTE, connection, chunkRoutes);
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
                case Protocol.STORE_FILE_ROUTE:
                    return new StoreFileRoute(marshalledBytes);
                default: return null;
            }
        } catch(IOException ioe) { 
            System.out.println(ioe.toString()); 
        }
        return null;
    }
}
