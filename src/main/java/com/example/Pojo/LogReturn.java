package com.example.Pojo;

import lombok.Data;

@Data
public class LogReturn {

 private Long id;
 private String appid;
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
