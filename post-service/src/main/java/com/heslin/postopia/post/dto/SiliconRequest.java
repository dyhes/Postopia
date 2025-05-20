package com.heslin.postopia.post.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SiliconRequest {
    public record SiliconMessage(String role, String content) {}
    private String model;
    private List<SiliconMessage> messages = new ArrayList<>();
    private boolean stream;

    public SiliconRequest(String model, String sysPrompt) {
        this.model = model;
        this.stream = false;
        this.messages.add(new SiliconMessage("system", sysPrompt));
    }

    public void append(String prompt) {
        this.messages.add(new SiliconMessage("user", prompt));
    }
}
