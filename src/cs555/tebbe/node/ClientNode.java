package cs555.tebbe.node;

import cs555.tebbe.transport.ConnectionFactory;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.wireformats.Event;
import cs555.tebbe.wireformats.EventFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by ct.
 */
public class ClientNode implements Node {

    private NodeConnection _Controller;

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
                System.out.println("Size in KB?");
                optGenerateAndStoreRandomFile(Integer.parseInt(keyboard.nextLine()));
            }
            input = keyboard.nextLine();
        }
    }

    private void optGenerateAndStoreRandomFile(int sizeKB) throws IOException {
        byte[] randomBytes = new byte[sizeKB * 1024];
        new Random().nextBytes(randomBytes);
        _Controller.sendEvent(EventFactory.buildStoreFileRequestEvent(_Controller, randomBytes.length));
    }

    private void printMenu() {
        System.out.println("1. Generate & store random file");
    }

    @Override
    public void onEvent(Event event) {

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
