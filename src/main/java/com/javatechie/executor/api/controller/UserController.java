package com.javatechie.executor.api.controller;

import com.javatechie.executor.api.entity.User;
import com.javatechie.executor.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class UserController {
    @Autowired
    private UserService service;

    @PostMapping(value = "/users", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public ResponseEntity saveUsers(@RequestParam(value = "files") MultipartFile[] files) throws Exception {
        for (MultipartFile file : files) {
            service.saveUsers(file);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // TODO :: create non-threaded POST for performance comparison

    @GetMapping(value = "/users", produces = "application/json")
    public CompletableFuture<ResponseEntity> findAllUsers() {
       return service.findAllUsers().thenApply(ResponseEntity::ok);
    }

    @GetMapping(value = "/getUsersByThread", produces = "application/json")
    public ResponseEntity<List<User>> getUsers() throws ExecutionException, InterruptedException {
        CompletableFuture<List<User>> users1 = service.findAllUsers();
        CompletableFuture<List<User>> users2 = service.findAllUsers();
        CompletableFuture<List<User>> users3 = service.findAllUsers();
        // CompletableFuture<Void> futureResult = CompletableFuture.allOf(users1,users2,users3);


        // ... merging multiple lists obtained from completeable futures
        // ... take a look: https://stackoverflow.com/a/29826611/1964707
        List<User> users = Stream
                .of(users1, users2, users3)
                .map(CompletableFuture::join)
                .reduce((a, b) -> { a.addAll(b); return a; })
                .get();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(users);
    }
}
