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
//@ConfigurationProperties (prefix = "spring.es")
@Data
public class ESproperties {
    private final Logger mylog = LoggerFactory.getLogger(ESproperties.class);

    @Scheduled(cron = "${spring.es.sche}")
    public void koko () throws Exception {
        this.Update(Log.class);
        this.Update(comptroller.class);
    }


    /**
     * 更新索引的 indexName属性，
     * @param cls cls必须有 @Document 注解，否则报错
     * @throws Exception
     */
    public void Update(Class<?> cls) throws Exception {
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
        Map memberValues = (Map)hField.get(invocationHandler);
        String oldName = (String) memberValues.get("indexName");
        String substring = "";
        if (oldName.contains("-")){
            int i = oldName.indexOf("-");
            substring = oldName.substring(0,i);
            memberValues.remove("indexName");
        }else {
            substring = oldName;
        }
        memberValues.put("indexName",substring + "-" +  TimeUtils.ParseDate(new Date(),2));
        mylog.info((String) memberValues.get("indexName"));
    }





}
