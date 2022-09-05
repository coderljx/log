package com.example.API.SJ;

import com.example.Interface.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SjMessage {

    /**
     ID	bigint(20)	否	自增	主键ID
     APP_ID	int(11)	否		业务系统ID
     APP_NAME	varchar(30)	否		业务系统名称
     ORG_ID	varchar(20)	否		行政组织ID
     ORG_NAME	varchar(30)	否		行政组织名称
     AUDIT_METHOD	varchar(128)	否		操作方法名【如：addUser】
     AUDIT_DESCRIPTION	varchar(128)	否		操作方法描述【如: 新增】
     AUDIT_CONTENT	varchar(255)	否		操作内容，一句话描述操作该方法的内容【如：新增用户】
     AUDIT_PARAMS	text	否		操作内容的详细参数，如：JSON
     USER_ID	varchar(20)	否		操作人
     RECORD_DATE	datetime	否		操作时间
     IP_ADDRESS	varchar(20)	否		操作的IP地址
     CREATE_DATE	datetime	否		创建时间
     CREATE_BY	varchar(30)	否		创建人
     */
    @NotNull
   private String appid;
    @NotNull
   private String appname;
   @NotNull
   private String orgid;
   @NotNull
   private String orgname;
   @NotNull
   private String method;
   @NotNull
   private String description;
   @NotNull
   private String content;
   @NotNull
   private String params;
   @NotNull
   private String userid;
   @NotNull
   private String username;
   @NotNull
   private String moudel;
   @NotNull
   private Date recorddate;
   @NotNull
   private String ipaddress;
   @NotNull
   private String createby;
   @NotNull
   private String status;
   @NotNull
   private String resparams;

}
