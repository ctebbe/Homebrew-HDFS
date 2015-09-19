package cs555.tebbe.wireformats;
import cs555.tebbe.node.ChunkStorage;
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

    // STORE FILE(chunks) ROUTE
    public static Event buildChunkRouteEvent(NodeConnection connection, String fileName, ChunkReplicaInformation[] chunkReplicases) throws IOException {
        return new ChunkRoute(Protocol.CHUNK_ROUTE, connection, fileName, chunkReplicases);
    }

    // STORE CHUNK EVENT
    public static Event buildStoreChunkEvent(NodeConnection connection, String name, String version, int chunk_sequence, byte[] bytes, ChunkReplicaInformation replicaInformation) throws IOException {
        return new StoreChunk(Protocol.STORE_CHUNK, connection, name, version, chunk_sequence, bytes, replicaInformation);
    }

    // for chaining store chunk events
    public static Event buildStoreChunkEvent(NodeConnection connection, StoreChunk storeChunk) throws IOException {
        return new StoreChunk(Protocol.STORE_CHUNK, connection, storeChunk.getFileName(), storeChunk.getVersion(), storeChunk.getChunkSequenceID(), storeChunk.getBytesToStore(), storeChunk.getChunkReplicaInformation());
    }

    // MAJOR HEARTBEAT
    public static Event buildMajorHeartbeat(NodeConnection connection, ChunkStorage[] records) throws IOException {
        return new Heartbeat(Protocol.MAJOR_HEARTBEAT, connection, records);
    }

    // MINOR HEARTBEAT
    public static Event buildMinorHeartbeat(NodeConnection connection, ChunkStorage[] records) throws IOException {
        return new Heartbeat(Protocol.MINOR_HEARTBEAT, connection, records);
    }

    // REQUEST CHUNK
    public static Event buildRequestChunk(NodeConnection connection, String filename, int sequence) {
        return new RequestChunk(Protocol.CHUNK_REQ, connection, filename, sequence);
    }

    // REQUEST FILE
    public static Event buildRequestReadFile(NodeConnection connection, String filename) {
        return new RequestChunk(Protocol.READ_FILE_REQ, connection, filename, 0);
    }

    // RESPONSE READ FILE
    public static Event buildFileRouteEvent(NodeConnection connection, String fileName, ChunkReplicaInformation[] chunkReplicases) throws IOException {
        return new ChunkRoute(Protocol.READ_FILE_RESP, connection, fileName, chunkReplicases);
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
                case Protocol.STORE_CHUNK:
                    return new StoreChunk(marshalledBytes);
                case Protocol.MAJOR_HEARTBEAT:
                    return new Heartbeat(marshalledBytes);
                case Protocol.CHUNK_REQ:
                    return new RequestChunk(marshalledBytes);
                case Protocol.READ_FILE_REQ:
                    return new RequestChunk(marshalledBytes);
                case Protocol.READ_FILE_RESP:
                    return new ChunkRoute(marshalledBytes);
                default: return null;
            }
        } catch(IOException ioe) { 
            System.out.println(ioe.toString()); 
        }
        return null;
    }
}
