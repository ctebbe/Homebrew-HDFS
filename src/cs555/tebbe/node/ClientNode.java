package cs555.tebbe.node;

import cs555.tebbe.transport.ConnectionFactory;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.wireformats.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by ct.
 */
public class ClientNode implements Node {

    private NodeConnection _Controller;
    private byte[] cachedBytes;

    public ClientNode(String chost, int cport) {
        /*try {
            _Controller = ConnectionFactory.getInstance().buildConnection(this, new Socket(chost, cport));
            _Controller.sendEvent(EventFactory.buildRegisterEvent(_Controller));
        } catch(IOException ioe) {
            System.out.println("IOException thrown contacting ControllerNode:"+ioe.getMessage());
            System.exit(0);
        }*/
    }

    public void run() throws IOException {
        printMenu();
        Scanner keyboard = new Scanner(System.in);
        String input = keyboard.nextLine();
        while(input != null) {
            if(input.contains("1")) {
                System.out.println("File name?");
                String fname = keyboard.nextLine();
                System.out.println("Size in KB?");
                optGenerateAndStoreRandomFile(fname, Integer.parseInt(keyboard.nextLine()));
            }
            input = keyboard.nextLine();
        }
    }

    private void optGenerateAndStoreRandomFile(String fname, int sizeKB) throws IOException {
        cachedBytes = new byte[sizeKB * 1024];
        new Random().nextBytes(cachedBytes);
        _Controller.sendEvent(EventFactory.buildStoreFileRequestEvent(_Controller, fname, cachedBytes.length));
    }

    private void printMenu() {
        System.out.println("1. Generate & store random file");
    }

    @Override
    public void onEvent(Event event) {
        switch(event.getType()) {
            case Protocol.CHUNK_ROUTE: // route the cached bytes to
                processChunkRoute((ChunkRoute) event);
                break;
        }
    }

    private void processChunkRoute(ChunkRoute event) {
        for(ChunkReplicaInformation info : event.getChunksInformation()) {
            System.out.println(info.getChunkName());
            for(String s : info.getReplicaChunkNodes())
                System.out.println("\t" + s);
        }
    }

    @Override
    public void registerConnection(NodeConnection connection) {

    }

    public static void main(String[] args) {
        try {
            new ClientNode("localhost", 8080).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
