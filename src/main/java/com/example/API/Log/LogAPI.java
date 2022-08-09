package com.example.API.Log;


import com.example.Pojo.Model;
import com.example.Run.Rocket;
import com.example.Service.LogDaoService;
import com.example.Utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping ("/log")
public class LogAPI {
    private final Logger mylog = LoggerFactory.getLogger(LogAPI.class);
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
     {
     "args": {

     "filters": {
     "rules": [

     {
     "type": "and",
     "children": [{
     "type": "and",
     "children": [{
     "field": "illegalLevel",
     "operator": "in",
     "values": ["一般"]
     },
     {
     "field": "createDate",
     "operator": "range",
     "value": "2022-08-07 00:00:00#2022-08-08 23:59:59"
     },
     {
     "field": "platformName",
     "operator": "in",
     "value": ["淘宝"],
     "values": ["淘宝"]
     }]

     }]


     }

     ]
     },






     "order": [{
     "field": "claimedDate",
     "order_type": "DESC"
     }],
     "search": "",
     "per_page": 20,
     "curr_page": 1
     }
     }
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

//        String[] tag = new String[]{"trace", "debug", "info", "warn", "error", "fatal"};
        String[] tag = new String[]{"正常","轻微","一般","严重","非常严重"};
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
            return this.logDevelopDaoService.findall(PageRequest.of(from, to));
        }catch (Exception e){
            e.printStackTrace();
            return new Response<>(Coco.ServerError);
        }
    }

    /**
     * 模糊/精确查询
     * @param maps
     * @return
     */
    @PostMapping ("/search/term")
    public Response SelectesTrem(@RequestBody Map<String, Object> maps) {

        int size;
        int page;
        Map<String, Object> payloads;
        String filed;
        String operat = "";
        List<String> value;
        String[] opear = new String[]{"=",">","<",">=","<="};
        try {
            size = maps.get("size") == null ?
                    20 : (Integer) maps.get("size");
            page = maps.get("page") == null ?
                    1 : (Integer) maps.get("page");
            if (!(maps.get("payload") instanceof Map)) return new Response<>(Coco.ParamsTypeError);

            payloads = (Map<String, Object>) maps.get("payload");
            filed = (String) payloads.get("filed");
            boolean Valifiled = Maputil.MapExistsBean(filed, LogMessage.class);
            if (!Valifiled) return new Response<>(Coco.ParamsTypeError);

            if (!(payloads.get("rule") instanceof Map)) return new Response<>(Coco.ParamsTypeError);

            Map<String,Object> rule = (Map<String, Object>) payloads.get("rule");
            operat = Objects.equals(rule.get("operat"), "") ? "=" : (String) rule.get("operat");
            value = (List<String>) rule.get("value");
        } catch (ClassCastException e) {
            return new Response<>(Coco.ParamsTypeError);
        }


        try {
            boolean b = Maputil.MapExistsBean(filed, LogMessage.class);
            if (!b) return new Response<>(Coco.ParamsNumError);

            if (operat.equals("=")) // 精确查询
                return this.logDevelopDaoService.SearchTrem(filed, operat, size, page, value);

            if (operat.equals("in")) // 模糊查询
                return this.logDevelopDaoService.Searchlike(filed, operat, size, page, value);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Coco.ServerError);
        }


        return new Response<>(Coco.ParamsNumError);

    }

    /**
     * 模糊查询
     * @param maps
     * @return
     */
    @SuppressWarnings ("unchecked")
//    @PostMapping ("/search/like")
    public Response Selectes(@RequestBody Map<String, Object> maps) {

        int size;
        int page;
        Map<String, Object> payloads;
        String filed;
        String operat = "";
        List<String> value;
        String[] opear = new String[]{"=",">","<",">=","<="};
        try {
            size = maps.get("size") == null ?
                    20 : (Integer) maps.get("size");
            page = maps.get("page") == null ?
                    1 : (Integer) maps.get("page");
            if (!(maps.get("payload") instanceof Map)) return new Response<>(Coco.ParamsTypeError);

            payloads = (Map<String, Object>) maps.get("payload");
            filed = (String) payloads.get("filed");
            boolean Valifiled = Maputil.MapExistsBean(filed, LogMessage.class);
            if (!Valifiled) return new Response<>(Coco.ParamsTypeError);

            if (!(payloads.get("rule") instanceof Map)) return new Response<>(Coco.ParamsTypeError);

            Map<String,Object> rule = (Map<String, Object>) payloads.get("rule");
            operat = Objects.equals(rule.get("operat"), "") ? "=" : (String) rule.get("operat");
            value = (List<String>) rule.get("value");
        } catch (ClassCastException e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsTypeError);
        }


        if (Arrays.asList(opear).contains(operat)){
            try {
                return  this.logDevelopDaoService.Searchlike(filed, operat, size , page, value);
            }catch (Exception e){
                e.printStackTrace();
                return new Response<>(Coco.ServerError);
            }
        }
        return new Response<>(Coco.ParamsError);
    }


    @GetMapping("/model")
    public Response Model(){
        try {
            List<Model> moudel = this.logDevelopDaoService.Moudel();
            return new Response<>(moudel);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsTypeError);
        }
    }

    /**
     * 多条件查询
     */
    @PostMapping ("/search/likemutil")
    public Response Selectesl(@RequestBody Map<String,Object> maps) {
        try {
            Map<String,Object> maps1 = (Map<String, Object>) maps.get("args");
            int per_page = (int) maps1.get("per_page");
            int curr_page = (int) maps1.get("curr_page");
            Map<String,Object> filters = (Map<String, Object>) maps1.get("filters");
            Map<String,Object> order = (Map<String, Object>) maps1.get("order");
            SearchArgsMap searchArgsMap = new SearchArgsMap(filters,order);
            // 解析查询参数
            if (!searchArgsMap.MapTpArgsItem())  throw new RuntimeException();
            // 解析排序方式
            if (!searchArgsMap.MapToOrder(LogMessage.class)) throw new RuntimeException();

            SearchArgs.ArgsItem argsItem = searchArgsMap.getArgsItem();
            SearchArgs.Order order1 = searchArgsMap.getOrder();
            return this.logDevelopDaoService.SearchMutilLog(argsItem,order1,per_page,curr_page);
        }catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsError);
        }
    }




}
