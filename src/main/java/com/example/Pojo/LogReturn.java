package com.example.Pojo;

import lombok.Data;

/**
 * 需要返回的字段写入，不写的字段将不做返回
 */
@Data
public class LogReturn {

 private Long id;
 private String appid;
 private String appname;
 private String orgid;
 private String level;
 private String eventype;
 private String logmessage;
 private String logdetail;
 private String userid;
 private String recorddate;
 private String createby;
 private String ipaddress;
 private String identity;

}
