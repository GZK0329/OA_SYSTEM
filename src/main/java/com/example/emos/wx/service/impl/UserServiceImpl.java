package com.example.emos.wx.service.impl;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.db.dao.TbUserDao;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.UserService;
import com.example.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * @Classname UserServiceImpl
 * @Description TODO
 * @Date 2021/7/30 9:26
 * @Created by GZK0329
 */

@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao tbUserDao;

    @Autowired
    private MessageTask messageTask;

    @Override
    public String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap<String, Object> map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String result = HttpUtil.post(url, map);
        JSONObject jsonObject = JSONUtil.parseObj(result);
        String openid = (String) jsonObject.get("openid");
        if (openid == null || openid.length() == 0) {
            throw new RuntimeException("临时登录凭证出错！");
        }
        return openid;
    }

    @Override
    public int registerUser(String registerCode, String code, String nickName, String photo) {
        if (registerCode.equals("000000")) {
            Boolean bool = tbUserDao.haveRootUser();
            if (!bool) {
                String openId = getOpenId(code);
                HashMap<String, Object> map = new HashMap<>();
                map.put("openId", openId);
                map.put("nickname", nickName);
                map.put("photo", photo);
                map.put("role", "[0]");
                map.put("status", 1);
                map.put("createTime", new Date());
                map.put("root", true);
                tbUserDao.insert(map);
                int id = tbUserDao.searchIdByOpenId(openId);

                MessageEntity entity = new MessageEntity();
                entity.setSenderId(0);
                entity.setSenderName("系统消息");
                entity.setMsg("欢迎您注册成为超级管理员，请及时更新你的员工个人信息。");
                entity.setSendTime(new Date());
                messageTask.sendAsync(id + "", entity);

                return id;
            } else {
                throw new EmosException("已经有超级管理员，绑定错误！");
            }
        } else {
            return 0;
        }
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        Set<String> permissions = tbUserDao.searchUserPermissions(userId);
        return permissions;
    }

    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer id = tbUserDao.searchIdByOpenId(openId);
        messageTask.receiveAsync(id.toString());
        if (id == null) {
            throw new EmosException("用户不存在");
        }
        //TODO 从消息队列中接收消息转移到消息表
        return id;
    }

    @Override
    public TbUser searchById(int userId) {
        TbUser tbUser = tbUserDao.searchById(userId);
        return tbUser;
    }

    @Override
    public String searchUserHireDate(int userId) {
        String hireDate = tbUserDao.searchUserHireDate(userId);
        return hireDate;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap map = tbUserDao.searchUserSummary(userId);
        return map;
    }
}
