package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Classname SearchMessageByIdForm
 * @Description TODO
 * @Date 2021/8/12 21:19
 * @Created by GZK0329
 */
@Data
@ApiModel
public class SearchMessageByIdForm {

    @NotBlank
    private String id;
}
