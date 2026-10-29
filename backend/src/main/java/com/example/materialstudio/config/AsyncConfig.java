package com.example.materialstudio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 后台多步骤任务执行器使用的线程池。
 * 用虚拟线程（Java 21）承载每个制作任务，关闭页面不会中断（任务在服务端线程上跑）。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskWorkerExecutor")
    public Executor taskWorkerExecutor() {
        // 注意：bean 名不能是 taskExecutor，否则会与 @Service TaskExecutor 的默认 bean 名冲突
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
