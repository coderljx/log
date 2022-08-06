package com.example.Run;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "email")
@Data
public class EmailProperties {
    private final Logger mylog = LoggerFactory.getLogger(EmailProperties.class);

    // 邮件发送人
    private String from;
    // 邮件接受人
    private List<String> to;
    // 抄送人邮箱
    private List<String> cc;

}
