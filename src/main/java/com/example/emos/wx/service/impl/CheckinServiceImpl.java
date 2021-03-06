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

        String type = "?????????";
        if (DateUtil.date().isWeekend()) {
            type = "?????????";
        }

        if (boolWorkday) {
            type = "?????????";
        } else if (boolHoliday) {
            type = "?????????";
        }

        if (type.equals("?????????")) {
            return "????????????????????????";
        } else {
            DateTime now = new DateTime();
            String start = DateUtil.today() + "" + systemConstants.attendanceStartTime;
            String end = DateUtil.today() + "" + systemConstants.attendanceEndTime;
            DateTime attendanceStartTime = DateUtil.parse(start);
            DateTime attendanceEndTime = DateUtil.parse(end);

            if (now.isBefore(attendanceStartTime)) {
                return "????????????????????????????????????";
            } else if (now.isAfter(attendanceEndTime)) {
                return "????????????????????????????????????";
            } else {
                HashMap map = new HashMap();
                map.put("userId", userId);
                map.put("date", date);
                map.put("start", attendanceStartTime);
                map.put("end", attendanceEndTime);
                boolean bool = checkinDao.haveCheckin(map) != null ? true : false;
                return bool ? "???????????????????????????????????????" : "????????????";
            }
        }
    }

    public void checkin(HashMap param) {
        DateTime now = DateUtil.date();
        DateTime d2 = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceTime);//????????????
        DateTime d3 = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);//????????????????????????

        int status = 1;//??????????????????
        if (now.compareTo(d2) <= 0) {
            status = 1;//??????
        } else if (now.compareTo(d2) > 0 && now.compareTo(d3) <= 0) {
            status = 2;//??????
        } else if (now.compareTo(d3) > 0) {
            status = 3;//??????
        }
        int userId = (int) param.get("userId");
        String faceModel = faceModelDao.searchFaceModel(userId);
        if (faceModel == null) {
            throw new EmosException("???????????????????????????");
        } else {
            String photoPath = (String) param.get("path");

            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(photoPath),
                    "targetModel", faceModel);
            request.form("code", code);
            HttpResponse response = request.execute();
            if (response.getStatus() == 200) {
                String body = response.body();
                if ("?????????????????????".equals(body) || "???????????????????????????".equals(body)) {
                    throw new EmosException(body);
                } else if ("False".equals(body)) {
                    throw new EmosException("?????????????????????????????????");
                } else if ("True".equals(body)) {
                    //TODO ???????????????????????????????????????
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
                                //result = "?????????";
                                if ("?????????".equals(result)) {
                                    risk = 3;
                                    //TODO ??????????????????
                                    HashMap map = userDao.searchNameAndDept(userId);
                                    String name = (String) map.get("name");
                                    String deptName = (String) map.get("dept_name");
                                    deptName = deptName != null ? deptName : "";
                                    SimpleMailMessage message = new SimpleMailMessage();
                                    message.setTo(hrEmail);
                                    message.setSubject("??????" + name + "???????????????????????????");
                                    message.setText(deptName + "????????????" + name + ":" + DateUtil.format(new Date(), "yyyy???MM???dd???") + "????????????????????????:" + city + district + "???");
                                    emailTask.sendAsync(message);
                                } else if ("?????????".equals(result)) {
                                    risk = 2;
                                    //TODO ??????????????????
                                    HashMap map = userDao.searchNameAndDept(userId);
                                    String name = (String) map.get("name");
                                    String deptName = (String) map.get("dept_name");
                                    deptName = deptName != null ? deptName : "";
                                    SimpleMailMessage message = new SimpleMailMessage();
                                    message.setTo(hrEmail);
                                    message.setSubject("??????" + name + "???????????????????????????");
                                    message.setText(deptName + "????????????" + name + ":" + DateUtil.format(new Date(), "yyyy???mm???dd???") + "????????????????????????:" + city + district + "???");
                                    emailTask.sendAsync(message);
                                }
                            }
                        } catch (IOException e) {
                            log.error("????????????", e);
                            throw new EmosException("????????????????????????");
                        }
                    }
                    //TODO ??????????????????
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
                log.error("????????????????????????");
                throw new EmosException("????????????????????????");
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
            throw new EmosException("????????????????????????????????????");
        } else {
            String body = response.body();
            if ("?????????????????????".equals(body) || "???????????????????????????".equals(body)) {
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

            String type = "?????????";
            if (one.isWeekend()) {
                type = "?????????";
            }
            if (holidaysList != null && holidaysList.contains(one)) {
                type = "?????????";
            } else if (workdaysList != null && workdaysList.contains(one)) {
                type = "?????????";
            }
            //?????? ?????????????????? ?????????????????? ??????????????? ???????????????????????????????????????????????????
            String status = "";
            if (type.equals("?????????") && DateUtil.compare(one, DateUtil.date()) <= 0) {
                status = "??????";
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
            res.put("day", one.dayOfWeekEnum().toChinese("???"));
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
