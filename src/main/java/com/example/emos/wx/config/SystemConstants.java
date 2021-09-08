package com.example.emos.wx.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Classname SystemConstants
 * @Description TODO
 * @Date 2021/8/2 16:32
 * @Created by GZK0329
 */

@Data
@Component
public class SystemConstants {

    public String attendanceStartTime;

    public String attendanceTime;

    public String attendanceEndTime;

    public String closingStartTime;

    public String closingTime;

    public String closingEndTime;

}
