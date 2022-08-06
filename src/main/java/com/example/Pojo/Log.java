package com.example.Pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
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
@Document (indexName = "log")
public class Log implements Serializable {
    @Id
    private Long id;

    /**
     * type : 字段数据类型
     * analyzer : 分词器类型
     * index : 是否索引(默认:true),索引不存在创建，存在直接写数据
     * Keyword : 短语,不进行分词
     * ik_max_word : 最大力度分词
     * ik_smart : 最小力度分词
     */
//    @Field (type= FieldType.Text)
    private String appid;

//    @Field (type= FieldType.Text)
    private String orgid;

//    @Field (type= FieldType.Text)
    private String level;

//    @Field (type= FieldType.Text)
    private String eventype;

//    @Field (type= FieldType.Text, analyzer = "ik_max_word")
    private String logmessage;

//    @Field (type= FieldType.Text, analyzer = "ik_max_word")
    private String logdetail;

//    @Field (type= FieldType.Text)
    private String userid;

    @Field (type= FieldType.Date)
    private Date recorddate;

//    @Field (type= FieldType.Text)
    private String createby;

    @Field (type= FieldType.Date)
    private Date createdate;

    @Field (type= FieldType.Ip)
    private String ipaddress;

    private String identity;

    private static final long serialVersionUID = 1L;
}