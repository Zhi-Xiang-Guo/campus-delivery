package com.sky.cache.bloom;

/**
 * 不启用布隆时的空实现。
 */
public class NoopBloomFilterStrategy implements BloomFilterStrategy {
    @Override
    public boolean mightContain(String bloomName, String value) {
        return true;
    }

    @Override
    public void put(String bloomName, String value) {
        // noop
    }
}

