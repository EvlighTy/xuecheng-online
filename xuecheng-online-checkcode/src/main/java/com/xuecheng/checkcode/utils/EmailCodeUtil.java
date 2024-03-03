package com.xuecheng.checkcode.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Date;

@Component
public class EmailCodeUtil {

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String username;

    public String sendEmailCode(String receiver){
        Context context = new Context(); //引入Template的Context
        String emailCode = RandomCodeUtil.getSixFigureCode(); //验证码
        context.setVariable("emailCode", Arrays.asList(emailCode.split("")));
        String process = templateEngine.process("emailCode.html", context); //模板
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        Session session = mimeMessage.getSession();
        //日志打印控制器
        session.setDebug(true);
        //解决本地DNS未配置 ip->域名场景下，邮件发送太慢的问题
        System.getProperties().setProperty("mail.mime.address.usecanonicalhostname", "false");
        session.getProperties().setProperty("mail.smtp.localhost", "192.168.31.95");
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setSubject("【学成在线】验证码"); //标题
            helper.setFrom(username); //发送者
            helper.setTo(receiver); //接收者
            helper.setSentDate(new Date()); //时间
            helper.setText(process, true); //第二个参数true表示这是一个html文本
        } catch (Exception e) {
            throw new RuntimeException("邮件发送异常");
        }
        javaMailSender.send(mimeMessage);
        return emailCode;
    }
    
}
