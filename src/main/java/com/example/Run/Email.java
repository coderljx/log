package com.example.Run;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Map;

@Controller
public class Email {
    private final Logger log = LoggerFactory.getLogger(Email.class);
    private final JavaMailSenderImpl javaMailSender;
    private final Configuration configuration; // 替换邮件模版中的内容

    private MimeMessage message;  // 邮件消息
    private MimeMessageHelper helper; // 邮件help类，内容以及附件等最终都是写在这个里面

    @SuppressWarnings ("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public Email(JavaMailSenderImpl javaMailSender,
                 Configuration configuration){
        this.javaMailSender = javaMailSender;
        this.configuration = configuration;
    }

    public void SetPerson (String from, String... to) {
        if (from.equals(""))
            return;

        message = javaMailSender.createMimeMessage();
        helper = new MimeMessageHelper(message);
        try {
            helper.setFrom(from);
            for (String s : to) {
                helper.addTo(s);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给邮件添加内容/主题等信息
     * @param Subject 主题
     * @param isHtml 是否html邮件
     * @param Contxt 邮件内容
     */
    public void SetContent( String Subject,boolean isHtml, String Contxt){
        try {
            this.helper.setSubject(Subject);
            this.helper.setText(Contxt,isHtml);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     *  替换邮件模版中的内容
     * @param TemplateName 需要替换的邮件模版名称
     * @param maps 替换的内容
     * @return 邮件的内容
     */
    public <T> String ReplaceParams(String TemplateName , Map<String,T> maps) {
        String Content = "";
        try {
            Template template = configuration.getTemplate(TemplateName);
            try {
                 Content = FreeMarkerTemplateUtils.processTemplateIntoString(template,maps);
            } catch (TemplateException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Content;
    }



    public boolean Send(){
        if (this.message != null){
            try {
                this.javaMailSender.send(message);
                return true;
            }catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }



}
