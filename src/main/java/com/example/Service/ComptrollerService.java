package com.example.Service;

import com.example.Dao.comptrollerDao;
import com.example.Pojo.Model;
import com.example.Pojo.comptroller;
import com.example.Pojo.comptrollerReturn;
import com.example.Run.ESproperties;
import com.example.Run.EsTemplate;
import com.example.Utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Component
public class ComptrollerService {
    private final Logger mylog = LoggerFactory.getLogger(ComptrollerService.class);
    private final com.example.Dao.comptrollerDao comptrollerDao;
    private final EsTemplate esTemplate;
    private final ExecutorService executorService;
    private final ESproperties eSproperties;

    @Value("${es.per.sj}")
    private String index;

    @Autowired
    public ComptrollerService(comptrollerDao comptrollerDao,
                              EsTemplate esTemplate,
                              ExecutorService executorService,
                              ESproperties eSproperties) {
        this.comptrollerDao = comptrollerDao;
        this.esTemplate = esTemplate;
        this.executorService = executorService;
        this.eSproperties = eSproperties;
    }

    /**
     * 向数据库和es中写入数据
     *
     * @param comptroller
     * @return
     */
    public boolean Insertsj(comptroller comptroller) {
        if (comptroller == null)
            return false;

        try {
            this.comptrollerDao.InsertSJ(comptroller);
            com.example.Pojo.comptroller comptroller1 = this.comptrollerDao.SelectByid(this.comptrollerDao.SelectMaxid());
            esTemplate.InsertDocument(eSproperties.currenTime(index), comptroller1);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Response<Map<String,Object>> findall(int size, int page,SearchArgs.Order order  , String indexName) throws java.text.ParseException, java.lang.IllegalAccessException  {
        PageRequest request = PageRequest.of(size, page);
        SearchHits<comptroller> searchHits = this.esTemplate.SearchAll(request, comptroller.class, order, indexName);
        return new Response<>(this.Parse(searchHits));
    }

    public Response<Map<String,Object>> SearchMutilLog(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page)
            throws ParseException, IllegalAccessException ,ExceptionInInitializerError {
        String indexName = eSproperties.currenTime(index);
        if (argsItem.getType() == null && argsItem.getChildren() == null) {
            return new Response<>(this.findall(curr_page, per_page, order, indexName).getData());
        }
        List<SearchArgs.Condition> children = argsItem.getChildren();
        String[] times = new String[2];  // 拿到开始时间和结束时间，用来查询索引库
        for (SearchArgs.Condition child : children) {
            // 如果本次查询设计到时间查询
            if (child.getOperator().equals("ge") || child.getOperator().equals("le")) {
                if (child.getOperator().equals("ge")) times[0] = child.getValue();

                if (child.getOperator().equals("le")) times[1] = child.getValue();
            }
            if (child.getField().equals("moudel") && child.operator.equals("in")) {
                if (child.getValues().get(0).equals("")) {
                    List<String> list = this.comptrollerDao.GetMoudel();
                    child.setValues(list);
                }
            }
        }
        // 如果开始时间和结束时间都有，则进行解析出所有的索引
        if (times[0] != null && times[1] != null) {
            List<String> mounth = eSproperties.suxMonth(times[0], times[1]);
            return new Response<>(this.SearchMulti(argsItem, order, per_page, curr_page, mounth));
        }
        SearchHits<comptroller> searchHits = this.esTemplate.SearchLikeMutil4(argsItem, order, per_page, curr_page, comptroller.class, indexName);
        Map<String, Object> parse = this.Parse(searchHits);
        return new Response<>(parse);
    }

    /**
     * 查询设计到时间，并且跨越多个索引库
     */
    @SuppressWarnings("unchecked")
    private Map<String,Object> SearchMulti(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page, List<String> indexName)
            throws ExceptionInInitializerError, ParseException, IllegalAccessException {
        List<SearchHits<comptroller>> lists = new ArrayList<>();
        Map<String,Object> reslist = new HashMap<>();
//        indexName.forEach(item -> executorService.execute(() -> {
//            System.out.println(Thread.currentThread().getName());
//            try {
//                lists.add(this.esTemplate.SearchLikeMutil4(argsItem, order, per_page, curr_page, comptroller.class, index + item));
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }));

        indexName.forEach(item -> {
            System.out.println(item);
            try {
                lists.add(this.esTemplate.SearchLikeMutil4(argsItem, order, per_page, curr_page, comptroller.class, index + item));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        int total = 0;
        List<comptrollerReturn> data = new ArrayList<>();
        for (SearchHits<comptroller> list : lists) {
            Map<String, Object> parse = this.Parse(list);
            total += (int) parse.get("total");
            for (comptrollerReturn comptrollerReturn : (List<comptrollerReturn>) parse.get("data")) {
                data.add(comptrollerReturn);
            }
        }
        reslist.put("total",total);
        reslist.put("data", data);
        return reslist;
    }

    private Map<String,Object> Parse(SearchHits<comptroller> searchHits) throws ParseException, IllegalAccessException {
        if (searchHits == null) return null;

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
        Map<String,Object> res = new HashMap<>();
        res.put("data",datas);
        res.put("total",Math.toIntExact(totalHits));
        return res;
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
            if (s.equals("全部")) {
                label.setLabel(s);
                label.setValue("");
            } else {
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
