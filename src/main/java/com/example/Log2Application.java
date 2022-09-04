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
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication(scanBasePackages = "com")
@MapperScan("com.example.Dao")
@ServletComponentScan("com.example.Run")
public class Log2Application {

//    @Bean
//    public RestHighLevelClient restHighLevelClient(){
//        RestHighLevelClient client = new RestHighLevelClient(
//                RestClient.builder(
////                        new HttpHost("10.0.0.183",9200)
//                        new HttpHost(host,9200)
//                )
//        );
//        return client;
//    }

    @Bean
    public ExecutorService ExecutorService () {
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        return executorService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Log2Application.class, args);
    }



}
