package com.example.Service;

import com.example.Dao.comptrollerDao;
import com.example.Pojo.Model;
import com.example.Pojo.comptroller;
import com.example.Pojo.comptrollerReturn;
import com.example.Run.ESproperties;
import com.example.Run.EsTemplate;
import com.example.Utils.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final Class<comptroller> cls = comptroller.class;

    @Value ("${es.per.sj}")
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

    public Response<Map<String, Object>> findall(int size, int page, SearchArgs.Order order, String indexName) throws java.text.ParseException, java.lang.IllegalAccessException {
        PageRequest request = PageRequest.of(size, page);
        SearchHits<comptroller> searchHits = this.esTemplate.SearchAll(request, comptroller.class, order, indexName);
        return new Response<>(this.Parse(searchHits));
    }

    public Response<Map<String, Object>> SearchMutisj(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page)
            throws ParseException, IllegalAccessException, ExceptionInInitializerError {
        String indexName = eSproperties.currenTime(index);
        // 传入参数为空，代表查询所有
        if (argsItem.getType() == null && argsItem.getChildren() == null) {
            return new Response<>(this.findall(curr_page, per_page, order, indexName).getData());
        }
        List<SearchArgs.Condition> children = argsItem.getChildren();
        String[] times = new String[2];  // 拿到开始时间和结束时间，用来查询索引库
        String timeFiled = "";
        for (SearchArgs.Condition child : children) {
            // 如果本次查询条件涉及到时间查询
            if (child.getOperator().equals("ge") || child.getOperator().equals("le")) {
                timeFiled = child.getField();
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
            String[] sjINdex = eSproperties.parseIndexName(eSproperties.suxMonth(times[0], times[1]), index);
            long[] lons = new long[2];
            lons[0] = TimeUtils.Parselong(times[0]);
            lons[1] = TimeUtils.Parselong(times[1]);
            return new Response<>(this.SearchMulti(argsItem, order, per_page, curr_page, lons, timeFiled, sjINdex));
        }else {
            return new Response<>(this.SearchMulti(argsItem, order, per_page, curr_page, null, timeFiled, indexName));
        }

    }


    /**
     * 查询设计到时间，跨越多个索引库
     */
    @SuppressWarnings ("unchecked")
    private Map<String, Object> SearchMulti(SearchArgs.ArgsItem argsItem, SearchArgs.Order order,
                                            int per_page, int curr_page, long[] time, String timeFiled,
                                            String... indexName)
            throws ExceptionInInitializerError, ParseException, IllegalAccessException {
        BoolQueryBuilder boolQueryBuilder = this.SearchMultiNoTime(argsItem);
        PageRequest of = this.esTemplate.GenPageRequest(curr_page, per_page);
        Sort orders = this.esTemplate.GenSort(order);
        if (time[0] > 0L && time[1] > 0L  && !timeFiled.equals("")) {
            RangeQueryBuilder rangeQueryBuilder = this.esTemplate.GenRangeQueryBuilder(timeFiled, time[0], time[1]);
            boolQueryBuilder.must(rangeQueryBuilder);
        }
        SearchHits<comptroller> searchHits = this.esTemplate.SearchLikeMutil1(boolQueryBuilder, of, orders, cls, indexName);
        return this.Parse(searchHits);
    }


    /**
     * 根据前端条件构建bool查询
     *
     * @return
     */
    private BoolQueryBuilder SearchMultiNoTime(SearchArgs.ArgsItem argsItem) {
        List<SearchArgs.Condition> children = argsItem.getChildren();
        BoolQueryBuilder boolQueryBuilder = null;
        for (SearchArgs.Condition child : children) {
            String value = child.getValue();
            String field = child.getField();
            String operator = child.getOperator();
            if (operator.equals("=")) {
                boolQueryBuilder = esTemplate
                        .GenBoolQueryBuilder(esTemplate.GenMatchQueryBuilder(field, value));
            }
        }
        return boolQueryBuilder;
    }


    /**
     * 将查询的结果进行转换
     *
     * @param searchHits
     * @return
     * @throws ParseException
     * @throws IllegalAccessException
     */
    private Map<String, Object> Parse(SearchHits<comptroller> searchHits)
            throws ParseException, IllegalAccessException {
        if (searchHits == null) return new HashMap<>();

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
        Map<String, Object> res = new HashMap<>();
        res.put("data", datas);
        res.put("total", Math.toIntExact(totalHits));
        return res;
    }


    /**
     * 获取审计模块的查询模块内容
     *
     * @return
     * @throws Exception
     */
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
