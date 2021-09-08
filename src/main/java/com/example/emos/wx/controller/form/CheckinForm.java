package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Classname CheckinForm
 * @Description TODO
 * @Date 2021/8/5 14:48
 * @Created by GZK0329
 */
@Data
@ApiModel
public class CheckinForm {

    private String address;
    private String country;
    private String province;
    private String city;
    private String district;

}

