package com.example.API.Log;


import com.example.Pojo.Model;
import com.example.Run.Rocket;
import com.example.Service.LogDaoService;
import com.example.Utils.*;
import jdk.nashorn.internal.runtime.regexp.joni.constants.CCSTATE;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping ("/log")
public class LogAPI {
    private final Logger mylog = LoggerFactory.getLogger(LogAPI.class);
    private final Rocket rocket;
    private final LogDaoService logDevelopDaoService;


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
        Coco coco = null;
        Response<?> response = null;
        Map<String, Object> payload = (Map<String, Object>) maps.get("payload");
        if (payload == null)  {
            throw new TypeException("请求参数不可为空");
        }

        final String[] tag1 = {"trace", "info", "warn", "error", "fatal"};
        final String[] tag =  {"正常","轻微","一般","严重","非常严重"};

        try {
            String date = (String) payload.get("recorddate");
            Timestamp timestamp =  TimeUtils.ParseTimestamp(date);
            payload.put("recorddate",timestamp);
            payload.put("ipaddress", Maputil.GetIp(request));
            Maputil.MapValiType(payload, LogMessage.class);
            Maputil.MapNotNull(payload,LogMessage.class);

            LogMessage log;
            log = Maputil.MapToObject(payload, LogMessage.class);
            if (log == null) {
                throw new TypeException("转换错误");
            }
            int i = -1;
            String level = log.getLevel();
            for (int i1 = 0; i1 < tag.length; i1++) {
                if (tag[i1].equals(level)){
                    i = i1;
                    break;
                }
            }
            if (i == -1){
                throw new TypeException("日志等级超出系统规范");
            }
            String topic = "log";
            this.rocket.Send(topic,tag1[i],log);
            coco = Coco.ok;
        } catch (ParseException e) {
            e.printStackTrace();
            coco.message = e.getMessage();
            coco.code = -101;
        } catch (TypeException typeException){
            coco.message = typeException.getMessage();
            coco.code = -100;
        } catch (Exception e) {
            coco = Coco.ServerError;
        } finally {
            response = new Response<>(coco);
        }
        return response;
    }



    /**
     * 获取日志的查询条件
     * @return
     */
    @GetMapping("/model")
    public Response<?> Model(){
        Coco coco = null;
        List<Model> moudel = null;
        Response<?> response = null;
        try {
            moudel = this.logDevelopDaoService.Moudel();
        } catch (Exception e) {
            e.printStackTrace();
           coco.message = e.getMessage();
           coco.code = -102;
        }finally {
            response = new Response<>(coco,moudel);
        }
        return response;
    }


    /**
     * 查询出结果 只显示系统名称等信息
     * @param maps
     * @return
     */
    @PostMapping ("/search/likemutil")
    @SuppressWarnings ({"unchecked"})
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

            Response<Map<String, Object>> search = this.logDevelopDaoService.SearchMutilLog(argsItem, order1, per_page, curr_page);
            return search;
        }catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsError);
        }
    }


    /**
     * 根据查询出的id 删除对应的数据
     * @param maps
     * @return
     */
    @PostMapping ("/delete/likemutil")
    @SuppressWarnings ({"unchecked"})
    public Response<?> delete(@RequestBody Map<String,Object> maps) {
        Response<?> response = null;
        Coco coco = null;
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

            this.logDevelopDaoService.delete(argsItem,order1,per_page,curr_page);
            coco = Coco.ok;
        } catch (Exception e) {
            e.printStackTrace();
            coco = Coco.ParamsError;
        } finally {
           response = new Response<>(coco);
        }
        return response;
    }

    /**
     * 导出excel
     * @param request
     * @param response
     */
    @PostMapping("/export")
    @SuppressWarnings ({"unchecked"})
    public void export(@RequestBody Map<String,Object> maps,HttpServletRequest request, HttpServletResponse response) {
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

            response.setHeader("Content-Disposition", "attachment;fileName=" + "1.xls");// 设置文件名
            response.setHeader("content-type", "application/vnd.ms-excel;charset=UTF-8");
            Workbook export = this.logDevelopDaoService.export(argsItem, order1, per_page, curr_page);
            export.write(response.getOutputStream());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }




}
