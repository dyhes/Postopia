package com.heslin.postopia.redis;

import com.heslin.postopia.redis.model.OpinionAggregation;
import com.heslin.postopia.redis.repository.OpinionAggregationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final OpinionAggregationRepository opinionAggregationRepository;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate, OpinionAggregationRepository opinionAggregationRepository) {
        this.redisTemplate = redisTemplate;
        this.opinionAggregationRepository = opinionAggregationRepository;
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

    public void updateOpinionAggregation(String spaceName, Long postId, Long commentId, String username, boolean isPositive) {
        String id;
        if (commentId != null) {
            id = "comment_" + commentId;
        } else {
            id = "post_" + postId;
        }
        OpinionAggregation opinionAggregation = opinionAggregationRepository.findById(id).orElse(new OpinionAggregation(id, spaceName, postId, commentId));
        opinionAggregation.update(username, isPositive);
        opinionAggregationRepository.save(opinionAggregation);
    }

    public Iterable<OpinionAggregation> findAll() {
        return opinionAggregationRepository.findAll();
    }

    public Page<OpinionAggregation> findAllOpinionAggregations(int page) {
        int size = 1000;
        Pageable pageable = PageRequest.of(page, size);
        String key = "opinion_aggregation:zset";

        // 计算分页索引
        long start = pageable.getOffset();
        long end = start + pageable.getPageSize() - 1;

        // 按score降序分页查询
        List<OpinionAggregation> aggregations = Objects.requireNonNull(redisTemplate.opsForZSet().reverseRange(key, start, end)).stream().map(aggregation -> (OpinionAggregation)aggregation).toList();

        // 获取总数
        Long total = redisTemplate.opsForZSet().size(key);

        // 构建Page对象
        return new PageImpl<>(new ArrayList<>(aggregations), pageable, total);
    }

    private void processBatchAggregations(List<String> keys, Consumer<List<OpinionAggregation>> consumer) {
//        System.out.println("keys");
//        System.out.println(keys);
//        List<OpinionAggregation> opinionAggregations = oaRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//            keys.forEach(key -> connection.hGetAll(key.getBytes()));
//            return null;
//        }).stream()
//        //.filter(Objects::nonNull) // 过滤空结果
//        .map(result -> {
//            // 反序列化 Map<byte[], byte[]> 为实体对象
//            System.out.println("result");
//            System.out.println(result);
//            Map<byte[], byte[]> rawData = (Map<byte[], byte[]>) result;
//            return oaRedisTemplate.getValueSerializer().deserialize();
//        })
//        .filter(Objects::nonNull) // 过滤反序列化失败项
//        .collect(Collectors.toList());
//        List<OpinionAggregation> aggregations = objects.stream().map(aggregation -> (OpinionAggregation)aggregation).toList();
//        System.out.println("aggregations");
//        System.out.println(aggregations);
        List<OpinionAggregation> aggregations = keys.stream().map(key -> opinionAggregationRepository.findById(key.split(":")[1]).orElseThrow()).toList();
        opinionAggregationRepository.deleteAll(aggregations);
        consumer.accept(aggregations);
        keys.clear();
    }

    public void sendOpinionMessage(Consumer<List<OpinionAggregation>> consumer) {
        int pageSize = 1000;
        ScanOptions options = ScanOptions.scanOptions()
            .match("opinion_aggregation:*")
            .count(pageSize)
            .build();
        Cursor<String> cursor = redisTemplate.scan(options);
        List<String> ids = new ArrayList<>();
        while (cursor.hasNext()) {
            String key = cursor.next();
            System.out.printf("key: %s%n", key);
            ids.add(key);
            if (ids.size() >= pageSize) {
                processBatchAggregations(ids, consumer);
            }
        }
        if (!ids.isEmpty()) {
            processBatchAggregations(ids, consumer);
        }
    }
}
