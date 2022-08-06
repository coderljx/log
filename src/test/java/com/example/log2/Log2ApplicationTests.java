package com.example.log2;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;

@SpringBootTest
class Log2ApplicationTests {

    @Autowired
    Jedis jedis;

    @Test
    void contextLoads() {



    }

}
