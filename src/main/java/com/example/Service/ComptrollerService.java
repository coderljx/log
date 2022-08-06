package com.example.Service;

import com.example.Dao.comptrollerDao;
import com.example.Pojo.comptroller;
import com.example.Run.EsTemplate;
import com.example.Run.Redis;
import com.example.Utils.Maputil;
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

    public List<comptroller> findall(int from,int to){
//        new Sort().setSort(new SortField("createdate", SortField.Type.DOC));
        return comptrollerES.findAllBy(PageRequest.of(from, to));
    }


    public List<comptroller> searchEsLike (String filed,String value,int size){
        SearchHits<comptroller> searchHits = esTemplate.SearchLike(filed, value, size,comptroller.class);
        List<SearchHit<comptroller>> searchHits1 = searchHits.getSearchHits();
        List<comptroller> datas = new ArrayList<>();
        for (SearchHit<comptroller> comptrollerSearchHit : searchHits1) {
            datas.add(comptrollerSearchHit.getContent());
        }
        return datas;
    }

    public List<comptroller> searchEsLikeMutile(Map<String,Object> maps, int size){
        SearchHits<comptroller> searchHits = esTemplate.SearchLikeMutil2(maps, size, comptroller.class);
        return this.Parse(searchHits);
    }

    public List<comptroller> SearchTerm (String filed,String value, String rule, int size) throws ParseException {
//        SearchHits<comptroller> searchHits = esTemplate.SearchTerm(filed,value,size,comptroller.class);
//        return this.Parse(searchHits);
        List<comptroller> byMessage = null;
        PageRequest of = PageRequest.of(0, size);

        String key = Maputil.ReplaceAddKeyword(filed);
        SearchHits<comptroller> searchHits = esTemplate.SearchTerm(key, value, size, comptroller.class);
        byMessage = this.Parse(searchHits);

        return byMessage;
    }


    private List<comptroller> Parse(SearchHits<comptroller> searchHits){
        List<SearchHit<comptroller>> searchHits1 = searchHits.getSearchHits();
        List<comptroller> datas = new ArrayList<>();
        for (SearchHit<comptroller> comptrollerSearchHit : searchHits1) {
            datas.add(comptrollerSearchHit.getContent());
        }
        return datas;
    }



}
