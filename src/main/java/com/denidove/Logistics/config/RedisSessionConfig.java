/*
package com.denidove.Logistics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
public class RedisSessionConfig {
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new JdkSerializationRedisSerializer();
    }

    /* Раньше был универсальный, человеко-читаемый сериализатор, но не умеет сериализовать сложные внутренние классы Spring Security
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        // ✅ теперь и Spring Session Redis, и твой RedisTemplate — оба используют JSON
        return new GenericJackson2JsonRedisSerializer();
    }
}*/
