package com.example.Service;

import com.example.Dao.comptrollerDao;
import com.example.Pojo.Model;
import com.example.Pojo.comptroller;
import com.example.Pojo.comptrollerReturn;
import com.example.Run.EsTemplate;
import com.example.Run.Redis;
import com.example.Utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
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

    public Response<List<comptrollerReturn>> findall(int size,int page) throws Exception {
        PageRequest request = PageRequest.of(size, page);
        SearchHits<comptroller> searchHits = this.esTemplate.SearchAll(request, comptroller.class);
        return  this.Parse(searchHits);
    }

    public Response<List<comptrollerReturn>> SearchMutilLog(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page) throws Exception {
        if (argsItem.getType() == null && argsItem.getChildren() == null) {
            Response<List<comptrollerReturn>> findall = this.findall(curr_page, per_page);
            return findall;
        }

        List<SearchArgs.Condition> children = argsItem.getChildren();
        for (SearchArgs.Condition child : children) {
            if (child.getField().equals("moudel") && child.operator.equals("in")){
                if (child.getValues().get(0).equals("")){
                    List<String> list = this.comptrollerDao.GetMoudel();
                    child.setValues(list);
                }
            }
        }
        SearchHits<comptroller> searchHits = this.esTemplate.SearchLikeMutil3(argsItem, order, per_page, curr_page, comptroller.class);
        Response<List<comptrollerReturn>> parse = this.Parse(searchHits);
        return parse;
    }

    public Response<List<comptrollerReturn>> searchEsLike (String filed, List<String> value, String rule, int page, int size) throws Exception {
        if (!filed.equals("recorddate")){
            SearchHits<comptroller> searchHits = esTemplate.SearchLike(filed, value.get(0), size,page,comptroller.class);
            Response<List<comptrollerReturn>> parse = this.Parse(searchHits);
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
        Response<List<comptrollerReturn>> parse = this.Parse(searchHits);
        return parse;
    }

    public Response searchEsLikeMutile(Map<String,Object> maps, int size,int page) throws Exception {
        SearchHits<comptroller> searchHits = esTemplate.SearchLikeMutil2(maps, size,page, comptroller.class);
        return this.Parse(searchHits);
    }

    public Response SearchTerm(String filed, List<String> value, String rule,int size, int page) throws Exception {
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


    private Response<List<comptrollerReturn>> Parse(SearchHits<comptroller> searchHits) throws Exception {
        List<SearchHit<comptroller>> searchHits1 = searchHits.getSearchHits();
        long totalHits = searchHits.getTotalHits();
        List<comptrollerReturn> datas = new ArrayList<>();
        for (SearchHit<comptroller> comptrollerSearchHit : searchHits1) {
            comptroller content = comptrollerSearchHit.getContent();
            Date Navicat = content.getRecorddate();
            comptrollerReturn comptroller = Maputil.BeanToBean(content, new comptrollerReturn());
            String newdate = TimeUtils.ParseDate(Navicat);
            comptroller.setRecorddate(newdate);
            datas.add(comptroller);
        }
        return new Response<>(datas, Math.toIntExact(totalHits));
    }




    public List<Model> GetModel() throws Exception {
        Model time = ModelReturn.Time();
        Model model = new Model();
        model.setField("model");
        model.setLabel("eq");
        model.setOperator("操作模块");
        model.setType("checkbox");
        model.setDatatype("string");
        model.setCanInput("no");
        List<String> list = this.comptrollerDao.GetMoudel();
        list.add("全部");
        List<Model.label> labelList = new ArrayList<>();
        for (String s : list) {
            Model.label label = new Model.label();
            if (s.equals("全部")){
                label.setLabel(s);
                label.setValue("");
            }else {
                label.setLabel(s);
                label.setValue(s);
            }
            labelList.add(label);
        }
        model.setOtions(labelList);
        List<Model> modelList = new ArrayList<>();
        modelList.add(time);
        modelList.add(model);
        return modelList;
    }







}
