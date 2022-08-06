package com.example.Dao;

import com.example.Pojo.Log;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface LogDao {

    @Select("SELECT max(id) FROM log")
    Integer Maxid();

    Integer Insertlog(@Param("LogOperation") Log LogOperation);

    @Select("select * from log where id = #{id}")
    Log SelectByid(@Param("id") Integer id );



}