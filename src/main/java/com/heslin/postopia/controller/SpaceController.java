package com.heslin.postopia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heslin.postopia.service.space.SpaceService;


@RestController
@RequestMapping("/space")
public class SpaceController {
    @Autowired
    private SpaceService spaceService;

    public record SpaceDto(String name, String description) {}

    @PostMapping("join")
    public String joinSpace(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    
}
