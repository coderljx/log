package com.example.API.SJ;

import com.alibaba.fastjson.JSON;
import com.example.API.Log.LogMessage;
import com.example.Pojo.Model;
import com.example.Pojo.comptroller;
import com.example.Pojo.comptrollerReturn;
import com.example.Run.Excel;
import com.example.Run.Rocket;
import com.example.Service.ComptrollerService;
import com.example.Utils.*;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 审计服务API
 */
@RestController
@RequestMapping("/sj")
public class SJAPI {
    private final static Logger mylog = LoggerFactory.getLogger(SJAPI.class);
    private final Rocket rocket;
    private final ComptrollerService comptrollerService;
    private final String Topic = "sj";

    @Autowired()
    public SJAPI(Rocket rocket,
                 ComptrollerService comptrollerService){
        this.rocket = rocket;
        this.comptrollerService = comptrollerService;
    }

    @GetMapping("/model")
    public Response<?> model() throws Exception{
        List<Model> list = this.comptrollerService.GetModel();
        return new Response<>(list);
    }



    @PostMapping("/config/setting")
    public Response<?> Config(@RequestBody Map<String,Object> maps,
                            HttpServletRequest request){
        Map<String, Object> payload = (Map<String, Object>) maps.get("payload");
        if (payload == null)
            return new Response<>(Coco.ParamsError);

        SjMessage log;
        try {
            String date = (String) payload.get("recorddate");
            Timestamp timestamp = TimeUtils.ParseTimestamp(date);
            payload.put("recorddate",timestamp);
            payload.put("ipaddress", Maputil.GetIp(request));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsTypeError);
        }

        try {
            boolean Validation = Maputil.MapValiType(payload, SjMessage.class);
            if (!Validation)
                return new Response<>(Coco.ParamsError);

            boolean b = Maputil.MapNotNull(payload, SjMessage.class);
            if (!b)
                return new Response<>(Coco.ParamsNullError);

            log = Maputil.MapToObject(payload, SjMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsError);
        }

        if (log == null)
            return new Response<>(Coco.ParamsError);

        try {
            this.rocket.Send(Topic,"config",log);
            return new Response<>();
        }catch ( Exception e){
            return new Response<>(Coco.ServerError);
        }

    }


    /**
     * 审计服务多条件查询
     * @param maps
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/search/likemutil")
    public  Response<?> searchEsotoMutil(@RequestBody Map<String,Object> maps)   {
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
            if (!searchArgsMap.MapToOrder(SjMessage.class)) throw new RuntimeException();
            SearchArgs.ArgsItem argsItem = searchArgsMap.getArgsItem();
            SearchArgs.Order order1 = searchArgsMap.getOrder();
            Response< Map<String, Object> > mapResponse = this.comptrollerService.SearchMutilLog(argsItem, order1, per_page, curr_page);
            return mapResponse;
        }  catch (ParseException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsTypeError);
        } catch (ExceptionInInitializerError e) {
            e.printStackTrace();
            return new Response<>(Coco.IndexNameNotFound);
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/export")
    public void ExportExcel(@RequestBody Map<String,Object> maps, HttpServletResponse response){
//        List<comptrollerReturn> result;
//        try {
//            Map<String,Object> maps1 = (Map<String, Object>) maps.get("args");
//            int per_page = (int) maps1.get("per_page");
//            int curr_page = (int) maps1.get("curr_page");
//            Map<String,Object> filters = (Map<String, Object>) maps1.get("filters");
//            Map<String,Object> order = (Map<String, Object>) maps1.get("order");
//            SearchArgsMap searchArgsMap = new SearchArgsMap(filters,order);
//            // 解析查询参数
//            if (!searchArgsMap.MapTpArgsItem())  throw new RuntimeException();
//            // 解析排序方式
//            if (!searchArgsMap.MapToOrder(SjMessage.class)) throw new RuntimeException();
//
//            SearchArgs.ArgsItem argsItem = searchArgsMap.getArgsItem();
//            SearchArgs.Order order1 = searchArgsMap.getOrder();
//            result = this.comptrollerService.SearchMutilLog(argsItem, order1, per_page, curr_page).getData();
//            ServletOutputStream outputStream = response.getOutputStream();
//            response.setHeader("Content-Disposition", "attachment;fileName=" + "1.xls");// 设置文件名
//            response.setHeader("content-type", "application/octet-stream;charset=UTF-8");
//            Workbook work = Excel.CreateHeader("审计", "2", "s", "s", "3", "s");
//            if (result != null) {
//                for (comptrollerReturn comptrollerReturn : result) {
//                    Excel.CreateData(
//                            comptrollerReturn.getAppid(),
//                            comptrollerReturn.getContent(),
//                            comptrollerReturn.getAppname(),
//                            comptrollerReturn.getAuditcontent(),
//                            comptrollerReturn.getDescription(),
//                            comptrollerReturn.getMethod(),
//                            comptrollerReturn.getOrgname(),
//                            comptrollerReturn.getOrgid(),
//                            comptrollerReturn.getStatus()
//                    );
//                }
//            }
//            work.write(outputStream);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }

    }




}
