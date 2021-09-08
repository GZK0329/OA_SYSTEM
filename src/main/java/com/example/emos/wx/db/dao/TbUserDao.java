package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.Set;

@Mapper
public interface TbUserDao {
    int insert(HashMap<String, Object> map);

    Integer searchIdByOpenId(String openId);

    Set<String> searchUserPermissions(int userId);

    boolean haveRootUser();

    TbUser searchById(int userId);

    HashMap searchNameAndDept(int userId);

    String searchUserHireDate(int userId);

    HashMap searchUserSummary(int userId);
}