package com.example.Run;

import com.example.Pojo.Log;
import com.example.Pojo.comptroller;
import com.example.Utils.TimeUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.Map;

@Component
@Data
public class ESproperties {
    private final Logger mylog = LoggerFactory.getLogger(ESproperties.class);

    @Scheduled(cron = "${spring.es.sche}")
    public void koko () throws Exception {
        this.Update(Log.class);
        this.Update(comptroller.class);
    }


    /**
     * 自动更新更新索引的 indexName属性，
     * @param cls cls必须有 @Document 注解，否则报错
     * @throws Exception
     */
    public void Update(Class<?> cls,String... Name) throws Exception {
        // 检查当前的cls是否包含这个注解，不包含报错
        if (!cls.isAnnotationPresent(Document.class)) {
            mylog.info("yesyesyeysyesyey");
            throw new RuntimeException("cls 不包含 @Document");
        }
        Document annotation =  cls.getAnnotation(Document.class);
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        // 获取 AnnotationInvocationHandler 的 memberValues 字段
        Field hField = invocationHandler.getClass().getDeclaredField("memberValues");
        // 因为这个字段 private final 修饰，所以要打开权限
        hField.setAccessible(true);
        Map memberValues = (Map) hField.get(invocationHandler);
        String oldName = (String) memberValues.get("indexName");
        if (Name.length == 0) {
            Name = new String[1];
            Name[0] = NewIndexName(oldName);
        }
        memberValues.remove("indexName");
        memberValues.put("indexName",Name[0]);
        mylog.info((String) memberValues.get("indexName"));
    }


    /**
     * 更新索引的名称，方便跨越索引进行查询
     * 根据当前时间来设置
     */
    private String NewIndexName(String oldName) throws Exception {
        String substring = "";
        if (oldName.contains("-")){
            int i = oldName.indexOf("-");
            substring = oldName.substring(0,i);
        }else {
            substring = oldName;
        }
        return substring + "-" +  TimeUtils.ParseDate(new Date(),2);
    }

    /**
     * 根据年月来设置对应的索引名称
     * @param year 对应的年份
     * @param month 对应的月份
     */
    public void NewNameFromYM(String year,String month){

    }



}
