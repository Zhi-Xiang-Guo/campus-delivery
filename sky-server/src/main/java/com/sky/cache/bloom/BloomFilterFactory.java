package com.sky.cache.bloom;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 工厂模式：按配置创建布隆过滤器策略实现。
 */
@Component
public class BloomFilterFactory {

    private final StringRedisTemplate stringRedisTemplate;

    public BloomFilterFactory(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public BloomFilterStrategy create(BloomProperties properties) {
        if (properties == null || !properties.isEnabled()) {
            return new NoopBloomFilterStrategy();
        }

        String strategy = properties.getStrategy();
        if (strategy == null) strategy = "redisBitmap";

        if ("redisBitmap".equalsIgnoreCase(strategy)) {
            return new RedisBitmapBloomFilterStrategy(
                    stringRedisTemplate,
                    properties.getKeyPrefix(),
                    properties.getBitSize(),
                    properties.getHashFunctions()
            );
        }

        // 默认降级为 noop，避免因配置错误影响业务
        return new NoopBloomFilterStrategy();
    }
}

