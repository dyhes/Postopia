package com.heslin.postopia.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setByMinute(String key, Object value, int minute) {
        redisTemplate.opsForValue().set(key, value, minute, TimeUnit.MINUTES);
    }

    public String get(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void sendOpinionMessage(Consumer<List<String>> consumer, String pattern) {
        int pageSize = 1000;
        ScanOptions options = ScanOptions.scanOptions()
        .match(pattern) //"opinion_aggregation:*")
        .count(pageSize)
        .build();
        Cursor<String> cursor = redisTemplate.scan(options);
        List<String> ids = new ArrayList<>();
        while (cursor.hasNext()) {
            String key = cursor.next();
            System.out.printf("key: %s%n", key);
            ids.add(key);
            if (ids.size() >= pageSize) {
                consumer.accept(ids);
            }
        }
        if (!ids.isEmpty()) {
            consumer.accept(ids);
        }
    }
}
