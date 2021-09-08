package com.example.emos.wx.service.impl;

import com.example.emos.wx.db.dao.MessageDao;
import com.example.emos.wx.db.dao.MessageRefDao;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.example.emos.wx.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @Classname MessageServiceImpl
 * @Description TODO
 * @Date 2021/8/12 17:35
 * @Created by GZK0329
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageDao messageDao;
    @Autowired
    private MessageRefDao refDao;

    @Override
    public String insertMessage(MessageEntity entity) {
        return messageDao.insert(entity);
    }

    @Override
    public String insert(MessageRefEntity entity) {
        return refDao.insert(entity);
    }

    @Override
    public long searchUnreadCount(int userId) {
        return refDao.searchUnReadCount(userId);
    }

    @Override
    public long searchLastCount(int userId) {
        return refDao.searchLastCount(userId);
    }

    @Override
    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        return messageDao.searchMessageByPage(userId,start,length);
    }

    @Override
    public HashMap searchMessageById(String id) {
        return messageDao.searchMessageById(id);
    }

    @Override
    public long updateUnreadMessage(String id) {
        return refDao.updateUnReadMessage(id);
    }

    @Override
    public long deleteMessageRefById(String id) {
        return refDao.deleteMessageById(id);
    }

    @Override
    public long deleteUserMessageRef(int userId) {
        return refDao.deleteMessageByUserId(userId);
    }
}
