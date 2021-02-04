package com.mintools.sendMailUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.mail.MailUtil;

import java.util.ArrayList;
import java.util.Date;

public class SendEmail {
    public static void sendEmail(String content) {
        // 收件人电子邮箱
        ArrayList<String> tos = CollUtil.newArrayList(
                "mintools@163.com",
                "jenkinsplatform@126.com");
        // 邮件标题
        Date date = DateUtil.date();
        String subject = "代码上库规范性审计（" + date + "）";
        // 邮件正文
//        String content = "<h1>邮件测试</h1>";
        // 是否为HTML
        boolean isHtml = true;

//        MailUtil.send("mintools@163.com", subject, content, isHtml);
        MailUtil.send(tos, subject, content, isHtml);
    }
}
