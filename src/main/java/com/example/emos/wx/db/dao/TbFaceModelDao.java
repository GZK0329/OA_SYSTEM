package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbFaceModelDao {

    String searchFaceModel(int userId);

    void insertFaceModel(TbFaceModel model);

    void deleteFaceModel(int userId);

}