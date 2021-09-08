package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @Classname MessageService
 * @Description TODO
 * @Date 2021/8/12 17:35
 * @Created by GZK0329
 */
@Service
public interface MessageService {

    public String insertMessage(MessageEntity entity);

    public String insert(MessageRefEntity entity);

    public long searchUnreadCount(int userId);

    public long searchLastCount(int userId);

    public List<HashMap> searchMessageByPage(int userId, long start, int length) ;

    public HashMap searchMessageById(String id);

    public long updateUnreadMessage(String id) ;

    public long deleteMessageRefById(String id);

    public long deleteUserMessageRef(int userId);
}

