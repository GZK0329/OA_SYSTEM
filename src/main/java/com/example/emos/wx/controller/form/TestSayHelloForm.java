package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;


/**
 * @Classname TestSayHelloForm
 * @Description TODO
 * @Date 2021/7/23 22:18
 * @Created by GZK0329
 */

@ApiModel
@Data
public class TestSayHelloForm {
    /*@NotBlank
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$")*/
    @ApiModelProperty(value = "姓名")
    private String name;
}
