package com.example.Run;

import com.example.Utils.Maputil;
import com.example.Utils.SearchArgs;
import com.example.Utils.TimeUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class EsTemplate {
    @Resource
    private  ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 根据索引名称，判断当前索引是否存在
     * @param IndexName
     * @return
     */
    public boolean ExistsIndexName(String IndexName) {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(IndexCoordinates.of(IndexName));

        return indexOperations.exists();
    }
    
    
    /**
     * 指定向某一个索引中写入数据
     * @param cls
     * @param IndexName
     * @param <T>
     * @return
     */
    public <T> boolean InsertDocument(String IndexName,T cls){
        elasticsearchRestTemplate.save(cls,IndexCoordinates.of(IndexName));
        return false;
    }



    /**
     * 设置过滤条件，只显示某些字段
     * @param showName
     * @return
     */
    public  SourceFilter sourceFilter(String showName) {
        return new SourceFilter() {
            @Override
            public String[] getIncludes() {
                return new String[]{showName};
            }

            @Override
            public String[] getExcludes() {
                return new String[0];
            }
        };
    }


    /**
     * 查询所有内容
     * @param cls 查询那个index
     */
    public <T> SearchHits<T> SearchAll(PageRequest request, Class<T> cls, SearchArgs.Order order, String... IndexName){

        if (!this.ExistsIndexName(IndexName[0])){
            throw new RuntimeException("索引名不存在");
        }
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(new MatchAllQueryBuilder())
                .withPageable(request)
                .withSort(Sort.by(order.getField()))
                .build();
        return elasticsearchRestTemplate.search(build,cls,IndexCoordinates.of(IndexName));
    }


    /**
     * 构建时间查询条件
     * @return
     */
    private RangeQueryBuilder GenRangeQueryBuilder(List<SearchArgs.Condition> children) throws ParseException {
        RangeQueryBuilder rangeQueryBuilder = null;
        String[] time = new String[2];
        for (SearchArgs.Condition child : children) {
            String filed = child.getField();
            String operator = child.getOperator();
            if (operator != null) {
                if (operator.equals("ge")) {
                    time[0] = child.getValue();
                    continue;
                }
                if (operator.equals("le")){
                    time[1] = child.getValue();
                }
            }

            if (time[0] != null && time[1] != null){
                // Date start = TimeUtils.ParseTimestamp("2000-01-01 00:00:00");
//                Date end = TimeUtils.ParseTimestamp(time[1]);
                long start = 950581812000L;
                long end = 1651161600123L;
                rangeQueryBuilder = new RangeQueryBuilder(filed);
                rangeQueryBuilder.gte(start);

            }
        }
        return rangeQueryBuilder;
    }

    /**
     * 构建模糊查询条件
     * @return
     */
    private  List<MatchQueryBuilder> GenMatchQueryBuilder(List<SearchArgs.Condition> children) {
        List<MatchQueryBuilder> matchQueryBuilder = new ArrayList<>();
        for (SearchArgs.Condition child : children) {
            String filed = child.getField();
            String operator = child.getOperator();
            String value = "";
            // 如果value有值
            if (child.getValue() != null) {
                value = child.getValue();
                if (value.contains(",")) {
                    String[] split = value.split(",");
                    for (String s : split) {
                        MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder(filed, s);
                        matchQueryBuilder.add(matchQueryBuilder1);
                    }
                }
                if (operator.contains("=")){
                    MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder(filed, value);
                    matchQueryBuilder.add(matchQueryBuilder1);
                }
            }
            //如果用的是多字段查询, 只获取values的值
            if (child.getValues() != null  && operator.equals("in")){
                List<String> values = child.getValues();
                for (String s : values) {
                    MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder(filed, s);
                    matchQueryBuilder.add(matchQueryBuilder1);
                }
            }
        }
        return matchQueryBuilder;
    }


    /**
     * 查询对应的数据
     * @param argsItem
     * @param order
     * @param size
     * @param page
     * @param cls
     * @param IndexName
     * @param <T>
     * @return
     * @throws ParseException
     */
    public <T> SearchHits<T> SearchLikeMutil4(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int size, int page, Class<T> cls,String... IndexName)
            throws ParseException, ExceptionInInitializerError {
        for (String s : IndexName) {
            if (!this.ExistsIndexName(s)){
                throw new ExceptionInInitializerError("索引名不存在");
            }
        }
        BoolQueryBuilder boolQueryBuilder = null;
        List<SearchArgs.Condition> children = argsItem.getChildren();
        List<MatchQueryBuilder> matchQueryBuilders = this.GenMatchQueryBuilder(children);
        if (matchQueryBuilders.size() > 0) {
            boolQueryBuilder = new BoolQueryBuilder();
            matchQueryBuilders.forEach(boolQueryBuilder::should);
        }
        RangeQueryBuilder rangeQueryBuilder = this.GenRangeQueryBuilder(children);
        if (rangeQueryBuilder != null) {
            if (boolQueryBuilder == null) boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(rangeQueryBuilder);
        }
        if (boolQueryBuilder == null) return null;

        Sort.Direction sor;
        if (order.getOrder_type() == null) {
            sor = Sort.Direction.DESC;
        }else {
            sor = order.getOrder_type().equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(PageRequest.of(page,size))
                .withSort(Sort.by(sor,order.getField()))
                .build();
        return elasticsearchRestTemplate.search(build,cls,IndexCoordinates.of(IndexName));
    }


    /**
     * 多条件模糊查询, 适用log
     * 对系统名称进行去重复，只返回appname字段
     */
    public <T> SearchHits<T> SearchLikeMutil3(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int size, int page, Class<T> cls,String...  IndexName) throws ParseException {
        for (String s : IndexName) {
            if (!this.ExistsIndexName(s)){
                throw new ExceptionInInitializerError("索引名不存在");
            }
        }
        BoolQueryBuilder boolQueryBuilder = null;
        SourceFilter sourceFilter = null;
        List<SearchArgs.Condition> children = argsItem.getChildren();
        for (SearchArgs.Condition child : children) {
            String field =  child.getField();
            String operator = child.getOperator();
            String value =  child.getValue();
            if (field.equals("appname") && value.equals("")) {
                boolQueryBuilder = new BoolQueryBuilder();
                sourceFilter = this.sourceFilter(field);
            }
            if (field.equals("appname") && !value.equals("")) {

            }
            if(operator.equals("ge") || operator.equals("le")){
                boolQueryBuilder = new BoolQueryBuilder();
                RangeQueryBuilder rangeQueryBuilder = this.GenRangeQueryBuilder(children);
                boolQueryBuilder.must(rangeQueryBuilder);

            }
        }
        if (boolQueryBuilder == null) return null;

        Sort.Direction sor;
        if (order.getOrder_type() == null) {
            sor = Sort.Direction.DESC;
        }else {
            sor = order.getOrder_type().equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        NativeSearchQuery build;
        if (sourceFilter != null) {
            // 如果不是空，代表传入的是appname查询系统名称，则只查询系统名称
            build = this.GenNativeSearchQuery(boolQueryBuilder,PageRequest.of(page,size),Sort.by(sor,order.getField()),sourceFilter,Maputil.ReplaceAddKeyword("appname"));
        }else {
            build = this.GenNativeSearchQuery(boolQueryBuilder,PageRequest.of(page,size),Sort.by(sor,order.getField()),Maputil.ReplaceAddKeyword("appname"));
        }

        return elasticsearchRestTemplate.search(build,cls,IndexCoordinates.of(IndexName));
    }


    /**
     * 构建查询条件
     * @param boolQueryBuilder
     * @param pageRequest
     * @param sort
     * @param sourceFilter
     * @param disconnt
     * @return
     */
    private NativeSearchQuery GenNativeSearchQuery(BoolQueryBuilder boolQueryBuilder, PageRequest pageRequest, Sort sort, SourceFilter sourceFilter,  String disconnt) {
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSort(sort)
                .withSourceFilter(sourceFilter)
                .withCollapseField(disconnt)
                .build();
        return build;
    }

    private NativeSearchQuery GenNativeSearchQuery(BoolQueryBuilder boolQueryBuilder, PageRequest pageRequest, Sort sort,   String disconnt) {
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSort(sort)
                .withCollapseField(disconnt)
                .build();
        return build;
    }


}
