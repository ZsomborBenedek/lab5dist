package com.example.demo.service;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RestNameService {
    //
    HashMap<Integer, Integer> replicationDatabase = new HashMap<>();
    // Key is filenameHash, value is nodeName hash
    public HashMap<Integer, Integer> dataBase = new HashMap<>();
    public HashMap<Integer, String> nodes = new HashMap<>();
    Integer highest = 0;
    InetAddress inetAddress = InetAddress.getLocalHost();
    String name = inetAddress.getHostName();
    String thisIp =inetAddress.getHostAddress();

    public RestNameService() throws IOException {
        System.out.println("Ik run nu RestNameService constructor");
        clearDataBase();
        //readNodeMap();
        //generateReplicationBase();
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
        System.out.println("Ik run nu addNodeToMap, Variebelen name "+name+" ip "+ip);
        /*
        BufferedWriter writer = new BufferedWriter(
                new FileWriter("//home//pi//DSLab5//src//main//java//com//example//demo/backLogic//NodeMap.txt", true)  //Set true for append mode
                //new FileWriter("C:\\Users\\Arla\\Desktop\\School\\lab5distStef\\src\\main\\java\\com\\example\\NodeMap.txt", true)  //Set true for append mode
        );
        writer.newLine();   //Add new line
        writer.write(name);
        writer.newLine();
        writer.write(ip);
        writer.close();
        readNodeMap();
        generateReplicationBase();

         */

        System.out.println(name+" "+ip+" "+"Toegevoegd aan nodemap");
        nodes.put(hashfunction(name,true),ip);
        if (hashfunction(name,true) > highest) {
            highest = hashfunction(name, true);
            System.out.println(name+" is nu de hoogst gehashte node");
        }
    }
    public int requestFile(String filename){
        Integer hash = hashfunction(filename, false);
        if(replicationDatabase.get(hash)!=null)
            return replicationDatabase.get(hash);
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
        File file2 = new File("/home/pi/lab5dist/src/main/java/com/example/DataBase.txt");
        BufferedReader br2 = new BufferedReader(new FileReader(file2));
        String st2;
        //replicatioDataBase.clear();
        while ((st2 = br2.readLine()) != null) {
            System.out.println("Dees is st2 " + st2);
            String[] temporary = st2.split("::");
            System.out.println("Dees is den array tostring" + Arrays.toString(temporary));
            String fileName = temporary[0];
            String nodeName = temporary[1];
            Integer tempfile = hashfunction(fileName, false);
            Integer temp = tempfile - 1;
            if (nodes.size() > 1) {
                while ((nodes.get(temp) == null || nodes.get(temp).equals(hashfunction(nodeName, true))) && temp != 0) {
                    temp--;
                }
                //EERST LISTNER DAN RECEIVE
                if (temp == 0) {
                    if (replicationDatabase.get(tempfile) == null) {
                        //Hier in database knalle da er een verandering is
                        if (!highest.equals(hashfunction(nodeName, true))) {
                            System.out.println("nieuwe file, temp = 0");
                            replicationDatabase.put(tempfile, highest);
                        }
                    } else
                        System.out.println("ouwe file niks toegevoegd temp=0");
                } else {
                    if (replicationDatabase.get(tempfile) == null) {
                        //Hier in database knalle da er een verandering is
                        System.out.println("nieuwe file, temp is nie 0");
                        replicationDatabase.put(tempfile, temp);
                    } else if (temp < replicationDatabase.get(tempfile)) {
                        //Er is een nieuwe betere gevonden
                        //Laat de nodus dus weten dat ze door moeten sturen
                        replicationDatabase.replace(tempfile, replicationDatabase.get(tempfile), temp);
                    } else
                        System.out.println("ouwe file niks toegevoegd, temp is nie 0");
                }

            /*
            DEZE CODE AANGEPAST NAAR BOVENSTAANDE CODE, KDENK DA DIE FOUT WAS
            if (temp == 0)
                replicatioDataBase.put(tempfile,highest);
            replicatioDataBase.put(tempfile,highest);
             */
            }
            else
                System.out.println("nog geen replication want der is maar één node");
            System.out.println(replicationDatabase.toString());
        }
    }
    public int addFileToDataBase(String name, String fileName) throws IOException {
        System.out.println("Ik run nu addFileToDataBase, Variebelen name "+name+" filename "+fileName);
        int nameHash = hashfunction(name,true);
        int fileHash = hashfunction(fileName,false);
        if(nodes.get(nameHash)!=null) {
            if (dataBase.get(fileHash) == null) {
                dataBase.put(fileHash, nameHash);
                System.out.println("na komt de schrijver van database");
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter("/home/pi/lab5dist/src/main/java/com/example/DataBase.txt", true)  //Set true for append mode
                        //new FileWriter("C:\\Users\\Arla\\Desktop\\School\\lab5distStef\\src\\main\\java\\com\\example\\NodeMap.txt", true)  //Set true for append mode
                );
                writer.write(fileName + "::" + name);
                writer.newLine();   //Add new line
                writer.close();
                generateReplicationBase();
                return 1;
            }
            return -1;
        }
        else
            return -1;
    }
    private void clearDataBase() throws IOException {
        File database = new File("/home/pi/lab5dist/src/main/java/com/example/DataBase.txt");
        if (database.exists() && database.isFile())
        {
            database.delete();
        }
        database.createNewFile();
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
