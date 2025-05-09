package com.heslin.postopia.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.heslin.postopia.service.intelligence.IntelligenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("intelligence")
public class IntelligenceController {

    private final IntelligenceService intelligenceService;

    @Autowired
    public IntelligenceController(IntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService;
    }

    @GetMapping(value = "summary", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> summary(@RequestParam Long postId) {
        return Flux.create(sink -> {
            CompletableFuture.runAsync(() -> {
                try {
                    intelligenceService.summary(sink, postId);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

}
