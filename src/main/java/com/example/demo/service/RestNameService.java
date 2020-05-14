package com.example.demo.service;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RestNameService {
    //
    HashMap<Integer, Integer> replicatioDataBase = new HashMap<>();
    // Key is filenameHash, value is nodeName hash
    HashMap<Integer, Integer> dataBase = new HashMap<>();
    public HashMap<Integer, String> nodes = new HashMap<>();
    Integer highest = 0;
    InetAddress inetAddress = InetAddress.getLocalHost();
    String name = inetAddress.getHostName();
    String thisIp =inetAddress.getHostAddress();

    public RestNameService() throws IOException {
        readNodeMap();
        generateReplicationBase();
    }

    private int hashfunction(String name, boolean node) {
        int hash=0;
        int temp = 0;
        int i;
        for (i = 0; i<name.length();i++) {
            hash = 3 * hash + name.charAt(i);
            temp = temp+ name.charAt(i);
        }
        hash = hash+temp;
        if (node) {
            System.out.println("node");
        }
        else
            hash = hash/53;
        return hash;
    }
    public void addNodeToMap(String name, String ip) throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new FileWriter("//home//pi//DSLab5//src//main//java//com//example//demo/backLogic//NodeMap.txt", true)  //Set true for append mode
        );
        writer.newLine();   //Add new line
        writer.write(name);
        writer.newLine();
        writer.write(ip);
        writer.close();
        readNodeMap();
        generateReplicationBase();
    }
    public int requestFile(String filename){
        Integer hash = hashfunction(filename, false);
        if(replicatioDataBase.get(hash)!=null)
            return replicatioDataBase.get(hash);
        else
            return -1;
    }
    public void removeNodeFromMap(Integer node) throws IOException {
        nodes.clear();
        File file = new File("//home//pi//DSLab5//src//main//java//com//example//demo/backLogic//NodeMap.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        nodes.clear();
        ArrayList<String> nameToAdd = new ArrayList<>();
        ArrayList<String> ipToAdd = new ArrayList<>();
        while ((st = br.readLine()) != null){
            String ip = br.readLine();
            int hash = hashfunction(st,true);
            if (hash!= node) {
                nodes.put(hash, ip);
                nameToAdd.add(st);
                ipToAdd.add(ip);
            }else
                System.out.println("removed "+st);
        }
        int i = 0;
        BufferedWriter writer = new BufferedWriter(
                new FileWriter("//home//pi//DSLab5//src//main//java//com//example//demo/backLogic//NodeMap.txt", false)  //Set true for append mode
        );
        while (i<nameToAdd.size()){
            if (i>=1)
                writer.newLine();
            writer.write(nameToAdd.get(i));
            writer.newLine();
            writer.write(ipToAdd.get(i));
            i++;
        }
        writer.close();
        highest = 0;
        readNodeMap();
        generateReplicationBase();
    }
    //Dees ga read replicationBase moete worre
    public void generateReplicationBase() throws IOException {
        File file2 = new File("//home//pi//DSLab5//src//main//java//com//example//demo/backLogic//Database2.txt");
        BufferedReader br2 = new BufferedReader(new FileReader(file2));
        String st2;
        replicatioDataBase.clear();
        while ((st2 = br2.readLine()) != null){
            Integer tempfile = hashfunction(st2,false);
            Integer temp = tempfile-1;
            while (nodes.get(temp)==null && temp != 0){
                temp--;
            }
            if (temp == 0)
                replicatioDataBase.put(tempfile,highest);
            else
                replicatioDataBase.put(tempfile,temp);

            /*
            DEZE CODE AANGEPAST NAAR BOVENSTAANDE CODE, KDENK DA DIE FOUT WAS
            if (temp == 0)
                replicatioDataBase.put(tempfile,highest);
            replicatioDataBase.put(tempfile,highest);
             */
        }
        System.out.println(replicatioDataBase.toString());
    }
    public int addFileToDataBase(String name, String fileName){
        int nameHash = hashfunction(name,true);
        int fileHash = hashfunction(fileName,false);
        if(nodes.get(nameHash)!=null) {
            dataBase.put(fileHash, nameHash);
            return 1;
        }
        else
            return -1;
    }
    public void readNodeMap() throws IOException {
        File file = new File("//home//pi//DSLab5//src//main//java//com//example//demo/backLogic///NodeMap.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        nodes.clear();
        while ((st = br.readLine()) != null){
            if(!st.isEmpty()) {
                String ip = br.readLine();
                int hash = hashfunction(st, true);
                System.out.println("node " + st + " heeft hashwaarde " + hash);
                nodes.put(hash, ip);
                if (hash > highest)
                    highest = hash;
            }
        }
    }

}
