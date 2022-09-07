package com.example.Service;


import com.example.Dao.LogDao;
import com.example.Pojo.Log;
import com.example.Pojo.LogReturn;
import com.example.Pojo.Model;
import com.example.Run.ESproperties;
import com.example.Run.Email;
import com.example.Run.EmailProperties;
import com.example.Run.EsTemplate;
import com.example.Utils.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Service
public class LogDaoService {
    private final Logger mylog = LoggerFactory.getLogger(LogDaoService.class);
    private final LogDao logDevelopDao;
    private final EsTemplate esTemplate;
    private final Email email;
    private final EmailProperties emailProperties;
    private final ESproperties eSproperties;
    private final ExecutorService executorService;
    private final String[] values = new String[]{"全部", "正常", "轻微", "一般", "严重", "非常严重"};
    private final Class<Log> cls = Log.class;

    @Value ("${es.per.log}")
    private String index;

    @Autowired (required = false)
    public LogDaoService(LogDao logDevelopDao,
                         EsTemplate esTemplate,
                         EmailProperties emailProperties,
                         ESproperties eSproperties,
                         ExecutorService executorService,
                         Email email) {
        this.logDevelopDao = logDevelopDao;
        this.esTemplate = esTemplate;
        this.emailProperties = emailProperties;
        this.eSproperties = eSproperties;
        this.executorService = executorService;
        this.email = email;
    }


