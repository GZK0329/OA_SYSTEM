package com.example.emos.wx.db.dao;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Classname MessageDao
 * @Description TODO
 * @Date 2021/8/12 10:44
 * @Created by GZK0329
 */

@Repository
public class MessageDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    /***
     * @description: //TODO 
     *
     * @param entity TODO 
     * @return {@link String} TODO
     * @author GZK0329
     * @date 2021/8/12 11:09
     */
    public String insert(MessageEntity entity) {
        Date sendTime = entity.getSendTime();
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, 8);
        entity.setSendTime(sendTime);
        entity = mongoTemplate.save(entity,"message");
        String _id = entity.get_id();
        return _id;
    }

    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        JSONObject json = new JSONObject();
        //将_id属性转换为字符串设置为json
        json.set("$toString", "$_id");
        Aggregation aggregation = Aggregation.newAggregation(
                //聚合查询 将json作为id插入聚合对象
                Aggregation.addFields().addField("id").withValue(json).build(),
                Aggregation.lookup("message_ref", "id", "messagedId", "ref"),
                Aggregation.match(Criteria.where("ref.receiverId").is(userId)),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "sendTime")),
                Aggregation.skip(start),
                Aggregation.limit(length)
        );
        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "message", HashMap.class);
        List<HashMap> list = results.getMappedResults();
        list.forEach(one -> {
            List<MessageRefEntity> refList = (List<MessageRefEntity>) one.get("ref");
            System.out.println("================" + "/n" + refList.get(0) + "=======" + refList.get(1) + "/n===================");
            //获取了对应的MessageRefEntity对象
            MessageRefEntity entity = refList.get(0);
            Boolean readFlag = entity.getReadFlag();
            String refId = entity.get_id();
            one.remove("_id");
            one.remove("ref");
            one.put("readFlag", readFlag);
            one.put("refId", refId);
            //将GMT转为北京时间
            Date sendTime = (Date) one.get("sendTime");
            sendTime = DateUtil.offset(sendTime, DateField.HOUR, -8);

            String today = DateUtil.today();
            if (today.equals(DateUtil.date(sendTime).toDateStr())) {
                one.put("sendTime", DateUtil.format(sendTime, "HH:mm"));
            } else {
                one.put("sendTime", DateUtil.format(sendTime, "yyyy-MM-dd HH:mm:ss,SSS"));
            }
        });
        return list;
    }

    public HashMap searchMessageById(String userId) {
        HashMap res = mongoTemplate.findById(userId, HashMap.class, "message");
        Date sendTime = (Date) res.get("sendTime");
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, -8);
        res.replace("sendTime", DateUtil.format(sendTime, "yyyy-MM-dd HH:mm:ss,SSS"));
        return res;
    }
}
