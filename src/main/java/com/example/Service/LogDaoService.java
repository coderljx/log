package com.example.Service;


import com.example.Pojo.Log;
import com.example.Pojo.LogReturn;
import com.example.Pojo.Model;
import com.example.Run.*;
import com.example.Utils.*;
import org.apache.poi.ss.usermodel.Workbook;
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
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Service
public class LogDaoService {
    private final Logger mylog = LoggerFactory.getLogger(LogDaoService.class);
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
    public LogDaoService(
            EsTemplate esTemplate,
            EmailProperties emailProperties,
            ESproperties eSproperties,
            ExecutorService executorService,
            Email email) {
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
                    esTemplate.InsertDocument(eSproperties.currenTime(index), logOperation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public SearchHits<Log> findall(PageRequest request, SearchArgs.Order order, String indexName) throws Exception {
        return this.esTemplate.SearchAll(request, cls, order, indexName);
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

    /**
     * 只查询时间系统等信息，过滤其他字段
     *
     * @param argsItem
     * @param order
     * @param per_page
     * @param curr_page
     * @return
     * @throws Exception
     */
    public Response<Map<String, Object>> SearchMutilLog(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page) throws Exception {
        // 查询所有数据
        String indexName = eSproperties.currenTime(index);
        if (argsItem.getType() == null && argsItem.getChildren() == null) {
            PageRequest of = PageRequest.of(curr_page, per_page);
            SearchHits<Log> findall = this.findall(of, order, indexName);
            Map<String, Object> stringObjectMap = this.Parse2(findall);
            return new Response<>(stringObjectMap);
        }
        return new Response<>(this.Parse2(this.SearchResout(argsItem, order, per_page, curr_page,"appname")));

    }


    /**
     * 根据查询的结果删除数据
     *
     * @param argsItem
     * @param order
     * @param per_page
     * @param curr_page
     */
    public void delete(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page) throws Exception {
        String indexName = eSproperties.currenTime(index);
        if (argsItem.getType() == null && argsItem.getChildren() == null) {
            PageRequest of = PageRequest.of(curr_page, per_page);
            SearchHits<Log> findall = this.findall(of, order, indexName);
            this.Batch(this.Parse3(findall), indexName);
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
            SearchHits<Log> id = this.SearchMulti(argsItem, order, per_page, curr_page, lons, timeFiled, "id", logINdex);
            List<String> list = this.Parse3(id);
            this.Batch(list,logINdex);
        } else {
            SearchHits<Log> id = this.SearchMulti(argsItem, order, per_page, curr_page, null, timeFiled, "", new String[]{indexName});
            List<String> list = this.Parse3(id);
            this.Batch(list,indexName);
        }
    }


    /**
     * 将查询到的数据 写入excel中
     * @param argsItem
     * @param order
     * @param per_page
     * @param curr_page
     */
    public Workbook export(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page) throws Exception {
        String indexName = eSproperties.currenTime(index);
        List<LogReturn> parse = null;
        if (argsItem.getType() == null && argsItem.getChildren() == null) {
            PageRequest of = PageRequest.of(curr_page, per_page);
            SearchHits<Log> findall = this.findall(of, order, indexName);
            parse = this.Parse(findall);
        }else {
            SearchHits<Log> searchHits = this.SearchResout(argsItem, order, per_page, curr_page, "");
            parse = this.Parse(searchHits);
        }
        Workbook workbook = Excel.CreateHeader("", "系统名称", "日志等级", "IP地址", "日志信息", "访问时间","事件类型");
        for (LogReturn logReturn : parse) {
            Excel.CreateData(
                    logReturn.getAppname(), logReturn.getLevel(),logReturn.getIpaddress(),
                    logReturn.getLogmessage(),logReturn.getRecorddate(), logReturn.getEventype()
            );
        }
        return workbook;
    }



//    ----------------------------------分隔符-----------------------------------------

    /**
     * 查询设计到时间，并且跨越多个索引库
     */
    @SuppressWarnings ("unchecked")
    private SearchHits<Log> SearchMulti(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page,
                                        long[] lons, String timeFiled, String sourceFilter, String[] indexName)
            throws ExceptionInInitializerError {
        BoolQueryBuilder boolQueryBuilder = this.SearchMultiBool(argsItem);
        Sort orders = this.esTemplate.GenSort(order);
        SourceFilter appname = null;
        if (!sourceFilter.equals("")) appname = this.esTemplate.sourceFilter(sourceFilter);

        PageRequest pageRequest = this.esTemplate.GenPageRequest(curr_page, per_page);
        if (lons != null && lons[0] > 0L && lons[1] > 0L && !timeFiled.equals("")) {
            RangeQueryBuilder rangeQueryBuilder = this.esTemplate.GenRangeQueryBuilder(timeFiled, lons[0], lons[1]);
            boolQueryBuilder.must(rangeQueryBuilder);
        }
        return this.esTemplate.SearchLikeMutil2(boolQueryBuilder, pageRequest, appname, orders, cls, indexName);
    }

    /**
     * 不构建时间类型的查询，只构建字段的bool查询条件
     *
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
                    boolQueryBuilder.should(matchQueryBuilder);
                }
            }
        }
        return boolQueryBuilder;
    }

    /**
     * 批量删除查询出来的日志信息， 根据id
     *
     * @param id
     * @param indexName
     */
    private void Batch(List<String> id, String... indexName) {
        if (id.size() <= 0) return;

        for (String s : id) {
            this.esTemplate.delete(s, indexName);
        }
    }

    /**
     * 将es查询的结果进行处理，返回List<LogReturn>数据
     *
     * @param searchHits
     * @return
     * @throws Exception
     */
    private List<LogReturn> Parse(SearchHits<Log> searchHits) throws ParseException, IllegalAccessException {
        if (searchHits == null) return new ArrayList<>();

        List<SearchHit<Log>> searchHits1 = searchHits.getSearchHits();
        List<LogReturn> datas = new ArrayList<>();
        for (SearchHit<Log> comptrollerSearchHit : searchHits1) {
            Log content = comptrollerSearchHit.getContent();
            Date recorddate = content.getRecorddate();
            LogReturn logReturn = Maputil.BeanToBean(content, new LogReturn());
            String newdate = TimeUtils.ParseDate(recorddate);
            logReturn.setRecorddate(newdate);
            datas.add(logReturn);
        }
        return datas;
    }

    /**
     * 第三期修改，只查询了系统名称等字段，所以不需要之前的转换函数
     *
     * @param searchHits
     * @return 系统名称，时间等信息 不返回全部数据
     */
    private Map<String, Object> Parse2(SearchHits<Log> searchHits) {
        if (searchHits == null) return new HashMap<>();

        List<SearchHit<Log>> searchHits1 = searchHits.getSearchHits();
        long totalHits = searchHits.getTotalHits();
        List<Map<String, Object>> datas = new ArrayList<>();
        for (SearchHit<Log> comptrollerSearchHit : searchHits1) {
            Map<String, Object> maps = new HashMap<>();
            String index = comptrollerSearchHit.getIndex();
            String appname = comptrollerSearchHit.getContent().getAppname();
            String[] substring = index.split(this.index);
            substring[1] += "01 -" + substring[1] + "30";
            maps.put("time", substring[1]);
            maps.put("system", appname);
            datas.add(maps);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("data", datas);
        res.put("total", Math.toIntExact(totalHits));
        return res;
    }

    /**
     * 提供删除能力，只筛选出所有数据的id，方便之后删除操作
     *
     * @param searchHits
     * @return
     */
    private List<String> Parse3(SearchHits<Log> searchHits) {
        if (searchHits == null) return new ArrayList<>();

        List<SearchHit<Log>> searchHits1 = searchHits.getSearchHits();
        List<String> datas = new ArrayList<>();
        for (SearchHit<Log> comptrollerSearchHit : searchHits1) {
            String id = comptrollerSearchHit.getId();
            datas.add(id);
        }
        return datas;
    }


    /**
     * 根据参数来查询对应的es查询结果，之后的除了则按照不同的逻辑来处理
     * @param argsItem
     * @param order
     * @param per_page
     * @param curr_page
     * @param filter
     * @return
     * @throws ParseException
     */
    private SearchHits<Log> SearchResout(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int per_page, int curr_page,String filter) throws ParseException {
        String indexName = eSproperties.currenTime(index);
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
            return this.SearchMulti(argsItem, order, per_page, curr_page, lons, timeFiled, filter, logINdex);
        } else {
            return this.SearchMulti(argsItem, order, per_page, curr_page, null, timeFiled, "", new String[]{indexName});
        }
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
