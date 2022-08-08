package com.example.Service;

import com.example.Dao.comptrollerDao;
import com.example.Pojo.comptroller;
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
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ComptrollerService {
    private final Logger mylog = LoggerFactory.getLogger(ComptrollerService.class);
    private final com.example.Dao.comptrollerDao comptrollerDao;
    private final com.example.ES.comptrollerES comptrollerES;
    private final EsTemplate esTemplate;
    private final Redis redis;

    @Autowired
    public ComptrollerService(comptrollerDao comptrollerDao,
                              com.example.ES.comptrollerES comptrollerES,
                              Redis redis,
                              EsTemplate esTemplate) {
        this.comptrollerDao = comptrollerDao;
        this.comptrollerES = comptrollerES;
        this.redis = redis;
        this.esTemplate = esTemplate;
    }

    public List<comptroller> tt(String value){
        PageRequest of = PageRequest.of(0, 10);

        //1658999038000L
        return comptrollerES.findByRecorddate(1658999038000L,of);
    }


    /**
     * 向数据库和es中写入数据
     * @param comptroller
     * @return
     */
    public boolean Insertsj(comptroller comptroller) {
        if (comptroller == null)
            return  false;

        try {
            synchronized (this){
                Integer integer = this.comptrollerDao.InsertSJ(comptroller);
                com.example.Pojo.comptroller comptroller1 = this.comptrollerDao.SelectByid(this.comptrollerDao.SelectMaxid());
                this.comptrollerES.save(comptroller1);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.redis.InsertFail(comptroller);
            return false;
        }
    }

    public Response findall(int from,int to){
        PageRequest request = PageRequest.of(from, to);
        SearchHits<comptroller> searchHits = this.esTemplate.SearchAll(request, comptroller.class);
        return  this.Parse(searchHits);
    }


    public Response<List<comptroller>> searchEsLike (String filed,List<String> value,String rule,int page,int size) throws ParseException {
        if (!filed.equals("recorddate")){
            SearchHits<comptroller> searchHits = esTemplate.SearchLike(filed, value.get(0), size,page,comptroller.class);
            Response<List<comptroller>> parse = this.Parse(searchHits);
            return parse;
        }

        long start = 0L;
        long end = 0L;
        if (value.size() == 1) {
            start = TimeUtils.Parselong(value.get(0));
        }
        if (value.size() > 1){
           start = TimeUtils.Parselong(value.get(0));
           end = TimeUtils.Parselong(value.get(1));
        }
        SearchHits<comptroller> searchHits = esTemplate.SearchRange(filed, start, end, rule, size, page, comptroller.class);
        Response<List<comptroller>> parse = this.Parse(searchHits);
        return parse;
    }

    public Response searchEsLikeMutile(Map<String,Object> maps, int size,int page){
        SearchHits<comptroller> searchHits = esTemplate.SearchLikeMutil2(maps, size,page, comptroller.class);
        return this.Parse(searchHits);
    }

    public Response SearchTerm(String filed, List<String> value, String rule,int size, int page) throws ParseException {
        List<comptroller> logs = null;
        int num = value.size();
        if (num == 1){
            String key = Maputil.ReplaceAddKeyword(filed);
            SearchHits<comptroller> searchHits = esTemplate.SearchTerm(key, value.get(0), size, page, comptroller.class);
            return this.Parse(searchHits);
        }

        long parselong = 0L;
        long parselongend = 0L;
        Response<List<comptroller>> result;
        if (num == 2) {
            parselong = TimeUtils.Parselong(value.get(0));
            parselongend = TimeUtils.Parselong(value.get(1));
        }
        PageRequest of = PageRequest.of(size,page);
        if (filed.equals("recorddate")){
            if (rule.equals("="))
                logs = this.comptrollerES.findByRecorddate(parselong,of);

            if (rule.equals(">=") || rule.equals(">"))
                logs = this.comptrollerES.findByRecorddateAfter(parselong,of);

            if (rule.equals("<=") || rule.equals("<") )
                logs = this.comptrollerES.findByRecorddateBefore(parselong,of);

            if (parselongend != 0L)
                logs = this.comptrollerES.findByRecorddateBetween(parselong,parselongend ,of);
        }
        result = new Response<>(logs);
        return result;
    }


    private Response Parse(SearchHits<comptroller> searchHits){
        List<SearchHit<comptroller>> searchHits1 = searchHits.getSearchHits();
        long totalHits = searchHits.getTotalHits();
        List<comptroller> datas = new ArrayList<>();
        for (SearchHit<comptroller> comptrollerSearchHit : searchHits1) {
            datas.add(comptrollerSearchHit.getContent());
        }
        return new Response<>(datas,Math.toIntExact(totalHits));
    }



}
