package com.denidove.Logistics.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.eclipse.angus.mail.smtp.SMTPMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class EmailService {

    public void sendEmail(String recipient, String siteUrl, String code) {

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
            Message message = buidMsg(session, props.getProperty("mail.smtp.username"),
                    recipient, siteUrl, code);
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

    public Message buidMsg(Session session, String sender, String recipient, String siteUrl, String code) throws MessagingException, IOException {
        Message message = new MimeMessage(session); //new MimeMessage(session);

        message.setFrom(new InternetAddress(sender));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject("Mail Subject");

        String verifyUrl = siteUrl + "/verify?code=" + code;

        String msg = "Привет всем! <br>" +
                "Please click the link below to verify your registration" +
                "<a href=\"" + verifyUrl + "\"> Verify account </a>";

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
}
