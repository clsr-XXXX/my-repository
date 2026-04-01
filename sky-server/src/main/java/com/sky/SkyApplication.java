package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableCaching // 开启缓存注解
public class SkyApplication {
    public static void main(String[] args) {

        //1. 运行时需要打开mysql服务器
        //2. 配置文件中 password 要和真实的password符合

        //
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
    }
}
