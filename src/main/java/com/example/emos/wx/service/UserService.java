package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.TbUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @Classname UserService
 * @Description TODO
 * @Date 2021/7/30 9:25
 * @Created by GZK0329
 */
@Service
public interface UserService {

    String getOpenId(String code);

    int registerUser(String registerCode, String code, String nickName, String photo);

    Set<String> searchUserPermissions(int userId);

    Integer login(String code);

    TbUser searchById(int userId);

    String searchUserHireDate(int userId);

    HashMap searchUserSummary(int userId);
}

