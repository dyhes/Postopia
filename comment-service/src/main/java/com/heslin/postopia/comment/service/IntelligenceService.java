package com.heslin.postopia.comment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.post.dto.SiliconRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

@Service
@RefreshScope
public class IntelligenceService {
    @Value("${postopia.silicon.model}")
    private String model;
    @Value("${postopia.silicon.key}")
    private String apiKey;
    @Value("${postopia.silicon.prompt.system}")
    private String sysPrompt;
    @Value("${postopia.silicon.prompt.summary}")
    private String summaryPrompt;
    private final WebClient webClient;
    private final CommentService commentService;
    private final ObjectMapper objectMapper;

    @Autowired
    public IntelligenceService(WebClient webClient, CommentService commentService, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.commentService = commentService;
        this.objectMapper = objectMapper;
    }

    public void summary(FluxSink<ServerSentEvent<String>> sink, Long postId) throws JsonProcessingException {
        System.out.println("sysPrompt:");
        System.out.println(sysPrompt);
        System.out.println("userPrompt:");
        String userPrompt = objectMapper.writeValueAsString(commentService.getSummaryInfo(postId));
        System.out.println(userPrompt);
        SiliconRequest body = new SiliconRequest(model, sysPrompt);
        body.append(userPrompt);
        // Simulate some processing
        webClient
        .post()
        .uri("/chat/completions")
        .header("Authorization", "Bearer " + apiKey)
        .bodyValue(body)
        .retrieve()
        .bodyToFlux(String.class)
        .doOnNext(data -> sink.next(ServerSentEvent.builder(data).build()))
        .doOnError(sink::error)
        .publishOn(Schedulers.boundedElastic())
        .doOnComplete(
        () -> {
            sink.next(ServerSentEvent.builder("[SUMMARY]").build());
            body.setStream(true);
            body.append(summaryPrompt);
            webClient
            .post()
            .uri("/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(String.class)
            .doOnNext(data -> sink.next(ServerSentEvent.builder(data).build()))
            .doOnError(sink::error)
            .doOnComplete(sink::complete)
            .subscribe();
        }
        )
        .subscribe();
    }
}