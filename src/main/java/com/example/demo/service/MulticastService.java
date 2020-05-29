package com.example.demo.service;

import java.net.*;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class MulticastService implements Runnable {

    RestNameService nameService;

    public MulticastService(RestNameService temp) {
        nameService = temp;
    }

    private ArrayList<String> getNameAndIp(String msg) throws IOException, InterruptedException {
        // System.out.println("Ik run nu /getNameAndIP");
        ArrayList<String> temp = new ArrayList<>();
        if (msg.contains("newNode")) {
            String haha = msg.replace("newNode ", "");
            if (!haha.isEmpty()) {
                String[] tokens = haha.split("::");
                for (String t : tokens)
                    temp.add(t);
            }

            if (nameService.hashfunction(temp.get(0), true) > nameService.highest) {
                nameService.highest = nameService.hashfunction(temp.get(0), true);
                System.out.println(temp.get(0) + " is now the highest hashed node");
            }
            if (nameService.hashfunction(temp.get(0), true) < nameService.lowest) {
                nameService.lowest = nameService.hashfunction(temp.get(0), true);
                System.out.println(temp.get(0) + " is now the lowest hashed node");
            }
            Thread.sleep(500);

            RestTemplate restTemplate = new RestTemplate();
            String url = "http://" + temp.get(1) + ":9000/SetNameServer";
            ResponseEntity<String> response = restTemplate.postForEntity(url,
                    new HttpEntity<String>(nameService.thisIp), String.class);
            System.out.println("\n" + response.toString() + "\n");

            sendUDPMessage("nodeCount " + nameService.nodes.size(), "230.0.0.0", 10000);
        }
        if (msg.contains("remNode")) {
            String haha = msg.replace("remNode ", "");
            if (!haha.isEmpty()) {
                String[] tokens = haha.split("::");
                for (String t : tokens)
                    temp.add(t);
            }
        }
        return temp;
    }

    public void receiveUDPMessage(int port) throws IOException, InterruptedException {
        byte[] buffer = new byte[1024];
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket.joinGroup(group);
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            System.out.println("Ik receive multicast " + msg);
            if (msg.contains("newNode"))
                getNameAndIp(msg);
            if ("OK".equals(msg)) {
                // System.out.println("No more message. Exiting : " + msg);
                break;
            }
        }
        socket.leaveGroup(group);
        socket.close();
    }

    public static void sendUDPMessage(String message, String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(ipAddress);
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
        socket.send(packet);
        socket.close();
    }

    @Override
    public void run() {
        try {
            receiveUDPMessage(10000);
            // receiveUDPMessage(eigenIP, 4321);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
