package com.example.Run;

import com.example.Utils.Maputil;
import com.example.Utils.SearchArgs;
import com.example.Utils.TimeUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
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
import java.util.ArrayList;
import java.util.List;

@Component
public class EsTemplate {
    @Resource
    public  ElasticsearchRestTemplate elasticsearchRestTemplate;


    /**
     * 查询所有内容
     * @param cls 查询那个index
     */
    public <T> SearchHits<T> SearchAll(PageRequest request, Class<T> cls, SearchArgs.Order order, String... IndexName){
        IndexName = this.filterIndexName(IndexName);
        if (IndexName.length == 0) return null;

        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(new MatchAllQueryBuilder())
                .withPageable(request)
                .withSort(Sort.by(order.getField()))
                .build();
        return elasticsearchRestTemplate.search(build,cls,IndexCoordinates.of(IndexName));
    }

    /**
     * 查询结果
     * @param boolQueryBuilder
     * @param pageRequest
     * @param sort
     * @param cls
     * @param IndexName
     * @param <T>
     * @return
     * @throws ExceptionInInitializerError
     */
    public <T> SearchHits<T> SearchLikeMutil1(BoolQueryBuilder boolQueryBuilder, PageRequest pageRequest, Sort sort, Class<T> cls, String... IndexName)
            throws ExceptionInInitializerError {
        IndexName = this.filterIndexName(IndexName);
        if (IndexName.length == 0) {
            return null;
        }
        NativeSearchQuery nativeSearchQuery = this.GenNativeSearchQuery(boolQueryBuilder, pageRequest, sort);
        return elasticsearchRestTemplate.search(nativeSearchQuery,cls,IndexCoordinates.of(IndexName));
    }

    /**
     * 查询结果2， 只查询部分字段，例如日志只查询系统名称等内容
     * 可以去重
     * @param <T>
     * @return
     */
    public <T> SearchHits<T> SearchLikeMutil2(BoolQueryBuilder boolQueryBuilder,PageRequest pageRequest,SourceFilter sourceFilter,
                                              Sort sort,Class<T> cls, String... IndexName){
        IndexName = this.filterIndexName(IndexName);
        if (IndexName.length == 0) {
            return null;
        }
        NativeSearchQuery appname = this.GenNativeSearchQuery(boolQueryBuilder, pageRequest, sort, sourceFilter, "appname");
        return elasticsearchRestTemplate.search(appname,cls,IndexCoordinates.of(IndexName));
    }

    /**
     * 查询对应的数据
     * @param cls
     * @param IndexName
     * @param <T>
     * @return
     * @throws ParseException
     */
    public <T> SearchHits<T> SearchLikeMutil4(BoolQueryBuilder boolQueryBuilder,RangeQueryBuilder rangeQueryBuilder, PageRequest pageRequest,Sort sort, Class<T> cls,String... IndexName)
            throws ParseException, ExceptionInInitializerError {
        IndexName = this.filterIndexName(IndexName);
        if (IndexName.length == 0) {
            return null;
        }
        NativeSearchQuery nativeSearchQuery = this.GenNativeSearchQuery(boolQueryBuilder, pageRequest, sort);
        return elasticsearchRestTemplate.search(nativeSearchQuery,cls,IndexCoordinates.of(IndexName));
    }







// ----------------------   分隔符  -------------------


