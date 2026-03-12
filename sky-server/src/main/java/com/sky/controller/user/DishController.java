package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import com.sky.cache.bloom.BloomFilterStrategy;
import com.sky.cache.bloom.BloomWarmupRunner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private BloomFilterStrategy bloomFilterStrategy;
    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        if(categoryId == null){
            return Result.success(java.util.Collections.emptyList());
        }
        // 缓存穿透防护：明显不存在的分类 id 直接返回空
        if(!bloomFilterStrategy.mightContain(BloomWarmupRunner.BLOOM_CATEGORY_ID, categoryId.toString())){
            return Result.success(java.util.Collections.emptyList());
            //return Result.success(List.of());
        }

        String key = "dish_"+categoryId;
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if(list!=null&& !list.isEmpty()){
            return Result.success(list);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        list = dishService.listWithFlavor(dish);
        // 正常列表缓存稍长，空列表缓存较短（防止缓存穿透/频繁打库）
        if(list == null || list.isEmpty()){
            redisTemplate.opsForValue().set(key, list, 5, TimeUnit.MINUTES);
        }else{
            redisTemplate.opsForValue().set(key, list, 30, TimeUnit.MINUTES);
        }

        return Result.success(list);
    }

}
