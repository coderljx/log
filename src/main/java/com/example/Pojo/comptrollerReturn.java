package com.example.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class comptrollerReturn {

 private Long id;
 private String appid;
 private String appname;
 private String orgid;
 private String orgname;
 private String method;
 private String description;
 private String content;
 private String auditcontent;
 private String params;
 private String moudel;
 private String userid;
 private String recorddate;
 private String ipaddress;
 private String createby;

}
