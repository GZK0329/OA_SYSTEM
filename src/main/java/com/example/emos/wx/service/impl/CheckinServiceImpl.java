package com.example.emos.wx.service.impl;

import cn.hutool.core.date.*;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.controller.form.CheckinForm;
import com.example.emos.wx.db.dao.*;
import com.example.emos.wx.db.pojo.TbCheckin;
import com.example.emos.wx.db.pojo.TbFaceModel;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @Classname CheckimServiceImpl
 * @Description TODO
 * @Date 2021/8/3 10:57
 * @Created by GZK0329
 */
@Service
@Slf4j
@Scope("prototype")
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private SystemConstants systemConstants;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbCheckinDao checkinDao;

    @Autowired
    private TbFaceModelDao faceModelDao;

    @Autowired
    private TbCityDao cityDao;

    @Autowired
    private TbUserDao userDao;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;

    @Value("${emos.email.hr}")
    private String hrEmail;

    @Value("${emos.email.system}")
    private String systemEmail;

    @Autowired
    private EmailTask emailTask;

    @Value("${emos.code}")
    private String code;

    @Override
    public String validCanCheckin(int userId, String date) {
        boolean boolWorkday = workdayDao.searchTodayIsWorkday() != null ? true : false;
        boolean boolHoliday = holidaysDao.searchTodayIsHoliday() != null ? true : false;

        String type = "工作日";
        if (DateUtil.date().isWeekend()) {
            type = "节假日";
        }

        if (boolWorkday) {
            type = "工作日";
        } else if (boolHoliday) {
            type = "节假日";
        }

        if (type.equals("节假日")) {
            return "节假日不需要考勤";
        } else {
            DateTime now = new DateTime();
            String start = DateUtil.today() + "" + systemConstants.attendanceStartTime;
            String end = DateUtil.today() + "" + systemConstants.attendanceEndTime;
            DateTime attendanceStartTime = DateUtil.parse(start);
            DateTime attendanceEndTime = DateUtil.parse(end);

            if (now.isBefore(attendanceStartTime)) {
                return "还没有到上班考勤签到时间";
            } else if (now.isAfter(attendanceEndTime)) {
                return "已经过了上班考勤结束时间";
            } else {
                HashMap map = new HashMap();
                map.put("userId", userId);
                map.put("date", date);
                map.put("start", attendanceStartTime);
                map.put("end", attendanceEndTime);
                boolean bool = checkinDao.haveCheckin(map) != null ? true : false;
                return bool ? "今日已经考勤，请勿重复考勤" : "可以考勤";
            }
        }
    }

    public void checkin(HashMap param) {
        DateTime now = DateUtil.date();
        DateTime d2 = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceTime);//上班时间
        DateTime d3 = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);//上班考勤截止时间

        int status = 1;//正常签到状态
        if (now.compareTo(d2) <= 0) {
            status = 1;//正常
        } else if (now.compareTo(d2) > 0 && now.compareTo(d3) <= 0) {
            status = 2;//迟到
        } else if (now.compareTo(d3) > 0) {
            status = 3;//缺勤
        }
        int userId = (int) param.get("userId");
        String faceModel = faceModelDao.searchFaceModel(userId);
        if (faceModel == null) {
            throw new EmosException("库内不存在人脸模型");
        } else {
            String photoPath = (String) param.get("path");

            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(photoPath),
                    "targetModel", faceModel);
            request.form("code", code);
            HttpResponse response = request.execute();
            if (response.getStatus() == 200) {
                String body = response.body();
                if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
                    throw new EmosException(body);
                } else if ("False".equals(body)) {
                    throw new EmosException("签到无效，人脸识别失败");
                } else if ("True".equals(body)) {
                    //TODO 获取签到地区的疫情风险等级
                    int risk = 1;
                    String city = (String) param.get("city");
                    String district = (String) param.get("district");
                    if (!StrUtil.isBlank(city) || !StrUtil.isBlank(district)) {
                        String code = cityDao.searchCityCode(city);
                        String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                        try {
                            Document document = Jsoup.connect(url).get();
                            Elements elements = document.getElementsByClass("list-content");
                            for (Element element : elements) {
                                String result = element.text().split("")[1];
                                //result = "高风险";
                                if ("高风险".equals(result)) {
                                    risk = 3;
                                    //TODO 发送邮件通知
                                    HashMap map = userDao.searchNameAndDept(userId);
                                    String name = (String) map.get("name");
                                    String deptName = (String) map.get("dept_name");
                                    deptName = deptName != null ? deptName : "";
                                    SimpleMailMessage message = new SimpleMailMessage();
                                    message.setTo(hrEmail);
                                    message.setSubject("员工" + name + "处于高风险地区警告");
                                    message.setText(deptName + "部门员工" + name + ":" + DateUtil.format(new Date(), "yyyy年MM月dd日") + "处于【高风险地区:" + city + district + "】");
                                    emailTask.sendAsync(message);
                                } else if ("中风险".equals(result)) {
                                    risk = 2;
                                    //TODO 发送邮件通知
                                    HashMap map = userDao.searchNameAndDept(userId);
                                    String name = (String) map.get("name");
                                    String deptName = (String) map.get("dept_name");
                                    deptName = deptName != null ? deptName : "";
                                    SimpleMailMessage message = new SimpleMailMessage();
                                    message.setTo(hrEmail);
                                    message.setSubject("员工" + name + "处于中风险地区警告");
                                    message.setText(deptName + "部门员工" + name + ":" + DateUtil.format(new Date(), "yyyy年mm月dd日") + "处于【中风险地区:" + city + district + "】");
                                    emailTask.sendAsync(message);
                                }
                            }
                        } catch (IOException e) {
                            log.error("执行异常", e);
                            throw new EmosException("获取风险等级失败");
                        }
                    }
                    //TODO 保存签到记录
                    TbCheckin checkin = new TbCheckin();
                    String address = (String) param.get("address");

                    String province = (String) param.get("province");

                    checkin.setUserId(userId);
                    checkin.setAddress(address);
                    checkin.setCity(city);

                    checkin.setDistrict(district);
                    checkin.setProvince(province);
                    checkin.setDate(DateUtil.today());
                    checkin.setCreateTime(now);
                    checkin.setStatus((byte) status);
                    checkin.setRisk(risk);

                    checkinDao.insertCheckin(checkin);
                }
            } else {
                log.error("人脸识别服务异常");
                throw new EmosException("人脸识别服务异常");
            }
        }
    }

    @Override
    public void createFaceModel(int userId, String path) {
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo", FileUtil.file(path));
        request.form("code", code);
        HttpResponse response = request.execute();
        if (response.getStatus() != 200) {
            throw new EmosException("生成人脸模型请求返回异常");
        } else {
            String body = response.body();
            if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
                throw new EmosException(body);
            } else {
                TbFaceModel entity = new TbFaceModel();
                entity.setUserId(userId);
                entity.setFaceModel(body);
                faceModelDao.insertFaceModel(entity);
            }
        }
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap map = checkinDao.searchTodayCheckin(userId);
        return map;
    }

    @Override
    public long searchCheckinDays(int userId) {
        long num = checkinDao.searchCheckinDays(userId);
        return num;
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap map) {

        ArrayList<HashMap> checkinWeekList = checkinDao.searchWeekCheckin(map);
        ArrayList<String> holidaysList = holidaysDao.searchHolidaysInRange(map);
        ArrayList<String> workdaysList = workdayDao.searchWorkdaysInRange(map);

        DateTime startDate = DateUtil.parse(map.get("startTime").toString());
        DateTime endDate = DateUtil.parse(map.get("startTime").toString());
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList list = new ArrayList<>();
        range.forEach(one -> {
            String date = one.toString("yyyy-MM-dd");

            String type = "工作日";
            if (one.isWeekend()) {
                type = "节假日";
            }
            if (holidaysList != null && holidaysList.contains(one)) {
                type = "节假日";
            } else if (workdaysList != null && workdaysList.contains(one)) {
                type = "工作日";
            }
            //状态 打卡或者迟到 默认设置为空 如果是当天 未到打卡结束时间则当天状态保持为空
            String status = "";
            if (type.equals("工作日") && DateUtil.compare(one, DateUtil.date()) <= 0) {
                status = "缺勤";
                boolean flag = false;
                for (HashMap<String, String> element : checkinWeekList) {
                    if (element.containsValue(date)) {
                        status = element.get("status");
                        flag = true;
                        break;
                    }
                    String today = DateUtil.today();
                    DateTime endTime = DateUtil.parse(today + systemConstants.attendanceEndTime);

                    if (today.equals(date) && DateUtil.date().isBefore(endTime) && flag == false) {
                        status = "";
                    }
                }
            }
            HashMap<String, String> res = new HashMap<>();
            res.put("date", date);
            res.put("status", status);
            res.put("type", type);
            res.put("day", one.dayOfWeekEnum().toChinese("周"));
            list.add(res);
        });
        return list;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap map) {

        ArrayList<HashMap> list = checkinDao.searchWeekCheckin(map);
        return list;
    }
}
