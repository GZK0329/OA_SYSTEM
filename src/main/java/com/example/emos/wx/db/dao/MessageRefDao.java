package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * @Classname MessageRefDao
 * @Description TODO
 * @Date 2021/8/12 10:44
 * @Created by GZK0329
 */
@Repository
public class MessageRefDao {

    @Autowired
    private MongoTemplate mongoTemplate;


    public String insert(MessageRefEntity entity) {
        MessageRefEntity refEntity = mongoTemplate.save(entity);
        return refEntity.get_id();
    }

    /**
     * @param userId TODO
     * @return {@link long} TODO
     * @description: //查询未读消息数量
     * @author GZK0329
     * @date 2021/8/12 17:04
     */
    public long searchUnReadCount(int userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("readFlag").is(false).and("receiverId").is(userId));
        long count = mongoTemplate.count(query, MessageRefEntity.class);
        return count;
    }

    /**
     * @param userId TODO
     * @return {@link long} TODO
     * @description: //该用户新接收的消息数量
     * @author GZK0329
     * @date 2021/8/12 17:03
     */
    public long searchLastCount(int userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("lastFlag").is(true).and("receiverId").is(userId));
        Update update = new Update();
        update.set("lastFlag", false);

        UpdateResult result = mongoTemplate.updateMulti(query, update, "message_ref");
        long modifiedCount = result.getModifiedCount();
        return modifiedCount;
    }

    /**
     * @description: //把未读消息转换为已读
     *
     * @param id TODO
     * @return {@link long} TODO
     * @author GZK0329
     * @date 2021/8/12 17:24
     */
    public long updateUnReadMessage(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();
        update.set("readFlag", true);

        UpdateResult result = mongoTemplate.updateMulti(query, update, "message_ref");
        return  result.getModifiedCount();
    }

    /**
     * @description: //删除某条ref数据
     *
     * @param id TODO
     * @return {@link long} TODO
     * @author GZK0329
     * @date 2021/8/12 17:32
     */
    public long deleteMessageById(String id){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        DeleteResult deleteResult = mongoTemplate.remove(query, "message_ref");
        return deleteResult.getDeletedCount();
    }

    public long deleteMessageByUserId(int userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("receiverId").is(userId));

        DeleteResult deleteResult = mongoTemplate.remove(query, "message_red");
        return deleteResult.getDeletedCount();
    }


}
