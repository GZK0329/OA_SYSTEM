package com.example.emos.wx.controller;

/**
 * @Classname TestController
 * @Description TODO
 * @Date 2021/7/22 17:22
 * @Created by GZK0329
 */

import com.example.emos.wx.common.utils.R;
import com.example.emos.wx.controller.form.TestSayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;

@RestController
@RequestMapping("/test")
@Api("测试Web接口")
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation("最简单的测试方法")
    public R sayHello(@Valid @RequestBody TestSayHelloForm form) {
        return R.ok().put("message", "Hello" + form.getName());
    }

    @PostMapping("/addUser")
    @ApiOperation("添加用户")
    @RequiresPermissions(value = {"ROOT","USER:ADD"}, logical = Logical.OR)
    public R addUser(){
        return R.ok("成功添加用户");
    }

}