    /**
     * 写入到数据库和es中
     *
     * @param logOperation
     */
    public void InsertDB(Log logOperation) {
        if (logOperation != null) {
            try {
                synchronized (this) {
                    //this.logDevelopDao.Insertlog(logOperation);
                    //Log logOperation1 = this.logDevelopDao.SelectByid(this.logDevelopDao.Maxid());
                    // this.logES.save(logOperation);
                    esTemplate.InsertDocument(eSproperties.currenTime(index), logOperation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Response<Map<String, Object>> findall(PageRequest request, SearchArgs.Order order, String indexName) throws Exception {
        SearchHits<Log> searchHits = this.esTemplate.SearchAll(request, cls, order, indexName);
        Map<String, Object> parse = this.Parse(searchHits);
        return new Response<>(parse);
    }


    /**
     * 发送邮件
     */
    public void Emails() {
        String[] objects = (String[]) emailProperties.getTo().toArray();
        email.SetPerson(emailProperties.getFrom(), objects);
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


    public Response<Map<String, Object>> SearchMutilLog(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page) throws Exception {
        // 查询所有数据
        String indexName = eSproperties.currenTime(index);
        if (argsItem.getType() == null && argsItem.getChildren() == null) {
            PageRequest of = PageRequest.of(curr_page, per_page);
            Response<Map<String, Object>> findall = this.findall(of, order, indexName);
            return findall;
        }

        List<SearchArgs.Condition> children = argsItem.getChildren();
        String[] times = new String[2];  // 拿到开始时间和结束时间，用来查询索引库
        String timeFiled = "";
        for (SearchArgs.Condition child : children) {
            // 如果本次查询设计到时间查询
            if (child.getOperator().equals("ge") || child.getOperator().equals("le")) {
                timeFiled = child.getField();
                if (child.getOperator().equals("ge")) times[0] = child.getValue();

                if (child.getOperator().equals("le")) times[1] = child.getValue();
            }
            // 如果前端传入的是""， 表示查询所有
            if (child.getField().equals("level") && child.getOperator().equals("in")) {
                if (child.getValues().get(0).equals("")) {
                    String[] strings = {"正常", "轻微", "一般", "严重", "非常严重"};
                    List<String> list = Arrays.asList(strings);
                    child.setValues(list);
                }
            }

        }
        if (times[0] != null && times[1] != null) {
            String[] logINdex = eSproperties.parseIndexName(eSproperties.suxMonth(times[0], times[1]), index);
            long[] lons = new long[2];
            lons[0] = TimeUtils.Parselong(times[0]);
            lons[1] = TimeUtils.Parselong(times[1]);
            return new Response<>(this.SearchMulti(argsItem, order, per_page, curr_page,lons, timeFiled, logINdex));
        }
        SearchHits<Log> searchHits = this.esTemplate.SearchLikeMutil4(argsItem, order, per_page, curr_page, Log.class, indexName);
        return new Response<>(this.Parse(searchHits));
    }

    /**
     * 查询设计到时间，并且跨越多个索引库
     */
    @SuppressWarnings ("unchecked")
    private Map<String, Object> SearchMulti(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page,
                                            long[] lons,String timeFiled,String[] indexName)
            throws ExceptionInInitializerError, Exception {

        BoolQueryBuilder boolQueryBuilder = this.SearchMultiBool(argsItem);
        Sort orders = this.esTemplate.GenSort(order);
        PageRequest pageRequest = this.esTemplate.GenPageRequest(curr_page, per_page);
        if (lons[0] > 0L && lons[1] > 0L && !timeFiled.equals("")) {
            RangeQueryBuilder rangeQueryBuilder = this.esTemplate.GenRangeQueryBuilder(timeFiled, lons[0], lons[1]);
            boolQueryBuilder.must(rangeQueryBuilder);
        }
 
        SearchHits<Log> searchHits = this.esTemplate.SearchLikeMutil3(argsItem, order, per_page, curr_page, Log.class, in);
        Map<String, Object> parse = this.Parse(searchHits);
        return parse;
    }

    /**
     * 根据前端传入的参数，生成对应的过滤查询
     * @param argsItem
     * @return
     */
    private BoolQueryBuilder SearchMultiBool(SearchArgs.ArgsItem argsItem) {
        List<SearchArgs.Condition> children = argsItem.getChildren();
        BoolQueryBuilder boolQueryBuilder = this.esTemplate.GenBoolQueryBuilder();
        for (SearchArgs.Condition child : children) {
            String operator = child.getOperator();
            String field = child.getField();
            String value = child.getValue();
            List<String> values = child.getValues();
            if (operator.equals("=")) {
                MatchQueryBuilder matchQueryBuilder = this.esTemplate.GenMatchQueryBuilder(field, value);
                boolQueryBuilder.must(matchQueryBuilder);
            }
            // 如果是多字段查询
            if (operator.equals("in") && values != null) {
                for (String s : values) {
                    MatchQueryBuilder matchQueryBuilder = this.esTemplate.GenMatchQueryBuilder(field, s);
                    boolQueryBuilder.must(matchQueryBuilder);
                }
            }
        }
        return boolQueryBuilder;
    }


    /**
     * 第三期修改查询接口，只查询系统名称和时间段
     *
     * @param argsItem
     * @param order
     * @param per_page
     * @param curr_page
     * @throws ParseException
     */
    @SuppressWarnings ("unchecked")
    public Response<Map<String, Object>> search(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page)
            throws Exception {
        // 查询所有数据
        String nowIndex = eSproperties.currenTime(this.index);
        // 如果参数为空，代表查询所有
        if (argsItem.getType() == null && argsItem.getChildren() == null) {
            argsItem = new SearchArgs.ArgsItem();
            SearchArgs.Condition condition = new SearchArgs.Condition();
            condition.setField("appname");
            condition.setValue("");
            List<SearchArgs.Condition> list = new ArrayList<>();
            list.add(condition);
            argsItem.setChildren(list);
            SearchHits<Log> searchHits = this.esTemplate.SearchLikeMutil3(argsItem, order, per_page, curr_page, Log.class, nowIndex);
            Map<String, Object> parse = this.Parse2(searchHits);
            return new Response<>(parse);
        } else {
            String[] times = new String[2];  // 拿到开始时间和结束时间，用来查询索引库
            for (SearchArgs.Condition child : argsItem.getChildren()) {
                // 如果本次查询设计到时间查询
                if (child.getOperator().equals("ge") || child.getOperator().equals("le")) {
                    if (child.getOperator().equals("ge")) times[0] = child.getValue();

                    if (child.getOperator().equals("le")) times[1] = child.getValue();
                }
            }
            // 如果开始时间和结束时间都有，则进行解析出所有的索引
            List<String> mounth = eSproperties.suxMonth(times[0], times[1]);
            return new Response<>(this.SearchMulti(argsItem, order, per_page, curr_page, mounth));
        }

    }

    /**
     * 根据查询的结果删除数据
     * @param argsItem
     * @param order
     * @param per_page
     * @param curr_page
     */
    public void delete(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page)
    {

    }


    /**
     * 将es查询的结果进行处理，返回List<LogReturn>数据
     * @param searchHits
     * @return
     * @throws Exception
     */
    private Map<String, Object> Parse(SearchHits<Log> searchHits) throws ParseException, IllegalAccessException {
        if (searchHits == null) return new HashMap<>();

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
        Map<String, Object> res = new HashMap<>();
        res.put("data", datas);
        res.put("total", Math.toIntExact(totalHits));
        return res;
    }

    /**
     * 第三期修改，只查询了系统名称等字段，所以不需要之前的转换函数
     * @param searchHits
     * @return 系统名称，时间等信息 不返回全部数据
     */
    private Map<String, Object> Parse2(SearchHits<Log> searchHits) {
        if (searchHits == null) return new HashMap<>();

        List<SearchHit<Log>> searchHits1 = searchHits.getSearchHits();
        long totalHits = searchHits.getTotalHits();
        List<Map<String,Object>> datas = new ArrayList<>();
        for (SearchHit<Log> comptrollerSearchHit : searchHits1) {
            Map<String,Object> maps = new HashMap<>();
            String index = comptrollerSearchHit.getIndex();
            String appname = comptrollerSearchHit.getContent().getAppname();
            String[] substring = index.split(this.index);
            substring[1] += "01 -" + substring[1] + "30" ;
            maps.put("time",substring[1]);
            maps.put("system", appname);
            datas.add(maps);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("data", datas);
        res.put("total", Math.toIntExact(totalHits));
        return res;
    }

    /**
     * 获得页面的标签模块
     *
     * @return
     * @throws Exception
     */
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
     *
     * @return
     */
    private Model Level() {
        Model model = new Model();
        model.setField("level");
        model.setLabel("eq");
        model.setOperator("严重等级");
        model.setType("checkbox");
        model.setDatatype("string");
        model.setCanInput("no");
        List<Model.label> labelList = new ArrayList<>();
        for (String value : this.values) {
            Model.label label = new Model.label();
            if (value.equals("全部")) {
                label.setLabel(value);
                label.setValue("");
            } else {
                label.setLabel(value);
                label.setValue(value);
            }
            labelList.add(label);
        }
        model.setOtions(labelList);
        return model;
    }


}
