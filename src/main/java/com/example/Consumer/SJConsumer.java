package com.example.Consumer;


import com.alibaba.fastjson.JSONObject;
import com.example.Pojo.comptroller;
import com.example.Service.ComptrollerService;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@RocketMQMessageListener (
        consumerGroup = "sjconsumer",
        topic = "sj",
        selectorExpression = "config"
)
public class SJConsumer implements RocketMQListener<MessageExt> {
    private final Logger log = LoggerFactory.getLogger(LogConsumer.class);

    @Resource
    private  ComptrollerService comptrollerService;

    @Override
    public void onMessage(MessageExt messageExt) {
            String data = new String(messageExt.getBody());
            log.info(data);
            comptroller parsecomptroller = JSONObject.parseObject(data, comptroller.class);
            this.comptrollerService.Insertsj(parsecomptroller);
    }

}
