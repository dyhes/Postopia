package com.heslin.postopia.dto.post;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


public record IntelligentPost(String subject, List<String> contents){}
