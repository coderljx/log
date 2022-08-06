package com.example.API.SJ;

import com.example.Pojo.comptroller;
import com.example.Run.Rocket;
import com.example.Service.ComptrollerService;
import com.example.Utils.Coco;
import com.example.Utils.Maputil;
import com.example.Utils.Response;
import com.example.Utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    @PostMapping("/")
    public Response tt(@RequestBody Map<String,Object> maps) throws Exception{
        Map<String, Object> payload = (Map<String, Object>) maps.get("payload");
        if (payload == null)
            return new Response<>(Coco.ParamsError);

        SjMessage log = null;

            String date = (String) payload.get("recorddate");
            Timestamp timestamp = TimeUtils.ParseTimestamp(date);
            payload.put("recorddate",timestamp);
            Maputil.MapValiType(payload, SjMessage.class);
        return new Response<>();
    }

    @PostMapping("/config/setting")
    public Response Config(@RequestBody Map<String,Object> maps,
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
     * 精确查询
     */
    @PostMapping("/search/oto")
    public Response searchEsoto(@RequestBody Map<String,Object> maps)  {
        if (maps.size() == 0)
            return new Response<>(Coco.ParamsNullError);

        Integer size = 0;
        String filed = "";
        String value = "";
        String rule = "";
        try {
            Map<String,String> payload;
            size = (Integer) maps.get("size");
            if (maps.get("payload") instanceof Map) {
                payload = (Map<String,String>) maps.get("payload");
                filed = payload.get("filed");
                value = payload.get("value");
                rule = payload.get("rule");
            }else {
                throw new RuntimeException("error");
            }
        }catch (Exception e) {
            return new Response<>(Coco.ParamsNumError);
        }

        if (size <= 0) size = 20;
        boolean b = Maputil.MapExistsBean(filed, SjMessage.class);
        if (!b)  return new Response<>(Coco.ParamsError);

        try {

            List<comptroller> comptrollers = this.comptrollerService.SearchTerm(filed,value,rule , size);
            return new Response<>(comptrollers);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsTypeError);
        }

    }

    @GetMapping ("/findall")
    public Response SelectAll(@RequestParam("from") Integer from,
                              @RequestParam("to") Integer to){
        try {
            List<comptroller> findall = this.comptrollerService.findall(from,to);
            return new Response<>(findall);
        }catch (Exception e){
            e.printStackTrace();
            return new Response<>(Coco.ServerError);
        }
    }

    /**
     * 模糊查询
     * @param maps
     */
    @PostMapping("/search/like")
    public Response searchEsLike(@RequestBody Map<String,Object> maps){
        Integer size = 0;
        String filed = "";
        String value = "";
        try {
            Map<String,String> payload;
            size = (Integer) maps.get("size");
            if (maps.get("payload") instanceof Map) {
                payload = (Map<String,String>) maps.get("payload");
                filed = payload.get("filed");
                value = payload.get("value");
            }else {
                throw new RuntimeException("error");
            }
        }catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsNumError);
        }

        boolean b = Maputil.MapExistsBean(filed, SjMessage.class);
        if (!b)  return new Response<>(Coco.ParamsError);

        if (size <= 0) size = 20;

        try {
            List<comptroller> comptrollers = this.comptrollerService.searchEsLike(filed, value, size);
            return new Response<>(comptrollers);
        }catch (Exception e) {
            return new Response<>(Coco.ParamsError);
        }
    }


    /**
     * 审计服务多条件查询
     * @param maps
     */
    @PostMapping("/search/likemutil")
    public Response searchEsotoMutil(@RequestBody Map<String,Object> maps){
        Integer size = 0;
        Map<String, Object> stringObjectMap;
        try {
            Map<String,Object> payload;
            size = (Integer) maps.get("size");
            if (maps.get("payload") instanceof Map) {
                payload = (Map<String,Object>) maps.get("payload");
                mylog.info(String.valueOf("payload=" + payload == null));
                SjMessage sjMessage = Maputil.MapToObject(payload, SjMessage.class);
                stringObjectMap = Maputil.ObjectToMap(sjMessage);
            }else {
                throw new RuntimeException("error");
            }
        }catch (Exception e) {
            e.printStackTrace();
            return new Response<>(Coco.ParamsNumError);
        }

        if (stringObjectMap.size() != 0){
            List<comptroller> datas =
                    this.comptrollerService.searchEsLikeMutile(stringObjectMap , size);
            return new Response<>(datas);
        }else {
            return new Response<>(Coco.ParamsError);
        }
    }





}
