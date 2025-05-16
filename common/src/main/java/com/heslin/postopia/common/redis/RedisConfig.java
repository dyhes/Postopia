package com.heslin.postopia.common.redis;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
//@EnableRedisRepositories(
//basePackages = "com.heslin.postopia.redis.repository", // 指定 Redis 仓库路径
//enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP,
//considerNestedRepositories = true
//)
public class RedisConfig {

    @Bean
    RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        return template;
    }

//    @Bean
//    public RedisTemplate<String, OpinionAggregation> oaRedisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, OpinionAggregation> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(OpinionAggregation.class));
//        template.afterPropertiesSet();
//        return template;
//    }
}
