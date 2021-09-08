package com.example.emos.wx.controller;

import com.example.emos.wx.common.utils.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.DeleteMessageByIdForm;
import com.example.emos.wx.controller.form.SearchMessageByIdForm;
import com.example.emos.wx.controller.form.SearchMessageByPageForm;
import com.example.emos.wx.controller.form.UpdateUnReadMessageForm;
import com.example.emos.wx.service.MessageService;
import com.example.emos.wx.task.MessageTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

/**
 * @Classname MessageController
 * @Description TODO
 * @Date 2021/8/12 20:47
 * @Created by GZK0329
 */
@RestController
@RequestMapping("/message")
@Api("消息模块接口")
public class MessageController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageTask messageTask;

    @PostMapping("/searchMessageByPage")
    @ApiOperation("获取分页消息列表")
    public R searchMessageByPage(@Valid @RequestBody SearchMessageByPageForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        int page = form.getPage();
        int length = form.getLength();
        long start = (page - 1) * length;

        List<HashMap> lists = messageService.searchMessageByPage(userId, start, length);

        return R.ok().put("result", lists);
    }

    @PostMapping("/searchMessageById")
    @ApiOperation("通过id获取消息")
    public R searchMessageById(@Valid @RequestBody SearchMessageByIdForm form) {
        HashMap map = messageService.searchMessageById(form.getId());
        return R.ok().put("result", map);
    }

    @PostMapping("/updateUnReadMessage")
    @ApiOperation("将未读消息更新为已读消息")
    public R updateUnReadMessage(@Valid @RequestBody UpdateUnReadMessageForm form) {
        String id = form.getId();
        long rows = messageService.updateUnreadMessage(id);
        return R.ok().put("result", rows == 1 ? true : false);
    }

    @PostMapping("/deleteMessageById")
    @ApiOperation("通过id删除消息")
    public R deleteMessageById(@Valid @RequestBody DeleteMessageByIdForm form) {
        long l = messageService.deleteMessageRefById(form.getId());
        return R.ok().put("result", l == 1 ? true : false);
    }


    @GetMapping("/refreshMessage")
    @ApiOperation("刷新用户的消息")
    public R refreshMessage(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        //异步接收消息
        messageTask.receiveAsync(userId + "");
        //查询接收了多少条消息
        long lastRows = messageService.searchLastCount(userId);
        //查询未读数据
        long unreadRows = messageService.searchUnreadCount(userId);
        return R.ok().put("lastRows", lastRows).put("unreadRows", unreadRows);
    }


}
