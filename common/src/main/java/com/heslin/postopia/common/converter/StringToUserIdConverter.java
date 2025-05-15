package com.heslin.postopia.common.converter;

import com.heslin.postopia.common.dto.UserId;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.core.convert.converter.Converter;

@JsonComponent
public class StringToUserIdConverter implements Converter<String, UserId> {

    @Override
    public UserId convert(String source) {
        System.out.println("StringToUserIdConverter convert");
        System.out.println("source = " + source);
        return new UserId(UserId.masked(Long.parseLong(source)));
    }
}
