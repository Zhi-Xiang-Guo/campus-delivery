package com.sky.cache.bloom;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/**
 * Redis 位图布隆过滤器（使用 SETBIT/GETBIT）。
 *
 * 说明：
 * - 这是“可能存在 / 一定不存在”的快速判定，用于挡住缓存穿透。
 * - Bloom 只能增加，无法删除；删除业务通过缓存失效 + TTL 来收敛。
 */
public class RedisBitmapBloomFilterStrategy implements BloomFilterStrategy {

    private final StringRedisTemplate stringRedisTemplate;
    private final String keyPrefix;
    private final long bitSize;
    private final int hashFunctions;

    public RedisBitmapBloomFilterStrategy(
            StringRedisTemplate stringRedisTemplate,
            String keyPrefix,
            long bitSize,
            int hashFunctions
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.keyPrefix = keyPrefix;
        this.bitSize = bitSize;
        this.hashFunctions = hashFunctions;
    }

    @Override
    public boolean mightContain(String bloomName, String value) {
        if (value == null) return false;
        String redisKey = redisKey(bloomName);
        long[] offsets = offsets(value);
        for (long offset : offsets) {
            Boolean bit = stringRedisTemplate.opsForValue().getBit(redisKey, offset);
            if (bit == null || !bit) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void put(String bloomName, String value) {
        if (value == null) return;
        String redisKey = redisKey(bloomName);
        long[] offsets = offsets(value);
        for (long offset : offsets) {
            stringRedisTemplate.opsForValue().setBit(redisKey, offset, true);
        }
    }

    private String redisKey(String bloomName) {
        return keyPrefix + bloomName;
    }

    /**
     * 采用双 hash 生成 k 个 offset：offset_i = (h1 + i*h2) mod m
     */
    private long[] offsets(String value) {
        long h1 = crc32(value);
        long h2 = fnv1a64(value);
        if (h2 == 0) h2 = 0x9e3779b97f4a7c15L;
        long[] result = new long[hashFunctions];
        for (int i = 0; i < hashFunctions; i++) {
            long combined = h1 + (long) i * h2;
            // 取正数并取模
            long offset = (combined & Long.MAX_VALUE) % bitSize;
            result[i] = offset;
        }
        return result;
    }

    private static long crc32(String s) {
        CRC32 crc32 = new CRC32();
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    private static long fnv1a64(String s) {
        final long FNV_64_PRIME = 1099511628211L;
        long hash = 1469598103934665603L;
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        for (byte b : data) {
            hash ^= (b & 0xff);
            hash *= FNV_64_PRIME;
        }
        return hash;
    }
}

