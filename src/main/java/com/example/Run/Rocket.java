package com.example.Run;


import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class Rocket {

    private final RocketMQTemplate template;

    @Autowired
    public Rocket(RocketMQTemplate template){
        this.template = template;
    }

    public RocketMQTemplate Get(){
        return this.template;
    }

    public void Send( String topic, Object Content){
        if ( topic != null && Content != null ){
            this.template.convertAndSend(topic, Content);
        }else {
            throw new RuntimeException("生产主题不可为空");
        }
    }

    /**
     * 生产消息
     * @param topic 消息的主题
     * @param tag  消息的标签，用作消费者二次区分的
     * @param Content 消息的内容
     */
    public void Send( String topic, String tag , Object Content){
        if ( topic != null && tag != null ){
            String Links = this.Links(topic, tag);
            this.template.convertAndSend(Links, Content);
        }
    }

    public void AsyncSend(String topic,  Object Content, SendCallback callback){
        if ( topic.equals("") )
            return;

        this.template.asyncSend(topic, Content,callback);
    }

    public void AsyncSend(String topic, String tag , Object Content,SendCallback callback){
        if (!topic.equals("") && tag != null ){
            String Links = this.Links(topic, tag);

            this.template.asyncSend(Links, Content, callback);
        }

    }

    /**
     * 生产者发送的消息按照队列进行发送，
     * 保证一条消息都是在同一个队列中的
     */
    public void SendQueue(String topic, String tag, Object Content,String key){
        // 想让设置的队列生效，则生产者生产的消息必须使用Orderly结尾的方法
        this.SetMessageQueue();
        String Links = this.Links(topic, tag);
        this.template.syncSendOrderly(Links,Content,key);
    }


    private void SetMessageQueue(){
        this.template.setMessageQueueSelector(new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> list, Message message, Object o) {
                return list.get(1);
            }
        });
    }

    private String Links(String topic, String tag) {
        return topic + ":" + tag;
    }


}
