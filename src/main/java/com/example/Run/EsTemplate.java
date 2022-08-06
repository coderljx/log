package com.example.Run;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import com.example.Utils.Maputil;
import org.elasticsearch.index.query.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class EsTemplate {
    @Resource
    private  ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 检查索引是否存在,不存在创建索引
     * @param cls
     * @param <T>
     * @return
     */
    public <T> boolean IndexExists(Class<T> cls){
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(cls);
         if (indexOperations.exists() ){
             return true;
         }
        return indexOperations.create();
    }


    /**
     * 精确查询, 单条件
     * @param key 查询的key
     * @param value 查询的value
     * @param cls 返回的类，该类是index，要存在索引
     */
    public <T> SearchHits<T> SearchTerm(String key, String value, int size ,Class<T> cls){
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(new TermQueryBuilder(key, value))  //总的查询
                .withPageable(Pageable.ofSize(size)) // 设置分页
                .build();//设置bool查

        return elasticsearchRestTemplate.search(build,cls);
    }

    /**
     * 多字段精确查询，一个key对应多个value， 单条件
     */
    public <T> SearchHits<T> SearchTerms(String key, int size ,Class<T> cls,String... value){
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(new TermsQueryBuilder(key,value))  //总的查询
                .withPageable(Pageable.ofSize(size))
                .build();//设置bool查

        return elasticsearchRestTemplate.search(build,cls);
    }

    /**
     * 模糊查询， 单条件
     */
    public <T> SearchHits<T> SearchLike(String key, String value,int size , Class<T> cls){
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(new MatchPhraseQueryBuilder(key,value))
                .withPageable(Pageable.ofSize(size))
                .build();
        return elasticsearchRestTemplate.search(build,cls);
    }


    /**
     * 多条件模糊查询
     * @param maps 查询的字段以及对应的值
     */
    public <T> SearchHits<T> SearchLikeMutil2(Map<String,Object> maps ,int size, Class<T> cls){
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        String[] Keys = Maputil.GetMapKey(maps);
        for (String key : Keys) {
            boolQueryBuilder.must(new MatchQueryBuilder(key,maps.get(key)));
        }
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(Pageable.ofSize(size))
                .build();
        return elasticsearchRestTemplate.search(build,cls);
    }

    /**
     * 查询所有内容
     * @param size 查询的条数
     * @param cls 查询那个index
     */
    public <T> SearchHits<T> SearchAll(int size , Class<T> cls){
        MatchAllQuery build1 = new MatchAllQuery.Builder().build();
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery((QueryBuilder) build1)
                .withPageable(Pageable.ofSize(size))
                .build();
        return elasticsearchRestTemplate.search(build,cls);
    }


    /**
     * 范围查找
     * range查询找出那些落在指定区间内的数字或者时间。range 查询允许以下字符
     * #gt 大于>
     * #gte 大于等于>=
     * #lt 小于<
     * #lte 小于等于<=
     */
    public <T,V> SearchHits<T> SearchRange(String filed,V valuewidth, V valuend,String rule , int size , Class<T> cls){
        RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder(filed);
        if (rule.equals(">"))
            rangeQueryBuilder.gt(valuewidth);

        if (rule.equals(">="))
            rangeQueryBuilder.gte(valuewidth);

        if (rule.equals("<"))
            rangeQueryBuilder.lt(valuend);

        if (rule.equals("<="))
            rangeQueryBuilder.lte(valuend);

        if (rule.equals("><")){ // 大于开始值，小于结束值
            rangeQueryBuilder.gte(valuewidth);
            rangeQueryBuilder.lte(valuend);
        }
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(rangeQueryBuilder)
                .withPageable(Pageable.ofSize(size))
                .build();
        return elasticsearchRestTemplate.search(build,cls);
    }






}
