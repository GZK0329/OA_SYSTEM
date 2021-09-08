package com.example.emos.wx.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;


/**
 * @Classname OAuth2Token
 * @Description 将token封装
 * @Date 2021/7/25 17:26
 * @Created by GZK0329
 */

public class OAuth2Token implements AuthenticationToken {
    private String token;

    public OAuth2Token(String token) {
        this.token = token;
    }

    @Override
    public String getPrincipal() {
        return token;
    }

    @Override
    public String getCredentials() {
        return token;
    }
}
