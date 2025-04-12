package com.heslin.postopia.redis;

import com.heslin.postopia.redis.model.OpinionAggregation;
import com.heslin.postopia.redis.repository.OpinionAggregationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
}
