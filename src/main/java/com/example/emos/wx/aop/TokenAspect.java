package com.example.emos.wx.aop;

import com.example.emos.wx.common.utils.R;
import com.example.emos.wx.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Classname TokenAspect
 * @Description TODO 切面类 如果更新token将更新后的token返回给客户端
 * @Date 2021/7/27 12:40
 * @Created by GZK0329
 */

@Component
@Aspect
public class TokenAspect {
    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Pointcut("execution(public * com.example.emos.wx.controller.*.*(..))")
    public void aspect() {
    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        R r = (R) joinPoint.proceed();//执行方法 将结果封装入R
        String token = threadLocalToken.getToken();
        if (token != null) {
            r.put("token", token);
            threadLocalToken.clear();
        }
        return r;
    }
}
