package com.heslin.postopia.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.heslin.postopia.common.dto.UserId;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserIdSerializer extends JsonSerializer<UserId> {

    @Override
    public void serialize(UserId userId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeNumber(userId.getMaskedId());
    }
}
