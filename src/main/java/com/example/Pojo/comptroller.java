package com.example.Pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;


@Data
@Document(indexName = "comptroller")
public class comptroller implements Serializable {
    @Id
    private Long id;

    /**
     * @author
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

    /**
     * type : 字段数据类型
     * analyzer : 分词器类型
     * index : 是否索引(默认:true)
     * Keyword : 短语,不进行分词
     * ik_max_word : 最大力度分词
     * ik_smart : 最小力度分词
     */
    private String appid;

    private String appname;

    private String orgid;

    private String orgname;

    private String method;

    private String description;

    private String content;

    @Field (type= FieldType.Text)
    private String auditcontent;

    @Field (type= FieldType.Text, analyzer = "ik_max_word")
    private String params;

    @Field (type= FieldType.Keyword)
    private String moudel;

    private String userid;

    private String username;

    private Date recorddate;

    @Field (type= FieldType.Ip)
    private String ipaddress;

    @Field (type= FieldType.Date)
    private Date createdate;

    private String createby;


    private static final long serialVersionUID = 1L;


}