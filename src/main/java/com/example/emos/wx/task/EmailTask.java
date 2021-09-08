package com.example.emos.wx.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @Classname EmailTask
 * @Description TODO
 * @Date 2021/8/5 23:05
 * @Created by GZK0329
 */
@Component
@Scope("prototype")
public class EmailTask implements Serializable {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${emos.email.system}")
    private String mailBox;

    @Async
    public void sendAsync(SimpleMailMessage mailMessage){
        mailMessage.setFrom(mailBox);
        javaMailSender.send(mailMessage);
    }
}
