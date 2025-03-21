package com.heslin.postopia.converter;

import com.heslin.postopia.dto.user.UserId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUserIdConverter implements Converter<String, UserId> {

    @Override
    public UserId convert(String source) {
        return new UserId(UserId.masked(Long.parseLong(source)));
    }
}
