package com.example.emos.wx.service;

import com.example.emos.wx.controller.form.CheckinForm;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Classname CheckinService
 * @Description TODO
 * @Date 2021/8/3 10:57
 * @Created by GZK0329
 */
@Service
public interface CheckinService {

    String validCanCheckin(int userId, String date);

    void checkin(HashMap param);

    void createFaceModel(int userId, String path);

    HashMap searchTodayCheckin(int userId);

    long searchCheckinDays(int userId);

    ArrayList<HashMap> searchWeekCheckin(HashMap map);

    ArrayList<HashMap> searchMonthCheckin(HashMap map);
}
