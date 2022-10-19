package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

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



    public static void main(String[] args) {
        SpringApplication.run(Log2Application.class, args);
    }



}
