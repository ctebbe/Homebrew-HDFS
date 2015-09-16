package cs555.tebbe.util;

import cs555.tebbe.wireformats.*;
import cs555.tebbe.transport.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class Util {

    // generates a uniq identifier based on the socket
    public static String generateHashKey(Socket sock) throws IOException {
        String address = sock.getRemoteSocketAddress().toString();
        System.out.println("hash key:"+address);
        return address.substring(address.indexOf("/")+1);
    }
    // generates id to put in event header
    public static String generateEventKey(Socket sock) throws IOException {
        return sock.getLocalAddress().toString().substring(1) + ":" + sock.getLocalPort(); // exclude leading /
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
    public static String[] stripFirstElement(String[] toStrip) {
        String[] toReturn = new String[toStrip.length-1];
        for(int i=1; i < toStrip.length; i++) {
            toReturn[i-1] = toStrip[i];
        }
        return toReturn;
    }
}
