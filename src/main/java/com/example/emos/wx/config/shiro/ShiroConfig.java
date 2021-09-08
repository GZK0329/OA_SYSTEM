package com.example.emos.wx.config.shiro;


import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Classname ShiroConfig
 * @Description TODO
 * @Date 2021/7/27 10:22
 * @Created by GZK0329
 */
@Configuration
public class ShiroConfig {
    /**
     * @param realm TODO
     * @return {@link SecurityManager} TODO
     * @description: //TODO 设置安全管理对象 将自定义的realm注入
     * @author GZK0329
     * @date 2021/7/27 10:47
     */
    @Bean("securityManager")
    public SecurityManager securityManager(OAuth2Realm realm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //TODO 设置realm
        securityManager.setRealm(realm);
        //TODO 是否记得登录状态 因为token保存在浏览器端 所以不在服务器设置
        securityManager.setRememberMeManager(null);
        return securityManager;
    }

    /**
     * @description: //TODO shiro过滤器的配置
     *
     * @param securityManager TODO
     * @param filter TODO
     * @return {@link ShiroFilterFactoryBean} TODO
     * @author GZK0329
     * @date 2021/7/27 10:59
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager, OAuth2Filter filter) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();

        shiroFilter.setSecurityManager(securityManager);

        Map<String, Filter> map = new HashMap<>();
        map.put("oauth2", filter);
        shiroFilter.setFilters(map);

        Map<String, String> filterChain = new LinkedHashMap<>();
        filterChain.put("/webjars/**", "anon");
        filterChain.put("/druid/**", "anon");
        filterChain.put("/app/**", "anon");
        filterChain.put("/sys/login", "anon");
        filterChain.put("/swagger/**", "anon");
        filterChain.put("/v2/api-docs", "anon");
        filterChain.put("/swagger-ui.html", "anon");
        filterChain.put("/swagger-resources/**", "anon");
        filterChain.put("/captcha.jpg", "anon");
        filterChain.put("/user/register", "anon");
        filterChain.put("/user/login", "anon");
        //filterChain.put("/test/**", "anon");
        //TODO 先写这么多 anon代表不拦截
        //TODO 除了上面的 都是用oauth2拦截
        filterChain.put("/**", "oauth2");
        shiroFilter.setFilterChainDefinitionMap(filterChain);
        return shiroFilter;
    }

    /**
     * @description: //TODO 生命周期管理
     *
     * @return {@link LifecycleBeanPostProcessor} TODO
     * @author GZK0329
     * @date 2021/7/27 10:59
     */
    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }

    /**
     * @description: //TODO 切面类AOP
     *
     * @param securityManager TODO
     * @return {@link AuthorizationAttributeSourceAdvisor} TODO
     * @author GZK0329
     * @date 2021/7/27 11:57
     */
    @Bean("authorizationAttributeSourceAdvisor")
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
