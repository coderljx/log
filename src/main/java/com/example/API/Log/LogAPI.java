package com.example.API.Log;


import com.example.Pojo.Log;
import com.example.Run.Rocket;
import com.example.Service.LogDaoService;
import com.example.Utils.Coco;
import com.example.Utils.Maputil;
import com.example.Utils.Response;
import com.example.Utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping ("/log")
public class LogAPI {
    private final Logger log = LoggerFactory.getLogger(LogAPI.class);
    private final Rocket rocket;
    private final LogDaoService logDevelopDaoService;
    private final String Topic = "log";

    @Autowired()
    public LogAPI(Rocket rocket,
                  LogDaoService logDevelopDaoService) {
        this.rocket = rocket;
        this.logDevelopDaoService = logDevelopDaoService;
    }

    /**
     * {
     *             "payload" : {
     *                    "appid" : "",     系统id
     *                     "orgid" : "",    行政组织id
     *                     "level" : "",    日志级别 ： trace, debug, info, warn, error, fatal
     *                     "class" : "",    类名
     *                     "line" : "",     代码行数
     *                     "method" : "",   方法名称
     *                     "params" : "",   参数
     *                     "messsage" : "", 具体信息
     *                     "user" : "",     操作人
     *                     "date" : "",     操作时间
     *                     "createby" : ""  创建人
     *         }
     *         }
     * 生产消息
     */
    @PostMapping ("/create")
    public Response create (@RequestBody Map<String, Object> maps,
                            HttpServletRequest request){
        Map<String, Object> payload = (Map<String, Object>) maps.get("payload");
        if (payload == null)
            return new Response<>(Coco.ParamsError);

        try {
            String date = (String) payload.get("recorddate");
            Timestamp timestamp = TimeUtils.ParseTimestamp(date);
            payload.put("recorddate",timestamp);
            // ip在前端接口中是没有传递的，如果不手动设置，则无法通过验证
            payload.put("ipaddress", Maputil.GetIp(request));
            boolean Valitation = Maputil.MapValiType(payload, LogMessage.class);
            if (!Valitation)
                return new Response<>(Coco.ParamsError);

            boolean Null = Maputil.MapNotNull(payload,LogMessage.class);
            if (!Null)
                return new Response<>(Coco.ParamsNullError);

        } catch (ParseException e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsTypeError);
        }

        LogMessage log;
        try {
            log = Maputil.MapToObject(payload, LogMessage.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsTypeError);
        }
        if (log == null)
            return new Response<>(Coco.ParamsError);

        String[] tag = new String[]{"trace", "debug", "info", "warn", "error", "fatal"};
        int i = 0;
        for (String s : tag) {
            if (s.equals(log.getLevel())){
                i++;
            }
        }
        if (i == 0)
            return new Response<>(Coco.LogTypeError);

        try {
            this.rocket.Send(Topic,log.getLevel(),log);
            return new Response<>();
        }catch (Exception e) {
            return new Response<>(Coco.ServerError);
        }
    }


    @GetMapping("/findall")
    public Response SelectAll(@RequestParam("from") Integer from,
                              @RequestParam("to") Integer to){
        try {
            List<Log> findall =
                    this.logDevelopDaoService.findall(PageRequest.of(from, to));
            return new Response<>(findall);
        }catch (Exception e){
            e.printStackTrace();
            return new Response<>(Coco.ServerError);
        }
    }

    @PostMapping ("/search/term")
    public Response SelectesTrem(@RequestBody Map<String, Object> maps) {

        int size;
        String value;
        Map<String, String> payloads;
        String filed = "";
        String rule = "";
        try {
            size = maps.get("size") == null ?
                    20 : (Integer) maps.get("size");
            Object payload = maps.get("payload");
            if (payload instanceof Map) {
                payloads = (Map<String, String>) payload;
                filed = payloads.get("filed");
                value = payloads.get("value");
                rule = payloads.get("rule");
            } else {
                return new Response<>(Coco.ParamsTypeError);
            }
        } catch (ClassCastException e) {
            return new Response<>(Coco.ParamsTypeError);
        }

        if (!filed.equals("")  && !value.equals("")) {
            try {
                boolean b = Maputil.MapExistsBean(filed,LogMessage.class);
                if (b) {
                    return this.logDevelopDaoService.SearchTrem(filed,value,rule,size);
                }
                throw new RuntimeException();
            }catch (Exception e){
                return new Response<>(Coco.ServerError);
            }
        } else {
            return new Response<>(Coco.ParamsNumError);
        }
    }

    /**
     * 模糊查询
     * @param maps
     * @return
     */
    @SuppressWarnings ("unchecked")
    @PostMapping ("/search/like")
    public Response Selectes(@RequestBody Map<String, Object> maps) {

        int size;
        String value;
        Map<String, String> payloads = null;
        String filed;
        try {
            size = maps.get("size") == null ?
                    20 : (Integer) maps.get("size");
            Object payload = maps.get("payload");
            if (payload instanceof Map) {
                payloads = (Map<String, String>) payload;
                filed = payloads.get("filed");
                value = payloads.get("value");
            } else {
                return new Response<>(Coco.ParamsTypeError);
            }
        } catch (ClassCastException e) {
            return new Response<>(Coco.ParamsTypeError);
        }

        if (!filed.equals("")  && !value.equals("")) {
            try {
                List<Log> searchlike = this.logDevelopDaoService.Searchlike(filed, value, size);
                return new Response<>(searchlike);
            }catch (Exception e){
                return new Response<>(Coco.ServerError);
            }
        } else {
            return new Response<>(Coco.ParamsNumError);
        }
    }


    @PostMapping ("/search/likemutil")
    public Response Selectesl(@RequestBody Map<String, Object> maps) {
        Integer size = 0;
        Map<String, Object> stringObjectMap;
        try {
            Map<String,Object> payload;
            size = (Integer) maps.get("size");
            if (maps.get("payload") instanceof Map) {
                payload = (Map<String,Object>) maps.get("payload");
                LogMessage LogMessage = Maputil.MapToObject(payload, LogMessage.class);
                stringObjectMap = Maputil.ObjectToMap(LogMessage);
            }else {
                throw new RuntimeException("error");
            }
        }catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsError);
        }


        if (stringObjectMap.size() == 0)  return new Response<>(Coco.ParamsNumError);

        try {
            return this.logDevelopDaoService.SearchlikeMutil(stringObjectMap, size);
        }catch (Exception e){
            e.printStackTrace();
            return new Response<>(Coco.ParamsError);
        }
    }




}
