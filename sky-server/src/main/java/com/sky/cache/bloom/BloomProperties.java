package com.sky.cache.bloom;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sky.bloom")
public class BloomProperties {
    /**
     * 是否启用布隆过滤器。
     */
    private boolean enabled = true;

    /**
     * 策略：redisBitmap / noop（默认 redisBitmap）
     */
    private String strategy = "redisBitmap";

    /**
     * Redis Key 前缀。
     */
    private String keyPrefix = "bf:";

    /**
     * 位图大小（bit）。
     */
    private long bitSize = 1_048_576L; // 2^20

    /**
     * hash 函数数量。
     */
    private int hashFunctions = 7;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public long getBitSize() {
        return bitSize;
    }

    public void setBitSize(long bitSize) {
        this.bitSize = bitSize;
    }

    public int getHashFunctions() {
        return hashFunctions;
    }

    public void setHashFunctions(int hashFunctions) {
        this.hashFunctions = hashFunctions;
    }
}

