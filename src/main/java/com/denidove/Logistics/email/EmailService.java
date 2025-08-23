package com.denidove.Logistics.email;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.eclipse.angus.mail.smtp.SMTPMessage;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class EmailService {

    private final UserSessionService userSessionService;

    public EmailService(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    public void sendOrderEmail(TaskDto taskDto, User user) {
    //public void sendOrderEmail(User user, String msgTopic, String msgText, String fileName) {
        var filePath = String.format("src/main/resources/static/images/%s", taskDto.getCompanyLogo());
        var file = new File(filePath);

        String msgTopic = "Заказ на сайте Logistics.pro";
        String htmlContent = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                </head>
                <body>
                <p>Добрый день, %s!</p>
                <p>Ваш заказ № %s оформлен в работу!</p>
                <div><img src="cid:company_logo" alt="Company Logo" width="95" height="33"></div> <div> стоимость: <b> %s </b> руб., минимальный срок доставки: %s дн.</div>
                <p>С уважением,</p>
                Команда ООО "Логистик Плюс" <br>
                тел.: +7(916) 007-42-35
                </body>
                </html>
                
                """;
        var msgText = String.format(htmlContent, user.getName(), taskDto.getId(), taskDto.getPrice(), taskDto.getDays());

        /*String htmlContent = "<h1>This is an email with HTML content</h1>" +
                "<p>This email body contains additional information and formatting.</p>" +
                "<img src=\"cid:company_logo\" alt=\"Company Logo\">"; */

        Email email = EmailBuilder.startingBlank()
                .from("dnis@mail.ru")
                .to("dnis@mail.ru")
                .withSubject(msgTopic)
                .withPlainText("This is a test email sent using SJM.")
                .withHTMLText(msgText)
                .withEmbeddedImage("company_logo", new FileDataSource(file))
                .buildEmail();

        // Дополнительная конфигурация
        Properties properties = new Properties();
        properties.put("mail.smtp.auth" , "true");
        properties.put("mail.smtp.ssl.enable" , "true");

        Mailer mailer = MailerBuilder
                .withSMTPServer("smtp.mail.ru", 465, "dnis@mail.ru", "IrOwOQnzyXlH4J0T3dNT")
                .withProperties(properties)
                .buildMailer();
        mailer.sendMail(email);
    }

    public void sendRegistrationEmail(User user, String code) {
        Properties props = new Properties();
        Properties conf = new Properties();

        try {
            conf = loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

            props.put("mail.smtp.auth", conf.getProperty("mail.smtp.auth"));
            props.put("mail.smtp.ssl.enable", conf.getProperty("mail.smtp.ssl.enable"));
            props.put("mail.smtp.host", conf.getProperty("mail.smtp.host"));
            props.put("mail.smtp.port", conf.getProperty("mail.smtp.port"));
            props.put("mail.smtp.username", conf.getProperty("mail.smtp.username"));
            props.put("mail.smtp.password", conf.getProperty("mail.smtp.password"));

        Session session = Session.getDefaultInstance(props);
            session.setDebug(true); // для вывода в консоль процесса отправки письма

            try {
            Message message = buildRegistrationMsg(session, props.getProperty("mail.smtp.username"),
                    user, code);
            Transport.send(message, props.getProperty("mail.smtp.username"),
                    props.getProperty("mail.smtp.password"));
        } catch (
        MessagingException m) {
            m.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendLoginEmail(User user, String code) {
        Properties props = new Properties();
        Properties conf = new Properties();

        try {
            conf = loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        props.put("mail.smtp.auth", conf.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.ssl.enable", conf.getProperty("mail.smtp.ssl.enable"));
        props.put("mail.smtp.host", conf.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", conf.getProperty("mail.smtp.port"));
        props.put("mail.smtp.username", conf.getProperty("mail.smtp.username"));
        props.put("mail.smtp.password", conf.getProperty("mail.smtp.password"));

        Session session = Session.getDefaultInstance(props);
        session.setDebug(true); // для вывода в консоль процесса отправки письма

        try {
            Message message = buildLoginMsg(session, props.getProperty("mail.smtp.username"),
                    user, code);
            Transport.send(message, props.getProperty("mail.smtp.username"),
                    props.getProperty("mail.smtp.password"));
        } catch (
                MessagingException m) {
            m.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public Properties loadProperties() throws IOException {
        Properties configuration = new Properties();
        InputStream inputStream = EmailService.class
                .getClassLoader()
                .getResourceAsStream("application.properties");
        configuration.load(inputStream);
        inputStream.close();
        return configuration;
    }

    public Message buildOrderMsg(Session session, String sender, User user, String msgTopic, String msgText) throws MessagingException, IOException {
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(sender));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(user.getEmail()+", d.v.golub@yandex.ru, fedor.sklyar@gmail.com"));
        message.setSubject(msgTopic);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msgText, "text/html; charset=utf-8");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        message.setContent(multipart);
        return message;
    }

    public Message buildRegistrationMsg(Session session, String sender, User user, String code) throws MessagingException, IOException {
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(sender));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
        message.setSubject("Подтверждение регистрации на сайте Logistics.pro");

        String siteUrl = userSessionService.getSiteUrl();
        String verifyUrl = siteUrl + "/verify?code=" + code;

        String msgText = """
                        <p>Добрый день, %s!<p>
                        Для подтверждения прохождения регистрации пройдите по ссылке ниже:<br>
                        <p><a href="%s">Подтвердить учетную запись</a></p>
                        <br>
                        С уважением,<br>
                        Ваша IT команда ООО "Логистик Плюс"
                        """;
        var msg = String.format(msgText, user.getName(), verifyUrl);

        //toDo Для отправки вложений (картинки и т.д.)
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

        //MimeBodyPart fileAttachment = new MimeBodyPart();
        //fileAttachment.attachFile("d:/DennyDove/Photos/WhatsApp Image 2024-12-08 at 13.29.14 (1).jpeg");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        //multipart.addBodyPart(fileAttachment);
        message.setContent(multipart);
        return message;
    }

    public Message buildLoginMsg(Session session, String sender, User user, String verifyCode) throws MessagingException, IOException {
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(sender));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
        message.setSubject("Одноразовый пароль для входа в сервис Logistics.pro");

        String msgText = """
                        <p>Добрый день, %s!<p>
                        Ваш одноразовый пароль для входа на сервис Logistics.pro<br>
                        <p>%s</p>
                        <br>
                        С уважением,<br>
                        Ваша IT команда ООО "Логистик Плюс"
                        """;

        var msg = String.format(msgText, user.getName(), verifyCode);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        message.setContent(multipart);
        return message;
    }
}
