package com.sky.cache.bloom;

/**
 * 策略模式：封装不同的布隆过滤器实现（如 Redis 位图、本地实现等）。
 */
public interface BloomFilterStrategy {

    /**
     * 判断 value 是否可能存在于指定 bloom 中。
     */
    boolean mightContain(String bloomName, String value);

    /**
     * 将 value 加入指定 bloom。
     */
    void put(String bloomName, String value);
}

