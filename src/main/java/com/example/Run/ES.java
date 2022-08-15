package com.example.Run;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

//@Component
public class ES {

    /**
     * index : 类似数据库，
     * document ： 类似数据库中的数据
     */
    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public ES(RestHighLevelClient restHighLevelClient){
        this.restHighLevelClient = restHighLevelClient;
    }

    public boolean IndexIsExists(String index){
        if (index.equals("")) {
            GetIndexRequest getIndexRequest = new GetIndexRequest(index);
            try {
                return restHighLevelClient.indices().exists(getIndexRequest,RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean IdExists(String index,String id){
        if (id.equals("")){
            try {
                GetRequest getIndexRequest = new GetRequest(index,id);
                return restHighLevelClient.exists(getIndexRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean CreateIndex(String index) {
        if (index.equals("")){
            try {
                CreateIndexRequest createIndex = new CreateIndexRequest(index);

                CreateIndexResponse createIndexResponse = restHighLevelClient
                        .indices().create(createIndex, RequestOptions.DEFAULT);
                return createIndexResponse.index().equals(index);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean DeleteIndex(String index){
        if (index.equals("")) {
            DeleteIndexRequest dele = new DeleteIndexRequest(index);
            try {
                AcknowledgedResponse delete = restHighLevelClient
                        .indices().delete(dele, RequestOptions.DEFAULT);
                return delete.isAcknowledged();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    public <T> boolean AddDocument(String index, Map<String,T> maps) {
        IndexRequest indexRequest = new IndexRequest(index);
        indexRequest.source(maps);
        indexRequest.timeout("1s");
        try {
            if (!this.IndexIsExists(index)){
                this.CreateIndex(index);
            }
            IndexResponse res = restHighLevelClient
                    .index(indexRequest, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DocumentExists(String index,String id){
        if (index.equals("") || id.equals(""))
            return false;

        GetRequest request = new GetRequest(index, id);
        // 不获取返回的 _source的上下文了
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");
        try {
            return restHighLevelClient.exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public org.elasticsearch.action.get.GetResponse GetDocument(String index,String id){
        if (index.equals("") || id.equals(""))
            return null;

        try {
            if (!this.DocumentExists(index,id))
                return null;

            GetRequest request = new GetRequest(index,id);
            return restHighLevelClient
                    .get(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> boolean UpdateDoucment(String index,String id,Map<String,T> maps){
        if (index.equals("") || id.equals(""))
            return false;

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.doc(maps);
        try {
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            return update.status().equals("ok");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DeleteDocument(String index,String id){
        if (index.equals("") || id.equals(""))
            return false;

        if (this.DocumentExists(index,id)){
            DeleteRequest deleteRequest = new DeleteRequest(index, id);
            try {
                DeleteResponse delete = restHighLevelClient
                        .delete(deleteRequest, RequestOptions.DEFAULT);
                return delete.status().name().equals("ok");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }else {
            return true;
        }

    }

    public <T> boolean BulkRequest(String index,List<T> lists){
        BulkRequest bulkRequest = new BulkRequest();
        int Numbers = lists.size();
        if (Numbers <= 0)
            return false;

        if (Numbers < 50)
            bulkRequest.timeout("1s");

        if (Numbers > 50 && Numbers < 150)
            bulkRequest.timeout("2s");

        if (Numbers > 150 && Numbers < 300)
            bulkRequest.timeout("4s");

        if (Numbers > 300 && Numbers < 1000)
            bulkRequest.timeout("10s");

        if (Numbers > 1000)
            bulkRequest.timeout("30s");

        if (Numbers > 5000)
            bulkRequest.timeout("60s");

        if (!this.IndexIsExists(index)){
            this.CreateIndex(index);
        }

        for (T list : lists) {
            IndexRequest source = new IndexRequest(index);
            String s = JSON.toJSONString(list);
            source.source(XContentType.JSON,s);
            bulkRequest.add(source);
        }
        try {
            BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *====================================
     * 搜索相关API
     *  * must(QueryBuilders) :   AND
     *      * mustNot(QueryBuilders): NOT
     *      * should(QueryBuilders) : OR
     */

    /**
     * 查询ES中所有的数据
     * @return
     */
    public SearchResponse SearchDocumentAll(String index,int from, int size){
        // 查询请求
        SearchRequest searchRequest = new SearchRequest(index);
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 查询所有
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        searchSourceBuilder.query(matchAllQueryBuilder);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        try {
            return  restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 精确匹配
     * @param filed es字段
     * @param value 字段对应的值
     */
    public SearchResponse SearchDocumenttermsQuery(String filed,int from, int size, Object value){
        SearchRequest searchRequest = new SearchRequest("coco");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termsQueryBuilder = QueryBuilders.termQuery(filed, value);
        searchSourceBuilder.query(termsQueryBuilder);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        try {
            return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 多条件精确查询，条件必须全部满足
     * @param maps 查询的数据，key为字段，value 为值
     */
    public <T> SearchResponse SearchDocumenttermsMultiTerm(Map<String,T> maps,int from, int size){
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        Set<Map.Entry<String, T>> entries = maps.entrySet();
        for (Map.Entry<String, T> entry : entries) {
            String key = entry.getKey();
            T value = entry.getValue();
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(key, value);
            boolQueryBuilder.must(termQueryBuilder); // AND
        }
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        try {
            return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 多条件精确查询，只需要满足其中某个条件即可查询出
     */
    public <T> SearchResponse SearchDocumenttermsMutilShould(Map<String,T> maps,int from, int size){
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        Set<Map.Entry<String, T>> entries = maps.entrySet();
        for (Map.Entry<String, T> entry : entries) {
            String key = entry.getKey();
            T value = entry.getValue();
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(key, value);
            boolQueryBuilder.should(termQueryBuilder); // OR
        }
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        try {
            return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 模糊查询，匹配 *value*
     */
    public SearchResponse SearchDocumentwildcardQuery(String filed,String value,int from, int size){
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders
                .wildcardQuery(filed, "*" + value + "*");
        searchSourceBuilder.query(wildcardQueryBuilder);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        try {
            return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 多条件模糊查询, 必须满足所有条件
     */
    public <T> SearchResponse SearchDocumenttermsMutillike(Map<String,T> maps,int from, int size){
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        Set<Map.Entry<String, T>> entries = maps.entrySet();
        for (Map.Entry<String, T> entry : entries) {
            String key = entry.getKey();
            T value = entry.getValue();
            WildcardQueryBuilder wildcardQueryBuilder =
                    QueryBuilders.wildcardQuery(key, "*" + value + "*");
            boolQueryBuilder.must(wildcardQueryBuilder); // AND
        }
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        try {
            return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 多条件模糊查询，满足任一条件即可
     */
    public <T> SearchResponse SearchDocumenttermsMutillike2(Map<String,T> maps,int from, int size){
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        Set<Map.Entry<String, T>> entries = maps.entrySet();
        for (Map.Entry<String, T> entry : entries) {
            String key = entry.getKey();
            T value = entry.getValue();
            WildcardQueryBuilder wildcardQueryBuilder =
                    QueryBuilders.wildcardQuery(key, "*" + value + "*");
            boolQueryBuilder.should(wildcardQueryBuilder); // OR
        }
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        try {
            return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }










}
