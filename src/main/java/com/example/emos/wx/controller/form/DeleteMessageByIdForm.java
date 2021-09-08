package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Classname DeleteMessageByIdForm
 * @Description TODO
 * @Date 2021/8/12 21:27
 * @Created by GZK0329
 */
@ApiModel
@Data
public class DeleteMessageByIdForm {

    @NotBlank
    private String id;

}
