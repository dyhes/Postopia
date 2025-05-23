package com.heslin.postopia.opinion.redis;

import com.heslin.postopia.common.redis.model.COAggragation;
import com.heslin.postopia.common.redis.model.POAggregation;
import com.heslin.postopia.common.redis.repository.COAggregationRepository;
import com.heslin.postopia.common.redis.repository.POAggregationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpinionRedisService {
    private final POAggregationRepository POAggregationRepository;
    private final COAggregationRepository COAggregationRepository;

    @Autowired
    public OpinionRedisService(POAggregationRepository POAggregationRepository, COAggregationRepository COAggregationRepository) {
        this.POAggregationRepository = POAggregationRepository;
        this.COAggregationRepository = COAggregationRepository;
    }

    public void updatePOOpinionAggregation(Long spaceId, Long postId, Long userId, String username, boolean isPositive) {
        POAggregation opinionAggregation = POAggregationRepository.findById(postId).orElse(new POAggregation(postId, spaceId));
        System.out.println("poaggregation");
        System.out.println(opinionAggregation);
        opinionAggregation.update(userId, username, isPositive);
        POAggregationRepository.save(opinionAggregation);
    }

    public void updateCOOpinionAggregation(Long spaceId, Long commentId, Long userId, String username, boolean isPositive) {
        COAggragation opinionAggregation = COAggregationRepository.findById(commentId).orElse(new COAggragation(commentId, spaceId));
        System.out.println("coaggregation");
        System.out.println(opinionAggregation);
        opinionAggregation.update(userId, username, isPositive);
        COAggregationRepository.save(opinionAggregation);
    }

//    public Iterable<OpinionAggregation> findAll() {
//        return opinionAggregationRepository.findAll();
//    }

//    public Page<OpinionAggregation> findAllOpinionAggregations(int page) {
//        int size = 1000;
//        Pageable pageable = PageRequest.of(page, size);
//        String key = "opinion_aggregation:zset";
//
//        // 计算分页索引
//        long start = pageable.getOffset();
//        long end = start + pageable.getPageSize() - 1;
//
//        // 按score降序分页查询
//        List<OpinionAggregation> aggregations = Objects.requireNonNull(redisTemplate.opsForZSet().reverseRange(key, start, end)).stream().map(aggregation -> (OpinionAggregation)aggregation).toList();
//
//        // 获取总数
//        Long total = redisTemplate.opsForZSet().size(key);
//
//        // 构建Page对象
//        return new PageImpl<>(new ArrayList<>(aggregations), pageable, total);
//    }

//    private void processBatchAggregations(List<String> keys, Consumer<List<OpinionAggregation>> consumer) {
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
//    }
}
