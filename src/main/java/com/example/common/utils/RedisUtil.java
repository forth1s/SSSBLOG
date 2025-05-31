package com.example.common.utils;


import com.example.common.enums.BusinessType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Redis 工具类
 * </p>
 *
 * @author Ya Shi
 * @since 2024/3/12 14:02
 */
@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Long RELEASE_SUCCESS = 1L;
    private static final String RELEASE_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('del', KEYS[1]) " +
            "else " +
            "return 0 " +
            "end";

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 检查请求频率
    public boolean isRequestFrequent(String key, BusinessType  businessType, long interval) {
        String requestKey = getKeyPrefix(businessType) + "REQUEST_" + key;
        String lastTimeStr = (String) this.get(requestKey);
        if (StringUtils.hasLength(lastTimeStr)) {
            long lastTime = Long.parseLong(lastTimeStr);

            if (!this.hasKey(getKeyPrefix(businessType) + key)) {
                // 检查频率时，要同时验证对应的业务键是否存在。要是业务键不存在，就认为频率限制已失效。
                this.delete(requestKey);
                return false;
            }

            if (System.currentTimeMillis() - lastTime < interval){
                this.updateRequestTime(businessType, key);
                return true;
            }
        }
        return false;
    }

    // 根据业务类型获取 Redis 键前缀
    public String getKeyPrefix(BusinessType businessType) {
        return businessType.getPrefix(); // 直接调用接口方法
    }

    // 记录上次请求的时间
    public void updateRequestTime(BusinessType businessType, String id) {
        String prefix = this.getKeyPrefix(businessType);
        String lastRequestTimeKey = prefix + "REQUEST_" + id;
        this.set(lastRequestTimeKey, String.valueOf(System.currentTimeMillis()), businessType.getExpireTime(), businessType.getTimeUnit());
    }


    // 设置键值对
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 设置键值对并指定过期时间
    public void set(String key, Object value, long duration, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, duration, unit);
    }

    // 设置键值对并指定过期时间
    public void set(String key, Object value, long duration) {
        redisTemplate.opsForValue().set(key, value, duration, TimeUnit.SECONDS);
    }

    // 获取值
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 获取值
    public String getString(String key) {
        Object obj = redisTemplate.opsForValue().get(key);
        return obj == null ? null : obj.toString();
    }

    // 删除键
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 判断键是否存在
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    // 减量
    public void decrement(String key) {
        redisTemplate.opsForValue().decrement(key);
    }

    // 如果不存在，则设置
    public Boolean setNx(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    // 如果不存在，则设置，附带过期时间
    public Boolean tryLock(String lockKey, String requestId, long seconds) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, seconds, TimeUnit.SECONDS);
    }

    // 如果不存在，则设置，附带过期时间
    public Boolean tryLock(String lockKey, String requestId, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, timeout, unit);
    }

    // 不存在返回true，存在则删除
    public Boolean releaseLock(String lockKey, String requestId) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(RELEASE_SCRIPT);
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        return RELEASE_SUCCESS.equals(result);
    }
}
