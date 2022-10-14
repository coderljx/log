package com.example.API.SJ;

import com.example.Pojo.Model;
import com.example.Run.Rocket;
import com.example.Service.ComptrollerService;
import com.example.Utils.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
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



    @PostMapping("/create")
    @SuppressWarnings("unchecked")
    public Response<?> Config(@RequestBody Map<String,Object> maps,
                            HttpServletRequest request){
        Coco coco = null;
        Response<?> response = null;
        Map<String, Object> payload = (Map<String, Object>) maps.get("payload");
        if (payload == null){
            throw new TypeException("请求参数不可为空");
        }
        try {
            String date = (String) payload.get("recorddate");
            Timestamp timestamp = TimeUtils.ParseTimestamp(date);
            payload.put("recorddate",timestamp);
            payload.put("ipaddress", Maputil.GetIp(request));
            Maputil.MapValiType(payload, SjMessage.class);

            Maputil.MapNotNull(payload, SjMessage.class);
            SjMessage log = Maputil.MapToObject(payload, SjMessage.class);
            this.rocket.Send(Topic,"config",log);

            this.rocket.AsyncSend(Topic,"config",log,new SendCallback(){
                @Override
                public void onSuccess(SendResult sendResult) {
                }
                @Override
                public void onException(Throwable throwable) {
                    throw new TypeException(throwable.getMessage());
                }
            });


            coco = Coco.ok;
        } catch (ParseException | IllegalAccessException e) {
            e.printStackTrace();
            coco.message = e.getMessage();
            coco.code = -101;
        } catch (TypeException typeException){
            coco.message = typeException.getMessage();
            coco.code = -102;
        }catch ( Exception e){
           coco = Coco.ServerError;
        }finally {
            response = new Response<>(coco);
        }
        return response;
    }


    /**
     * 审计服务多条件查询
     * @param maps
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/search/likemutil")
    public  Response<?> searchEsotoMutil(@RequestBody Map<String,Object> maps)   {
        Map<String, Object>  mapResponse = null;
        Coco coco = null;
        Response<?> response = null;
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
            mapResponse = this.comptrollerService.SearchMutisj(argsItem, order1, per_page, curr_page);
        }  catch (ParseException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            coco = Coco.ParamsTypeError;
        } catch (ExceptionInInitializerError e) {
            e.printStackTrace();
            coco = Coco.IndexNameNotFound;
        } finally {
            response = new Response<>(coco,mapResponse);
        }
        return response;
    }


    /**
     * 审计服务下载
     * @param maps
     * @param response
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/export")
    public void ExportExcel(@RequestBody Map<String,Object> maps, HttpServletResponse response){
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

            response.setHeader("Content-Disposition", "attachment;fileName=" + "1.xls");// 设置文件名
            response.setHeader("content-type", "application/vnd.ms-excel;charset=UTF-8");
            Workbook export = this.comptrollerService.export(argsItem, order1, per_page, curr_page);
            export.write(response.getOutputStream());
        }catch (Exception e) {
            e.printStackTrace();
        }

    }




}
