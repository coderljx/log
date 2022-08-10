package com.example.API.Log;

import com.example.Interface.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**

 ID	bigint(20)	否	自增	主键ID
 APP_ID	int(11)	是		业务系统ID
 ORG_ID	varchar(20)	是		行政组织ID
 LEVEL	varchar(5)	否		日志级别: trace, debug, info, warn, error, fatal
 EVENT_TYPE	varchar(10)	否		事件类型，如：安全(Security)/系统(System)/错误(Error)/异常(Exception)
 LOG_MESSAGE	varchar(255)	否		日志概要信息
 LOG_MESSAGE_DETAIL	text	是		日志详细内容，如：异常信息
 USER_ID	varchar(20)	否		操作人
 RECORD_DATE	datetime	否		操作时间
 IP_ADDRESS	varchar(20)	否		操作的IP地址
 CREATE_DATE	datetime	否		创建时间
 CREATE_BY	varchar(30)	否		创建人
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogMessage {
    private String orgid;
    private String appid;
    @NotNull
    private String appname;
    @NotNull
    private String level;
    @NotNull
    private String eventype;
    @NotNull
    private String logmessage;
    private String logdetail;
    @NotNull
    private String userid;
    @NotNull
    private Date recorddate;
    @NotNull
    private String createby;
    @NotNull
    private String ipaddress;
    @NotNull
    private String identity;
}