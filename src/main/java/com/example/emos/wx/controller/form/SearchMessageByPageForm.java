package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @Classname SearchMessageByPageForm
 * @Description TODO
 * @Date 2021/8/12 20:46
 * @Created by GZK0329
 */
@ApiModel
@Data
public class SearchMessageByPageForm {
    @NotNull
    @Min(1)
    private Integer page;

    @NotNull
    @Range(min = 1,max = 40)
    private Integer length;
}

