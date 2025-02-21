package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    //编写我们自己的RedisTemplate(这是固定模板)

    @Bean
    /*
    *   spring-data-redis 的 RedisTemplate<K, V>模板类 在操作redis时默认使用JdkSerializationRedisSerializer
        来进行序列化。spring操作redis是在jedis客户端基础上进行的，而jedis客户端与redis交互的时候协议中定义是用byte类型交互，
        看到spring-data-redis中RedisTemplate<K, V>在操作的时候k，v是泛型对象，而不是byte[]类型的，
        这样导致的一个问题就是，如果不对RedisTemplate进行设置，spring会默认采用
        defaultSerializer = new JdkSerializationRedisSerializer();
        这个方法来对key、value进行序列化操作，JdkSerializationRedisSerializer它使用的编码是ISO-8859-1
    * */
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        //设置key序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
