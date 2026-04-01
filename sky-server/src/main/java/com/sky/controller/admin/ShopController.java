package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "商户相关接口")
@Slf4j
public class ShopController {
    public static final String SHOP_STATUS_KEY = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 修改商户状态
     * @param status
     * @return
     */
    @PutMapping("/status/{status}")
    @ApiOperation("修改商户状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("修改商户状态：{}", status ==1 ? "营业中" : "已关闭");
        redisTemplate.opsForValue().set(SHOP_STATUS_KEY, status);

        return Result.success();
    }

    /**
     * 查询商户状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查询商户状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        if (status == null) {
            status = 1; // 默认关闭
        }
        log.info("商户状态：{}", status ==1 ? "营业中" : "已关闭");
        return Result.success(status);
    }


}
