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
     * 生产消息
     * @param maps
     */
    @PostMapping ("/create")
    @SuppressWarnings ({"unchecked"})
    public Response<?> create (@RequestBody Map<String, Object> maps,
                            HttpServletRequest request){
        Map<String, Object> payload = (Map<String, Object>) maps.get("payload");
        if (payload == null)  return new Response<>(Coco.ParamsError);

        final String[] tag1 = {"trace", "info", "warn", "error", "fatal"};
        final String[] tag =  {"正常","轻微","一般","严重","非常严重"};
        try {
            String date = (String) payload.get("recorddate");
            Timestamp timestamp = TimeUtils.ParseTimestamp(date);
            payload.put("recorddate",timestamp);
            // ip在前端接口中是没有传递的，如果不手动设置，则无法通过验证
            payload.put("ipaddress", Maputil.GetIp(request));
            boolean Valitation = Maputil.MapValiType(payload, LogMessage.class);
            if (!Valitation) return new Response<>(Coco.ParamsError);

            boolean Null = Maputil.MapNotNull(payload,LogMessage.class);
            if (!Null) return new Response<>(Coco.ParamsNullError);

            LogMessage log;
            log = Maputil.MapToObject(payload, LogMessage.class);
            if (log == null) return new Response<>(Coco.ParamsError);

            int i = -1;
            String level = log.getLevel();
            for (int i1 = 0; i1 < tag.length; i1++) {
                if (tag[i1].equals(level)){
                    i = i1;
                    break;
                }
            }
            if (i == -1) return new Response<>(Coco.LogTypeError);
            try {
                this.rocket.Send(Topic,tag1[i],log);
                return new Response<>();
            }catch (Exception e) {
                e.printStackTrace();
                return new Response<>(Coco.ServerError);
            }
        } catch (ParseException | IllegalAccessException e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsTypeError);
        }
    }



    /**
     * 获取日志的查询条件
     * @return
     */
    @GetMapping("/model")
    @SuppressWarnings ({"unchecked"})
    public Response<?> Model(){
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
   // @PostMapping ("/search/likemutil")
    @SuppressWarnings ({"unchecked"})
    public Response<?> Selectesl(@RequestBody Map<String,Object> maps) {
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


    @PostMapping ("/search/likemutil")
    public Response<?> SelecteAll(@RequestBody Map<String,Object> maps) {
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

            Response<Map<String, Object>> search = this.logDevelopDaoService.search(argsItem, order1, per_page, curr_page);
            return search;
        }catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsError);
        }

    }



}
