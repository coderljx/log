package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com")
@MapperScan("com.example.Dao")
@ServletComponentScan("com.example.Run")
@EnableElasticsearchRepositories (basePackages = "com.example.ES")
@EnableScheduling
public class Log2Application {

    @Value("${redis.host}")
    private String host;
    
//    @Bean
//    public RestHighLevelClient restHighLevelClient(){
//        RestHighLevelClient client = new RestHighLevelClient(
//                RestClient.builder(
//                        new HttpHost("10.0.0.183",9200)
//                )
//        );
//        return client;
//    }

    public static void main(String[] args) {
        SpringApplication.run(Log2Application.class, args);
    }



}
