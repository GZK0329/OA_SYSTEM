package com.example.emos.wx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Classname ThreadPoolConfig
 * @Description TODO
 * @Date 2021/8/5 22:42
 * @Created by GZK0329
 */

@Configuration
@Scope("prototype")
public class ThreadPoolConfig {

    @Bean("AsyncTaskExecutor")
    public AsyncTaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数量设置
        executor.setCorePoolSize(6);
        //最大线程数
        executor.setMaxPoolSize(12);
        //设置队列容量
        executor.setQueueCapacity(32);
        //设置线程活跃时间
        executor.setKeepAliveSeconds(60);
        //设置线程的命名前缀
        executor.setThreadNamePrefix("task-");
        //设置拒绝策略 TODO 深入学习一下
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
