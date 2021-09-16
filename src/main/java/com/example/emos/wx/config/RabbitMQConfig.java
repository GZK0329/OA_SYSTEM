package com.example.emos.wx.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Collections;

/**
 * @Classname RabbitMQConfig
 * @Description TODO
 * @Date 2021/8/13 10:35
 * @Created by GZK0329
 */
@Configuration
public class RabbitMQConfig {
    @Bean
    public ConnectionFactory getFactory(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("106.14.104.198");
        factory.setPort(5672);
        return factory;
    }
}
