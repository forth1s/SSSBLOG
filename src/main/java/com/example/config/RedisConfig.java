package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    /*
    *   spring-data-redis 的 RedisTemplate<K, V>模板类 在操作redis时默认使用JdkSerializationRedisSerializer
        来进行序列化。spring操作redis是在jedis客户端基础上进行的，而jedis客户端与redis交互的时候协议中定义是用byte类型交互，
        看到spring-data-redis中RedisTemplate<K, V>在操作的时候k，v是泛型对象，而不是byte[]类型的，
        这样导致，如果不对RedisTemplate进行设置，spring会默认采用
        defaultSerializer = new JdkSerializationRedisSerializer();
        这个方法来对key、value进行序列化操作，JdkSerializationRedisSerializer它使用的编码是ISO-8859-1
    * */
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        //设置key序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 设置 value 序列化
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }
}