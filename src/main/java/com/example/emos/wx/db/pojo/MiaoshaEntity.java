package com.example.emos.wx.db.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @Classname MiaoshaEntity
 * @Description TODO
 * @Date 2021/9/8 16:57
 * @Created by GZK0329
 */

@Data
public class MiaoshaEntity {

    /**
     * 秒杀主键
     */
    private int miaoshaId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 库存
     */
    private int number;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 当前时间
     */
    private Date currentTime;

}
