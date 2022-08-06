package com.example.Run;

import com.alibaba.fastjson.JSONObject;
import com.example.Pojo.Log;
import com.example.Pojo.comptroller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
public class Redis {

    private final Jedis jedis;

    @Autowired
    public Redis(Jedis jedis) {
        this.jedis = jedis;
    }

    public boolean Select(Integer db){
        if (db >= 0){
            this.jedis.select(db);
            return true;
        }
        return false;
    }

    /**
     * 写入数据库失败的数据会写入到redis中
     * comptroller || LogOperation
     */
    public <T> boolean InsertFail(T data){
        this.Select(9);
        String key = "";
        String value = JSONObject.toJSONString(data);
        if (data instanceof comptroller)
            key = "comptroller";

        if (data instanceof Log)
            key = "Log";


        if (key.equals(""))
            return false;

        try {
            jedis.lpush(key, value);
            return true;
        }catch (Exception e){
            return false;
        }

    }








}
