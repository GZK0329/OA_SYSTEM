package com.example.emos.wx.controller;

import com.example.emos.wx.common.utils.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.LoginForm;
import com.example.emos.wx.controller.form.RegisterForm;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Classname UserController
 * @Description TODO
 * @Date 2021/7/30 17:54
 * @Created by GZK0329
 */

@RestController
@RequestMapping("/user")
@Api("用户模块web接口")
public class UserController {

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public R register(@Valid @RequestBody RegisterForm form) {
        int id = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
        String token = jwtUtil.createToken(id);
        Set<String> permissions = userService.searchUserPermissions(id);
        saveCacheToken(token, id);
        return R.ok("注册成功").put("token", token).put("permissions", permissions);
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public R login(@Valid @RequestBody LoginForm form){
        String code = form.getCode();
        int userId = userService.login(code);
        String token = jwtUtil.createToken(userId);
        saveCacheToken(token,userId);
        Set<String> permissions = userService.searchUserPermissions(userId);
        return R.ok("登录成功").put("token", token).put("permission", permissions);
    }
    private void saveCacheToken(String token, int userId) {
        redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
    }

    @GetMapping("/searchUserSummary")
    @ApiOperation("查找用户信息")
    public R searchUserSummary(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        HashMap map = userService.searchUserSummary(userId);
        return R.ok().put("result", map);
    }
}
