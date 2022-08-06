package com.example.Dao;

import com.example.Pojo.comptroller;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface comptrollerDao {

    Integer InsertSJ(@Param("comptroller") comptroller comptroller);

    @Select("select * from log_audit where id = #{id}")
    comptroller SelectByid(@Param("id") Integer id);

    @Select("select max(id) from log_audit")
    Integer SelectMaxid();

}