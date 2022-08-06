package com.example.Service;


import com.example.Dao.LogDao;
import com.example.ES.LogES;
import com.example.Pojo.Log;
import com.example.Run.Email;
import com.example.Run.EmailProperties;
import com.example.Run.EsTemplate;
import com.example.Run.Redis;
import com.example.Utils.Maputil;
import com.example.Utils.Response;
import com.example.Utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.text.ParseException;
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

    public List<Log> findall(PageRequest request){
        return logES.findAllBy(request);
    }

    public Response<List<Log>> SearchTrem(String filed, String value, String rule , int size) throws ParseException {
        if (!filed.equals("createdate"))
        {
            String Newfiled = Maputil.ReplaceAddKeyword(filed);
            return this.Parse(this.esTemplate.SearchTerm(Newfiled,value,size, Log.class));
        }

        long parselong = TimeUtils.Parselong(value);
        PageRequest of = PageRequest.of(0, size);
//        if (rule.equals("="))
//            return this.logES.findByRecorddate(parselong,of);
//
//        if (rule.equals(">=") || rule.equals(">"))
//            return this.logES.findByRecorddateAfter(parselong,of);
//
//        if (rule.equals("<=") || rule.equals("<") )
//            return this.logES.findByRecorddateBefore(parselong,of);

//        if (rule.equals("<>"))
//            return this.logES.findByDateBetween(parselong,of);

        return null;
    }

    public List<Log> Searchlike(String filed, String value, int size){
        SearchHits searchHits = this.esTemplate.SearchLike(filed, value, size, Log.class);
        List<SearchHit<Log>> searchHits1 = searchHits.getSearchHits();
        List<Log> datas = new ArrayList<>();
        for (int i = 0; i < searchHits1.size(); i++) {
            SearchHit<Log> comptrollerSearchHit = searchHits1.get(i);
            datas.add(comptrollerSearchHit.getContent());
        }
        return datas;
    }

    public Response<List<Log>> SearchlikeMutil(Map<String,Object> maps, int size){
        SearchHits<Log> searchHits = this.esTemplate.SearchLikeMutil2(maps, size, Log.class);
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



    private Response<List<Log>> Parse(SearchHits<Log> searchHits){
        List<SearchHit<Log>> searchHits1 = searchHits.getSearchHits();
        long totalHits = searchHits.getTotalHits();
        List<Log> datas = new ArrayList<>();
        for (SearchHit<Log> comptrollerSearchHit : searchHits1) {
            datas.add(comptrollerSearchHit.getContent());
        }
        return new Response<>(datas, Math.toIntExact(totalHits));
    }




}
