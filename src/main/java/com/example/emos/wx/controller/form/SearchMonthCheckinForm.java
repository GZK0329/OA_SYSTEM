package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Classname SearchMonthCheckinForm
 * @Description TODO
 * @Date 2021/8/10 10:08
 * @Created by GZK0329
 */

@Data
@ApiModel
public class SearchMonthCheckinForm {

    @NotNull
    @Range(min=2000,max=3000)
    private Integer year;

    @NotNull
    @Range(min=1,max=12)
    private Integer month;
}
