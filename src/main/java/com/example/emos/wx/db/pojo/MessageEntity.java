package com.example.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname MessageEntity
 * @Description TODO
 * @Date 2021/8/12 10:23
 * @Created by GZK0329
 */
@Data
@Document(collation = "message")
public class MessageEntity implements Serializable {

    @Id
    private String _id;

    @Indexed
    private Integer senderId;

    private String senderPhoto = "";
    private String senderName;

    @Indexed
    private Date sendTime;

    //消息正文内容
    private String msg;

}
