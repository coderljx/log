package com.example;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SpringBootApplication(scanBasePackages = "com")
@MapperScan("com.example.Dao")
@ServletComponentScan("com.example.Run")
@EnableElasticsearchRepositories (basePackages = "com.example.ES")
public class Log2Application {

    @Value("${redis.host}")
    private String host;

    @Bean
    public Jedis Setjedis(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(30);
        config.setMaxIdle(30);
        JedisPool pool = new JedisPool(config,host,6379);
        return pool.getResource();
    }
    
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("10.0.0.183",9200)
                )
        );
        return client;
    }

    public static void main(String[] args) {
        SpringApplication.run(Log2Application.class, args);
    }



}
