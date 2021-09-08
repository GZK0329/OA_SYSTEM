package com.example.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

/**
 * @Classname ThreadLocalToken
 * @Description TODO
 * @Date 2021/7/26 10:38
 * @Created by GZK0329
 */
@Component
public class ThreadLocalToken {
    private ThreadLocal local= new ThreadLocal();

    public void setToken(String token){
        local.set(token);
    }
    public String getToken(){
        return (String) local.get();
    }
    public void clear(){
        local.remove();
    }
}
