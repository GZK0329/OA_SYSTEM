package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Classname RegisterForm注册表单
 * @Description TODO
 * @Date 2021/7/30 16:38
 * @Created by GZK0329
 */

@ApiModel
@Data
public class RegisterForm {
    /**
     * registerCode 注册码
     */
    @NotBlank(message = "注册码不能为空")
    @Pattern(regexp = "^[0-9]{6}$", message = "注册码必须为6位数字")
    private String registerCode;

    /**
     * code临时码
     */
    @NotBlank(message = "临时码不能为空")
    private String code;

    /**
     * nickName昵称
     */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /**
     * photo 头像
     */
    @NotBlank(message = "头像不能为空")
    private String photo;

}
