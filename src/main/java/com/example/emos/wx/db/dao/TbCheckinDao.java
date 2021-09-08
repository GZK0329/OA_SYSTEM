package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbCheckinDao {

    Integer haveCheckin(HashMap map);

    void insertCheckin(TbCheckin checkin);

    HashMap searchTodayCheckin(int userId);

    long searchCheckinDays(int userId);

    ArrayList<HashMap> searchWeekCheckin(HashMap map);
}