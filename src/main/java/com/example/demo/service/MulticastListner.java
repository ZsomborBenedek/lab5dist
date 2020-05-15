package com.example.demo.service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.io.IOException;
import java.util.ArrayList;

public class MulticastListner implements Runnable {

    RestNameService nameService;

    public MulticastListner(RestNameService temp){
        nameService = temp;
    }

    private ArrayList<String> getNameAndIp(String msg) throws IOException {
        System.out.println("Ik run nu /getNameAndIP");
        ArrayList<String> temp = new ArrayList<>();
        if (msg.contains("newNode")) {
            String haha = msg.replace("newNode ","");
            if (!haha.isEmpty()) {
                String[] tokens = haha.split("::");
                for (String t : tokens)
                    temp.add(t);
            }
            //addNodeToMap(temp.get(0),temp.get(1));
            //nodes.put(hashfunction(temp.get(0),true),temp.get(1));
            System.out.println(temp.toString());
            System.out.println("Node added");
            System.out.println("NodeCount is " +nameService.nodes.size());
            sendUDPMessage("nodeCount "+nameService.nodes.size(),"230.0.0.0",10000);
            sendUDPMessage("NameServer "+nameService.name+"::"+nameService.thisIp,temp.get(1),10000);
            URL connection2 = new URL("http://"+temp.get(1)+":9000/SetNameServer?ip="+nameService.thisIp);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection2. openStream()));
            String message = in.readLine();
            System.out.println(message);

        }
        if (msg.contains("remNode")) {
            String haha = msg.replace("remNode ","");
            if (!haha.isEmpty()) {
                String[] tokens = haha.split("::");
                for (String t : tokens)
                    temp.add(t);
            }
            // Hier wordt terug gegeve de hoeveelste node hij was dus ist mogenlijk de hash te fixe
            //removeNodeFromMap(hashfunction(temp.get(0),true));
            System.out.println(temp.toString());
            System.out.println("Node removed");
        }
        return temp;
    }
    public void receiveUDPMessage(int port) throws
            IOException {
        byte[] buffer = new byte[1024];
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket.joinGroup(group);
        while (true) {
            System.out.println("Waiting for multicast message...");
            DatagramPacket packet = new DatagramPacket(buffer,
                    buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(),
                    packet.getOffset(), packet.getLength());
            System.out.println("Deze multicat krijgk binne: "+msg);
                getNameAndIp(msg);
            if ("OK".equals(msg)) {
                System.out.println("No more message. Exiting : " + msg);
                break;
            }
        }
        socket.leaveGroup(group);
        socket.close();
    }
    public static void sendUDPMessage(String message,
                                      String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(ipAddress);
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length,
                group, port);
        socket.send(packet);
        socket.close();
    }

    @Override
    public void run() {
        try {
            receiveUDPMessage( 10000);
            //receiveUDPMessage(eigenIP, 4321);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
