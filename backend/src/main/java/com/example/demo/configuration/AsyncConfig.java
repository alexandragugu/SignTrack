package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean("hashExecutor")
    public ExecutorService hashExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}