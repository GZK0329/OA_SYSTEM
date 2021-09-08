package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.example.emos.wx.common.utils.R;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.CheckinForm;
import com.example.emos.wx.controller.form.SearchMonthCheckinForm;
import com.example.emos.wx.db.dao.TbUserDao;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * @Classname CheckinController
 * @Description TODO
 * @Date 2021/8/3 13:48
 * @Created by GZK0329
 */
@RequestMapping("/checkin")
@RestController
@Slf4j
@Api("签到模块web接口")
public class CheckinController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;

    @GetMapping("/validCanCheckin")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckin(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        String res = checkinService.validCanCheckin(userId, DateUtil.today());
        return R.ok(res);
    }


    @PostMapping("/checkin")
    @ApiOperation("用户签到")
    public R checkin(CheckinForm form, @RequestParam("photo") MultipartFile file,
                     @RequestHeader("token") String token) {
        if (null == file) {
            return R.error("没有上传文件");
        }
        int userId = jwtUtil.getUserId(token);
        String fileLowName = file.getOriginalFilename().toLowerCase();
        String path = imageFolder + "/" + fileLowName;
        if (!StrUtil.endWith(fileLowName, ".jpg")) {
            FileUtil.del(path);
            return R.error("必须提交jpg格式的图片");
        } else {
            try {
                file.transferTo(Paths.get(path));//Paths.get()将路径字符串转为path对象
                HashMap map = new HashMap();
                map.put("userId", userId);
                map.put("path", path);
                map.put("address", form.getAddress());
                map.put("country", form.getCountry());
                map.put("city", form.getCity());
                map.put("province", form.getProvince());
                map.put("district", form.getDistrict());

                checkinService.checkin(map);
                return R.ok("签到成功");

            } catch (IOException exception) {
                log.error(exception.getMessage());
                exception.printStackTrace();
            } finally {
                FileUtil.del(path);
            }
        }
        return R.error("不存在人脸模型数据");
    }

    @PostMapping("/createFaceModel")
    @ApiOperation("生成人脸模型数据")
    public R createFaceModel(@RequestParam("photo") MultipartFile file,
                             @RequestHeader("token") String token) {

        int userId = jwtUtil.getUserId(token);
        if (null == file) {
            return R.error("未上传照片");
        } else {
            String name = file.getOriginalFilename().toLowerCase();
            String path = imageFolder + "/" + name;
            if (!StrUtil.endWith(path, ".jpg")) {
                return R.error("请上传以jpg结尾的图片");
            } else {
                try {
                    file.transferTo(Paths.get(path));
                    checkinService.createFaceModel(userId, path);
                    return R.ok("人脸建模成功");
                } catch (IOException exception) {
                    log.error(exception.getMessage());
                    exception.printStackTrace();
                } finally {
                    FileUtil.del(path);
                }
            }
        }
        return R.error("生成人脸模型数据过程中发生未知异常！");
    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户一周的签到情况")
    public R searchTodayCheckin(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = checkinService.searchTodayCheckin(userId);
        map.put("attendanceTime", constants.attendanceTime);
        map.put("closingTime", constants.closingTime);
        long days = checkinService.searchCheckinDays(userId);
        map.put("checkinDays", days);

        //判断用户入职时间  入职前如果缺勤不算
        DateTime hireDate = DateUtil.parse(userService.searchUserHireDate(userId));
        DateTime startTime = DateUtil.beginOfWeek(DateUtil.date());//本周第一天的时间
        if (startTime.isBefore(hireDate)) {
            startTime = hireDate;
        }

        HashMap param = new HashMap<>();
        map.put("startTime", startTime);
        map.put("endTime", DateUtil.endOfWeek(DateUtil.date()));
        map.put("userId", userId);
        ArrayList<HashMap> list = checkinService.searchWeekCheckin(map);
        map.put("weekCheckin", list);
        return R.ok().put("result", map);
    }


    @RequestMapping("/searchMonthCheckin")
    @ApiOperation("查询用户月签到情况")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form,
                                @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        DateTime hireDate = DateUtil.parse(userService.searchUserHireDate(userId));
        String month = form.getMonth() > 10 ? form.getMonth().toString() : "0" + form.getMonth();
        DateTime startTime = DateUtil.parse(form.getYear() + "-" + month + "-01");

        if (startTime.isBefore(DateUtil.beginOfMonth(hireDate))) {
            throw new EmosException("日期异常,月份早于入职当月的第一天");
        } else if (startTime.isBefore(hireDate)) {
            startTime = hireDate;
        }
        DateTime endTime = DateUtil.endOfMonth(startTime);

        HashMap param = new HashMap();
        param.put("userId", userId);
        param.put("startTime", startTime);
        param.put("endTime", endTime);

        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);

        int sum_1 = 0,sum_2 = 0, sum_3 = 0;
        for (HashMap map : list) {
            String type = (String) map.get("type");
            String status = (String) map.get("status");
            if("工作日".equals(type)){
                if("正常".equals(status)){
                    sum_1++;
                }else if("迟到".equals(status)){
                    sum_2++;
                }else if("缺勤".equals(status)){
                    sum_3++;
                }
            }
        }
        return R.ok().put("sum_1", sum_1).put("sum_2", sum_2).put("sum_3", sum_3).put("list", list);
    }
}
