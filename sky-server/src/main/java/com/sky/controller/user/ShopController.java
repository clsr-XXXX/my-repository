package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags ="商户相关接口")
@Slf4j
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    /**
     * 用户查询商户状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查询商户状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS_KEY);

        // ✅ 处理 null 情况
        if (status == null) {
            log.warn("⚠️ Redis 中未找到商户状态，使用默认值 1");
            status = 1;  // 默认营业
        }

        log.info("商户状态查询: {}", status == 1 ? "营业中" : "已关闭");
        return Result.success(status);
    }
}
