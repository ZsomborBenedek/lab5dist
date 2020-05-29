package com.example.demo.controller;

import com.example.demo.model.FileModel;
import com.example.demo.model.NodeModel;
import com.example.demo.service.MulticastService;
import com.example.demo.service.RestNameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class NameController {

    RestNameService nameService;

    public NameController() throws IOException {
        this.nameService = new RestNameService();
        final ExecutorService threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(new MulticastService(this.nameService));
    }

    @PostMapping("/AddNode")
    public ResponseEntity<NodeModel> addNode(@RequestBody NodeModel node) {
        try {
            this.nameService.addNodeToMap(node.getName(), node.getIp());
        } catch (IOException e) {
            return new ResponseEntity<>(node, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        return new ResponseEntity<>(node, HttpStatus.CREATED);
    }

    @DeleteMapping("/RemoveNode")
    public ResponseEntity<NodeModel> removeNode(@RequestBody NodeModel node)
            throws NumberFormatException, IOException, InterruptedException {
        try {
            this.nameService.removeNodeFromMap(Integer.parseInt(node.getName()));
        } catch (IOException e) {
            return new ResponseEntity<>(node, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(node, HttpStatus.NO_CONTENT);
    }

    @GetMapping("/LocateFile/{fileName}")
    public ResponseEntity<FileModel> locateFile(@PathVariable String fileName,
            @RequestParam(value = "remove", defaultValue = "false") String remove) {
        if (!fileName.isEmpty()) {
            Integer nodeHash = nameService.requestFile(fileName);
            String ip = nameService.nodes.get(nodeHash);
            if (nodeHash == -1)
                return new ResponseEntity<>(new FileModel(fileName), HttpStatus.NOT_FOUND);
            else if (remove.equals("true")) {
                try {
                    this.nameService.removeNodeFromMap(nodeHash);
                } catch (IOException | InterruptedException e) {
                    return new ResponseEntity<>(new FileModel(ip, fileName), HttpStatus.NOT_FOUND);
                }
                return new ResponseEntity<>(new FileModel(fileName), HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(new FileModel(ip, fileName), HttpStatus.OK);
        }
        return new ResponseEntity<>(new FileModel(fileName), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/AddFile")
    public ResponseEntity<FileModel> addFile(@RequestBody FileModel file) {
        if (!file.getNode().isEmpty() && !file.getFile().isEmpty()) {
            try {
                if (nameService.addFileToDataBase(file.getNode(), file.getFile()) == 1) {
                    System.out.println("Running /AddFile, name " + file.getNode() + " file  " + file.getFile());
                    return new ResponseEntity<>(file, HttpStatus.CREATED);
                } else
                    return new ResponseEntity<>(file, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            } catch (IOException | InterruptedException e) {
                return new ResponseEntity<>(file, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
        }
        return new ResponseEntity<>(file, HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/RemoveFile")
    public ResponseEntity<FileModel> removeFile(@RequestBody FileModel file) {
        if (!file.getNode().isEmpty() && !file.getFile().isEmpty()) {
            try {
                if (nameService.removeFileFromDatabase(file.getNode(), file.getFile()) == 1) {
                    return new ResponseEntity<>(file, HttpStatus.NO_CONTENT);
                } else
                    return new ResponseEntity<>(file, HttpStatus.NOT_FOUND);
            } catch (IOException | InterruptedException e) {
                return new ResponseEntity<>(file, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(file, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/Database")
    public ResponseEntity<String> database() {
        return new ResponseEntity<String>(nameService.dataBase.toString(), HttpStatus.OK);
    }
}