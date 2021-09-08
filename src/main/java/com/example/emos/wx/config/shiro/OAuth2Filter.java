package com.example.emos.wx.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Classname OAuth2
 * @Description //TODO 拦截http请求 将token提取封装后交给shiro框架，shiro对其进行操作
 * //TODO 检查token的有效性，如果过期了 让客户端重新登录， 如果客户端token过期服务器端redis存储的token未过期则更新token并发送给客户端
 * @Date 2021/7/26 11:12
 * @Created by GZK0329
 */

@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Autowired
    private RedisTemplate redisTemplate;


    @Value("${emos.jwt.cache-expire}")
    private int cacheEmpire;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * @param request TODO
     * @return {@link String} TODO
     * @description: //TODO 获取请求中的token字符串
     * @author GZK0329
     * @date 2021/7/26 16:59
     */
    private String getRequestToken(HttpServletRequest request) {
        String token = (String) request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }
        return token;
    }

    /**
     * @param request  TODO
     * @param response TODO
     * @return {@link AuthenticationToken} TODO
     * @description: //TODO 将token字符串封装成token对象 可被shiro调用
     * @author GZK0329
     * @date 2021/7/26 16:59
     */
    public AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String token = getRequestToken((HttpServletRequest) request);
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return new OAuth2Token(token);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return false;
    }

    /**
     * @param request     TODO
     * @param response    TODO
     * @param mappedValue TODO
     * @return {@link boolean} TODO
     * @description: //TODO 拦截请求 判断是否需要被shiro处理 放行options
     * @author GZK0329
     * @date 2021/7/26 19:58
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = (HttpServletRequest) request;
        if (req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        return false;
    }

    /**
     * @param request     TODO
     * @param response    TODO
     * @param mappedValue TODO
     * @return {@link boolean} TODO
     * @description: //TODO 需要被shiro处理的请求
     * @author GZK0329
     * @date 2021/7/26 20:06
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Content-Type", "text/html;charset=UTF-8");
        //TODO 开启跨域请求 解决同源策略问题
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        threadLocalToken.clear();
        //TODO 获取请求中的token
        String token = getRequestToken((HttpServletRequest) request);

        if (StrUtil.isBlank(token)) {
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            return false;
        }
        try {
            jwtUtil.verifyToken(token);
        } catch (TokenExpiredException e) {
            //TODO 令牌过期异常 检查redis存储的令牌
            if (redisTemplate.hasKey(token)) {
                redisTemplate.delete(token);
                int userId = jwtUtil.getUserId(token);
                token = jwtUtil.createToken(userId);
                //把新的token更新到redis中
                redisTemplate.opsForValue().set(token, userId + "", cacheEmpire, TimeUnit.DAYS);

                //把新的token交给threadLcoalToken
                threadLocalToken.setToken(token);
            } else {
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                resp.getWriter().print("令牌已过期，请重新登录!");
                return false;
            }
        } catch (JWTDecodeException e) {
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌！");
            return false;
        }

        Boolean bool = executeLogin(request, response);
        return bool;
    }

    /**
     * @param token    TODO
     * @param e        TODO
     * @param request  TODO
     * @param response TODO
     * @return {@link boolean} TODO
     * @description: //TODO 登录失败的处理
     * @author GZK0329
     * @date 2021/7/27 10:16
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Content-Type", "text/html;charset=UTF-8");
        //TODO 开启跨域请求 解决同源策略问题
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            resp.getWriter().print(e.getMessage());
        } catch (IOException exception) {
        }
        return false;
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }
}
