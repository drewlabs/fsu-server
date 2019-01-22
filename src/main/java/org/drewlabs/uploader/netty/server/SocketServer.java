package org.drewlabs.uploader.netty.server;

import org.drewlabs.uploader.netty.cache.FileCache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Arrays;

public class SocketServer {
    public static ServerSocket server;
    public static int port = 9997;
    private static final String IP_ADDRESS = "localhost";
    private static final String META_DATA_ACKNOWLEDGE = "META_DATA_RECEIVING";
    private static final String RECONNECT = "RECONNECT";
    public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException {
        server = new ServerSocket(9997, 1, InetAddress.getByName(IP_ADDRESS));
        //keep listens indefinitely until receives 'exit' call or program terminates
        while(true){
            InetSocketAddress socketAddress = (InetSocketAddress) server.getLocalSocketAddress();
            System.out.println("Listening on " + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort());
            System.out.println("Waiting for client request");
            //creating socket and waiting for client connection
            Socket socket = server.accept();
            //read from socket to ObjectInputStream object
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            //convert ObjectInputStream object to String
            String message = (String) ois.readObject();
            System.out.println("Message Received: " + message);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            //create ObjectOutputStream object
            if(message.contains(META_DATA_ACKNOWLEDGE)){
                int sizeLength = Character.getNumericValue(message.charAt(META_DATA_ACKNOWLEDGE.length()));
                System.out.println("Image byte length is " + sizeLength);
                int imageSize = Integer.parseInt(message.substring(META_DATA_ACKNOWLEDGE.length()+1, META_DATA_ACKNOWLEDGE.length()+sizeLength+1));
                System.out.println("Image byte size is " + imageSize);
                String ipIdentifier = message.substring(META_DATA_ACKNOWLEDGE.length()+1+sizeLength+2, META_DATA_ACKNOWLEDGE.length()+1+sizeLength+2+Integer.parseInt(message.substring(META_DATA_ACKNOWLEDGE.length()+1+sizeLength, META_DATA_ACKNOWLEDGE.length()+1+sizeLength+2)));
                System.out.println("IP identifier is " + ipIdentifier);
                String fileName = message.substring(META_DATA_ACKNOWLEDGE.length()+1+sizeLength+2+Integer.parseInt(message.substring(META_DATA_ACKNOWLEDGE.length()+1+sizeLength, META_DATA_ACKNOWLEDGE.length()+1+sizeLength+2)), message.length());
                System.out.println("File name is " + fileName);
                FileCache.fileBytesMap.put(ipIdentifier, new byte[]{});
                FileCache.fileNameMap.put(ipIdentifier, fileName);
                FileCache.fileSize.put(ipIdentifier, imageSize);
                System.out.println("Map value on server socket is " + FileCache.fileBytesMap);
                // FIXME :: check if a process with same configurations is already there
                oos.writeObject("OK");
                oos.flush();
            }else if(message.contains(RECONNECT)){
                System.out.println("Changing process id, assigned for the previous connection");
                message = message.replace(RECONNECT, "");
                String [] pids = message.split("\\.\\.\\.\\.", 2);
                System.out.println("Two process ids are :: " + Arrays.toString(pids));
                if(FileCache.fileBytesMap.get(pids[0]) != null){
                    byte[] alreadyReceivedBytes = FileCache.fileBytesMap.remove(pids[0]);
                    FileCache.fileBytesMap.put(pids[1], alreadyReceivedBytes);
                    FileCache.fileNameMap.put(pids[1], FileCache.fileNameMap.remove(pids[0]));
                    FileCache.fileSize.put(pids[1], FileCache.fileSize.remove(pids[0]));
                    oos.writeObject(String.valueOf(alreadyReceivedBytes.length));
                    oos.flush();
                }else {
                    oos.writeObject(String.valueOf(0)); // no file was there, start again
                    oos.flush();
                }
            }else{
                oos.writeObject("PONG");
                oos.flush();
            }
            //close resources
            ois.close();
            oos.close();
            socket.close();
            // session ends, terminate this socket connection
        }
    }
}
