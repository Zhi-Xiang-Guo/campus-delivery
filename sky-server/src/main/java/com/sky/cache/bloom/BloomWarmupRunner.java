package com.sky.cache.bloom;

import com.sky.mapper.CategoryMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时把“存在的分类 id”灌入布隆，挡住明显不存在的 categoryId 请求。
 */
@Component
public class BloomWarmupRunner implements ApplicationRunner {

    public static final String BLOOM_CATEGORY_ID = "category:id";

    private final BloomFilterStrategy bloomFilterStrategy;
    private final CategoryMapper categoryMapper;

    public BloomWarmupRunner(BloomFilterStrategy bloomFilterStrategy, CategoryMapper categoryMapper) {
        this.bloomFilterStrategy = bloomFilterStrategy;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Long> ids = categoryMapper.listAllIds();
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            if (id != null) {
                bloomFilterStrategy.put(BLOOM_CATEGORY_ID, id.toString());
            }
        }
    }
}

