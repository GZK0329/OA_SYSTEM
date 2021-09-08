package com.example.emos.wx.config.shiro;

import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * @Classname OAuth2Realm
 * @Description TODO
 * @Date 2021/7/25 17:29
 * @Created by GZK0329
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /*
    * 授权（验证权限时调用）
    * */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        TbUser user = (TbUser)principals.getPrimaryPrincipal();
        int userId = user.getId();
        Set<String> permissionSet = userService.searchUserPermissions(userId);
        info.setStringPermissions(permissionSet);
        //TODO 查询用户权限列表
        //TODO 把权限列表添加到info对象中
        return info;
    }

    /*
    * 认证（登录时调用）
    * */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //TODO 从令牌中获取用户userId，并检测该账户是否被冻结
        String accessToken = (String) token.getPrincipal();
        int userId = jwtUtil.getUserId(accessToken);
        TbUser user = userService.searchById(userId);
        if(user == null){
            throw new LockedAccountException("账户已被锁定，请联系管理员！");
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());
        //TODO 往用户对象info中添加用户信息以及token字符串
        return info;
    }
}
