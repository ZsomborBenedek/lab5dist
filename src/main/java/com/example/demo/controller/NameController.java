package com.example.demo.controller;
import com.example.demo.service.MulticastListner;
import com.example.demo.service.RestNameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class NameController {
    RestNameService nameService;
    public NameController() throws IOException {
        nameService = new RestNameService();
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(new MulticastListner(nameService));
    }


    @GetMapping("/AddNode")
    public String output (@RequestParam(value = "Name", defaultValue = "omo") String name,@RequestParam(value = "Ip", defaultValue = "omo") String ip) throws IOException {
        if (!name.equals("omo") && !ip.equals("omo")) {
            nameService.addNodeToMap(name, ip);
            return "node "+name+" with ip address "+ip+" was succesfully added to the node map";
        }
        else
            return"adding new node failed";
    }
    @GetMapping("/RemoveNode")
    public String output (@RequestParam(value = "ID", defaultValue = "omo") String ID) throws IOException, InterruptedException {
        if (!ID.equals("omo")) {

            nameService.removeNodeFromMap(Integer.parseInt(ID));
            return "node "+ID+" was succesfully Removed from the node map";
        }
        else
            return"removing node failed";
    }
    @GetMapping("/LocateFile")
    public String output2 (@RequestParam(value = "fileName", defaultValue = "omo")String fileName,@RequestParam(value = "remove", defaultValue = "false") String remove) throws IOException, InterruptedException {
        if (!fileName.equals("omo")){
            if (nameService.requestFile(fileName) == -1)
                return "File Not present in any of the nodes";
            else
            if(remove.equals("true")){
                Integer temp = nameService.requestFile(fileName);
                String ip = nameService.nodes.get(nameService.requestFile(fileName));
                nameService.removeNodeFromMap(temp);
                return "File "+fileName +" is located at node with ip: "+ip+" and the node was removed";
            }
            else
                return "File "+fileName +" is located at node with ip: "+nameService.nodes.get(nameService.requestFile(fileName));
        }
        return "This command requires a filename";
    }
    @GetMapping("/AddFile")
    public String addFile (@RequestParam(value = "Name", defaultValue = "omo") String name,@RequestParam(value = "File", defaultValue = "omo") String file) throws IOException, InterruptedException {
        if (!name.equals("omo") && !file.equals("omo")) {
            if (nameService.addFileToDataBase(name,file)==1) {
                System.out.println("Running /AddFile, name " + name + " file  " + file);
                return "file " + file + " located at " + name + " was succesfully added to the node map";
            }
            else
                return "file "+file+" located at "+name+" was not added to the node map, Unexisting node";
        }
        else
            return"removing node failed";
    }
    @GetMapping("/RemoveFile")
    public String removeFile (@RequestParam(value = "Name", defaultValue = "omo") String name,@RequestParam(value = "File", defaultValue = "omo") String file) throws IOException, InterruptedException {
        if (!name.equals("omo") && !file.equals("omo")) {
            if (nameService.removeFileFromDatabase(name,file)==1) {
                //System.out.println("Ik run nu /AddFile, Variebelen name " + name + " file  " + file);
                return "file " + file + " located at " + name + " was succesfully removed";
            }
            else
                return "file "+file+" located at "+name+" was not removed to the node map, Unexisting node";
        }
        else
            return"removing node failed";
    }
    @GetMapping("/Database")
    public String database(){
        return nameService.dataBase.toString();
    }
}