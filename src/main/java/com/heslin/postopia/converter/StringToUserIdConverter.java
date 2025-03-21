package com.heslin.postopia.converter;

import com.heslin.postopia.dto.UserId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUserIdConverter implements Converter<String, UserId> {

    @Override
    public UserId convert(String source) {
        UserId userId = null;
        try {
            System.out.println("source:" + source);
            userId = new  UserId(UserId.masked(Long.parseLong(source)));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return userId;
    }
}
