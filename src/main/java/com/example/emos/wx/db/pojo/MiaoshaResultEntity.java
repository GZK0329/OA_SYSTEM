package com.example.emos.wx.db.pojo;

import java.util.Date;

/**
 * @Classname MiaoshaResultEntity
 * @Description TODO
 * @Date 2021/9/8 17:02
 * @Created by GZK0329
 */
public class MiaoshaResultEntity {
    /**
     * 秒杀Id
     */
    private int miaoshaId;

    /**
     * 用户的标识openId
     */
    private String openId;

    /**
     * 秒杀的状态
     */
    private Byte status;

    /**
     * 当前时间 生成时间
     */
    private Date currentTime;

    /**
     * 关联秒杀表
     */
    private MiaoshaEntity miaoshaEntity;

}
