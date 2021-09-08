package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Classname LoginForm
 * @Description TODO
 * @Date 2021/7/31 15:25
 * @Created by GZK0329
 */
@ApiModel
@Data
public class LoginForm {
    @NotBlank(message = "临时授权凭证不能为空")
    private String code;
}
