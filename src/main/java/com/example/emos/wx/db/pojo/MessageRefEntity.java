package com.example.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @Classname MessageRefEntity
 * @Description TODO
 * @Date 2021/8/12 10:23
 * @Created by GZK0329
 */

@Document(collection = "message_ref")
@Data
public class MessageRefEntity implements Serializable {
    @Id
    private String _id;

    @Indexed
    private String messageId;

    @Indexed
    private Integer receiverId;

    @Indexed
    private Boolean readFlag;

    @Indexed
    private Boolean lastFlag;
}

