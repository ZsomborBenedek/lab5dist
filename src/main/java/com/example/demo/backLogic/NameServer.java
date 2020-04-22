package com.example.demo.backLogic;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;

@RestController
public class NameServer {
    Map<Integer, Integer> dataBase = new HashMap();
    Map<Integer, String> nodes = new HashMap<>();
    Integer highest = 0;
    public NameServer() throws IOException {
        readNodeMap();
        readDatabase();
    }
    private int hashfunction(String name, boolean node) {
        int hash=0;
        int temp = 0;
        int i;
        for (i = 0; i<name.length();i++) {
            hash = 3 * hash + name.charAt(i);
            temp = temp+ name.charAt(i);
        }
        hash = hash/(temp/7);

        if (node) {
            for (i = 1; i <= nodes.size() + 1; i++)
                hash = (hash) / (i);
        }
        else
            hash = hash/53;
        return hash;
    }
    private void addNodeToMap(String name, String ip) throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new FileWriter("src\\main\\java\\com\\example\\NodeMap.txt", true)  //Set true for append mode
        );
        writer.newLine();   //Add new line
        writer.write(name);
        writer.newLine();
        writer.write(ip);
        writer.close();
        readNodeMap();
        readDatabase();
    }
    private int requestFile(String filename){
        Integer hash = hashfunction(filename, false);
        if(dataBase.get(hash)!=null)
            return dataBase.get(hash);
        else
            return -1;
    }
    private void removeNodeFromMap(Integer node) throws IOException {
        nodes.clear();
        File file = new File("src\\main\\java\\com\\example\\NodeMap.txt");
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
                new FileWriter("src\\main\\java\\com\\example\\NodeMap.txt", false)  //Set true for append mode
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
        readDatabase();
    }
    private void readDatabase() throws IOException {
        File file2 = new File("src\\main\\java\\com\\example\\Database2.txt");
        BufferedReader br2 = new BufferedReader(new FileReader(file2));
        String st2;
        dataBase.clear();
        while ((st2 = br2.readLine()) != null){
            Integer tempfile = hashfunction(st2,false);
            Integer temp = tempfile-1;
            while (nodes.get(temp)==null && temp != 0){
                temp--;
            }
            if (temp == 0)
                dataBase.put(tempfile,highest);
            dataBase.put(tempfile,highest);
        }
        System.out.println(dataBase.toString());
    }
    private void readNodeMap() throws IOException {
        File file = new File("src\\main\\java\\com\\example\\NodeMap.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        nodes.clear();
        while ((st = br.readLine()) != null){
            String ip = br.readLine();
            int hash = hashfunction(st, true);
            System.out.println("node "+st+" heeft hashwaarde "+ hash);
            nodes.put(hash, ip);
            if (hash>highest)
                highest = hash;
        }
    }
    @GetMapping("/AddNode")
    public String output (@RequestParam(value = "name", defaultValue = "omo") String name,@RequestParam(value = "ip", defaultValue = "omo") String ip) throws IOException {
        if (!name.equals("omo") && !ip.equals("omo")) {
            addNodeToMap(name, ip);
            return "node "+name+" with ip address "+ip+" was succesfully added to the node map";
        }
        else
            return"adding new node failed";
    }
    @GetMapping("/RemoveNode")
    public String output (@RequestParam(value = "ID", defaultValue = "omo") String ID) throws IOException {
        if (!ID.equals("omo")) {
            removeNodeFromMap(Integer.parseInt(ID));
            return "node "+ID+" was succesfully Removed from the node map";
        }
        else
            return"removing node failed";
    }
    @GetMapping("/LocateFile")
    public String output2 (@RequestParam(value = "fileName", defaultValue = "omo")String fileName,@RequestParam(value = "remove", defaultValue = "false") String remove) throws IOException {
        if (!fileName.equals("omo")){
            if (requestFile(fileName) == -1)
                return "File Not present in any of the nodes";
            else
            if(remove.equals("true")){
                Integer temp = requestFile(fileName);
                String ip = nodes.get(requestFile(fileName));
                removeNodeFromMap(temp);
                return "File "+fileName +" is located at node with ip: "+ip+" and the node was removed";
            }
            else
                return "File "+fileName +" is located at node with ip: "+nodes.get(requestFile(fileName));
        }
        return "This command requires a filename";
    }
}