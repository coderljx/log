package com.example.Service;


import com.example.Dao.LogDao;
import com.example.ES.LogES;
import com.example.Pojo.Log;
import com.example.Pojo.LogReturn;
import com.example.Pojo.Model;
import com.example.Run.Email;
import com.example.Run.EmailProperties;
import com.example.Run.EsTemplate;
import com.example.Run.Redis;
import com.example.Utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class LogDaoService {
    private final Logger mylog = LoggerFactory.getLogger(LogDaoService.class);
    private final LogDao logDevelopDao;
    private final EsTemplate esTemplate;
    private final LogES logES;
    private final Redis redis;
    private final Email email;
    private final EmailProperties emailProperties;


    @Autowired(required = false)
    public LogDaoService(LogDao logDevelopDao,
                         EsTemplate esTemplate,
                         EmailProperties emailProperties,
                         LogES logES,
                         Redis redis,
                         Email email){
        this.logDevelopDao = logDevelopDao;
        this.esTemplate = esTemplate;
        this.emailProperties = emailProperties;
        this.logES = logES;
        this.redis = redis;
        this.email = email;
    }


    /**
     * 写入到数据库和es中
     * @param logOperation
     */
    public void InsertDB(Log logOperation){
        if (logOperation != null){
            try {
               synchronized (this){
                   this.logDevelopDao.Insertlog(logOperation);
                   Log logOperation1 = this.logDevelopDao.SelectByid(this.logDevelopDao.Maxid());
                   this.logES.save(logOperation1);
               }
            }catch (Exception e){
                e.printStackTrace();
                this.redis.InsertFail(logOperation);
            }
        }
    }

    public Response<List<LogReturn>>findall(PageRequest request) throws Exception {
        SearchHits<Log> searchHits = this.esTemplate.SearchAll(request, Log.class);
        return this.Parse(searchHits);
    }

    public Response<List<LogReturn>> SearchTrem(String filed,  String rule , int size,int page,List<String> value) throws Exception {
        int num = value.size();
        if (num > 2) return null;

        if (!filed.equals("recorddate"))
        {
            if (num == 1){
                String Newfiled = Maputil.ReplaceAddKeyword(filed);
                String values = value.get(0);
                return this.Parse(this.esTemplate.SearchTerm(Newfiled, values, size, page, Log.class));
            }
        }

        long parselong = 0L;
        long parselongend = 0L;
        if (num == 1) {
            parselong = TimeUtils.Parselong(value.get(0));
        }
        if (num == 2) {
            parselong = TimeUtils.Parselong(value.get(0));
            parselongend = TimeUtils.Parselong(value.get(1));
        }
        PageRequest of = PageRequest.of(0, size);
        List<Log> logs = new ArrayList<>();

        SearchHits<Log> searchHits = this.esTemplate.SearchRange(filed, parselong, parselongend, rule, size, page, Log.class);
        Response<List<LogReturn>> parse = this.Parse(searchHits);
        return parse;
    }

    public Response<List<LogReturn>> Searchlike(String filed, String rule, int size,int page,List<String> value) throws Exception {
        String values = "";
        int num = value.size();
        if (num == 1) {
            values = value.get(0);
            SearchHits searchHits = this.esTemplate.SearchLike(filed, values, size,page, Log.class);
            return this.Parse(searchHits);
        }
 
        long parselong = 0L;
        long parselongend = 0L;
        if (num == 2) {
            parselong = TimeUtils.Parselong(value.get(0));
            parselongend = TimeUtils.Parselong(value.get(1));
        }
        if (filed.equals("recorddate")) {
            SearchHits<Log> searchHits = this.esTemplate.SearchRange(filed, parselong, parselongend, rule, size, page, Log.class);
            Response<List<LogReturn>> parse = this.Parse(searchHits);
            return parse;
        }
        return new Response<>(null);
    }

    public Response<List<LogReturn>> SearchlikeMutil(Map<String,Object> maps, int size,int page) throws Exception {
        SearchHits<Log> searchHits = this.esTemplate.SearchLikeMutil2(maps, size, page,Log.class);
        return this.Parse(searchHits);
    }

    /**
     * 发送邮件
     */
    public void Emails() {
        String[] objects = (String[]) emailProperties.getTo().toArray();
        email.SetPerson(emailProperties.getFrom(),objects);
        Map<String, String> maps = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String time = dateFormat.format(new Date());
        maps.put("msg", "Brother");
        maps.put("link", "https://baidu.com");
        maps.put("time", time);

        email.SetContent("系统异常", true,
                email.ReplaceParams("SystemError.html", (maps)));
        email.Send();
    }



    public Response<List<LogReturn>> SearchMutilLog(SearchArgs.ArgsItem argsItem,SearchArgs.Order order,int per_page,int curr_page) throws Exception {
        // 查询所有数据
        if (argsItem.getType() == null && argsItem.getChildren() == null){
            PageRequest of = PageRequest.of(curr_page, per_page);
            Response<List<LogReturn>> findall = this.findall(of);
            return findall;
        }

        SearchHits<Log> searchHits = this.esTemplate.SearchLikeMutil3(argsItem, order, per_page, curr_page, Log.class);
        Response<List<LogReturn>> parse = this.Parse(searchHits);
        return parse;
    }

    private Response<List<LogReturn>> Parse(SearchHits<Log> searchHits) throws Exception {
        List<SearchHit<Log>> searchHits1 = searchHits.getSearchHits();
        long totalHits = searchHits.getTotalHits();
        List<LogReturn> datas = new ArrayList<>();
        for (SearchHit<Log> comptrollerSearchHit : searchHits1) {
            Log content = comptrollerSearchHit.getContent();
            Date recorddate = content.getRecorddate();
            LogReturn logReturn = Maputil.BeanToBean(content, new LogReturn());
            String newdate = TimeUtils.ParseDate(recorddate);
            logReturn.setRecorddate(newdate);
            datas.add(logReturn);
        }
        return new Response<>(datas, Math.toIntExact(totalHits));
    }


    public List<Model> Moudel() throws Exception {
        Model time = ModelReturn.Time();
        Model level = this.Level();
        List<Model> list = new ArrayList<>();
        list.add(time);
        list.add(level);
        return list;
    }

    /**
     * 设置log页面的等级
     * @return
     */
    private Model Level(){
        Model model = new Model();
        model.setField("level");
        model.setLabel("=");
        model.setOperator("严重等级");
        model.setType("checkbox");
        model.setDatatype("string");
        model.setCanInput("no");
        List<Model.label> labelList = new ArrayList<>();
        String[] values = new String[]{"正常","轻微","一般","严重","非常严重"};
        for (String value : values) {
            Model.label label = new Model.label();
            label.setLabel(value);
            label.setValue(value);
            labelList.add(label);
        }
        model.setOtions(labelList);
        return model;
    }




}
