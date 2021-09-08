package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Classname UpdateUnReadMessageForm
 * @Description TODO
 * @Date 2021/8/12 21:23
 * @Created by GZK0329
 */
@Data
@ApiModel
public class UpdateUnReadMessageForm {

    @NotBlank
    private String id;
}