    /**
     * 检查该es库是否存在
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
     * 构建时间查询条件
     * @return
     */
    public RangeQueryBuilder GenRangeQueryBuilder(List<SearchArgs.Condition> children) throws ParseException {
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
                long start = TimeUtils.Parselong(time[0]);
                long end = TimeUtils.Parselong(time[1]);;
                rangeQueryBuilder = new RangeQueryBuilder(filed);
                rangeQueryBuilder.gte(start);
                rangeQueryBuilder.lte(end);
            }
        }
        return rangeQueryBuilder;
    }


    /**
     * 生产一个模糊查询
     * @param filed
     * @param value
     * @return
     */
    public  MatchQueryBuilder GenMatchQueryBuilder(String filed,String value) {
        return new MatchQueryBuilder(filed,value);
    }
    /**
     * 生产一个时间查询函数
     * @param filed
     * @param start
     * @param end
     * @return
     */
    public RangeQueryBuilder GenRangeQueryBuilder(String filed, long start, long end) {
        RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder(filed);
        rangeQueryBuilder.gte(start);
        rangeQueryBuilder.lte(end);
        return rangeQueryBuilder;
    }

    /**
     * 构建一个范围查询条件
     * @param filed
     * @param start
     * @param end
     * @return
     * @throws ParseException
     */
    public RangeQueryBuilder GenRangeQueryBuilder(String filed, String start, String end) throws ParseException {
        long sta = TimeUtils.Parselong(start);
        long en = TimeUtils.Parselong(end);
        return this.GenRangeQueryBuilder(filed,sta,en);
    }

    /**
     * 生成一个bool查询
     * @return
     */
    public BoolQueryBuilder GenBoolQueryBuilder(){
        return new BoolQueryBuilder();
    }
    /**
     * 生成一个bool查询
     * @param matchQueryBuilder
     * @return
     */
    public BoolQueryBuilder GenBoolQueryBuilder(MatchQueryBuilder... matchQueryBuilder){
        return this.GenBoolQueryBuilder(null,matchQueryBuilder);
    }
    /**
     * 生成一个bool查询
     * @param matchQueryBuilder
     * @param rangeQueryBuilder
     * @return
     */
    public BoolQueryBuilder GenBoolQueryBuilder(RangeQueryBuilder rangeQueryBuilder,MatchQueryBuilder... matchQueryBuilder){
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        for (MatchQueryBuilder queryBuilder : matchQueryBuilder) {
            boolQueryBuilder.must(queryBuilder);
        }
        if (rangeQueryBuilder != null)  boolQueryBuilder.must(rangeQueryBuilder);
        return boolQueryBuilder;
    }

    /**
     * 根据传入的字段，生成排序
     * @param filed
     * @return
     */
    public Sort GenSort(String filed,String order){
        Sort.Direction direction =  order.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction,filed);
    }

    /**
     * 生成排序字段
     * @param order
     * @return
     */
    public Sort GenSort(SearchArgs.Order order){
        String field = order.getField();
        String order_type = order.getOrder_type();
        return this.GenSort(field,order_type);
    }

    /**
     * 生成分页请求
     * @param page
     * @param size
     * @return
     */
    public PageRequest GenPageRequest(int page,int size) {
        return PageRequest.of(page,size);
    }



    /**
     * 多条件模糊查询, 适用log
     * 对系统名称进行去重复，只返回appname字段
     */
    public <T> SearchHits<T> SearchLikeMutil3(SearchArgs.ArgsItem argsItem, SearchArgs.Order order, int size, int page, Class<T> cls,String...  IndexName) throws ParseException {
        IndexName = this.filterIndexName(IndexName);
        if (IndexName.length == 0) {
            return null;
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
            if(operator != null){
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
     * 删除数据
     * @param id
     * @param IndexName
     */
    public void delete(String id, String...  IndexName) {
        IndexName = this.filterIndexName(IndexName);
        if (IndexName.length == 0 || id.equals("")) return;
        elasticsearchRestTemplate.delete(id,IndexCoordinates.of(IndexName));
    }

    /**
     * 构建查询条件, 过滤字段，只显示部分字段，去重
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

    /**
     * 构建查询条件，显示分页去重
     * @param boolQueryBuilder
     * @param pageRequest
     * @param sort
     * @param disconnt
     * @return
     */
    private NativeSearchQuery GenNativeSearchQuery(BoolQueryBuilder boolQueryBuilder, PageRequest pageRequest, Sort sort,   String disconnt) {
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSort(sort)
                .withCollapseField(disconnt)
                .build();
        return build;
    }

    /**
     * 构建查询请求，显示分页，排序
     * @param boolQueryBuilder
     * @param pageRequest
     * @param sort
     * @return
     */
    private NativeSearchQuery GenNativeSearchQuery(BoolQueryBuilder boolQueryBuilder, PageRequest pageRequest, Sort sort) {
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSort(sort)
                .build();
        return build;
    }

    /**
     * 对查询的索引库进行过滤，不存在的索引库不进行搜索
     * @param oldIndex
     * @return
     */
    public String[] filterIndexName(String[] oldIndex){
        List<String> NowIndex = new ArrayList<>();
        for (String s : oldIndex) {
            if (this.ExistsIndexName(s)){
                NowIndex.add(s);
            }
        }
        return  NowIndex.toArray(new String[NowIndex.size()]);
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
     * 根据索引名称，判断当前索引是否存在
     * @param IndexName
     * @return
     */
    public boolean ExistsIndexName(String IndexName) {
          IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(IndexCoordinates.of(IndexName));
        return indexOperations.exists();
    }

}
