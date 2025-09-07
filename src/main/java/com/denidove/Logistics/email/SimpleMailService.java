package com.denidove.Logistics.email;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.UserSessionService;
import com.denidove.Logistics.utils.NIO;
import jakarta.activation.FileDataSource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.Properties;

@Service
public class SimpleMailService {

    private final UserSessionService userSessionService;

    public SimpleMailService(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    public void sendOrderEmail(TaskDto taskDto, User user) {

    String filePath = String.format("src/main/resources/static/images/%s", taskDto.getCompanyLogo());
    var file = new File(filePath);
    var file1 = new File("src/main/resources/static/images/logo.jpg");
    //var path = file.getAbsolutePath();

    // Загружаем текст прямо из файла:
    var path = Path.of("src/main/resources/static/email_order.html");
    var htmlFile = NIO.readFile(path);
    // Вставляем в полученный текст переменные s%
    var msgText = String.format(htmlFile, user.getName(), taskDto.getId(), taskDto.getPrice(), taskDto.getDays());
    System.out.println(msgText);

    Email email = EmailBuilder.startingBlank()
            .from("dnis@mail.ru")
            .to("dnis@mail.ru")
            .withSubject("Заказ на сайте Logistics.pro")
            .withPlainText("Заказ на сайте Logistics.pro")
            .withHTMLText(msgText)
            .withEmbeddedImage("company_logo", new FileDataSource(file))
            .withEmbeddedImage("logo", new FileDataSource(file1))
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

    //toDo
    public void sendRegEmail(User user, String randomCode) {

        var file = new File("src/main/resources/static/images/logo.jpg");
        //var path = file.getAbsolutePath();

        String siteUrl = userSessionService.getSiteUrl();
        String verifyUrl = siteUrl + "/verify?code=" + randomCode;

        // Загружаем текст прямо из файла:
        var path = Path.of("src/main/resources/static/email_confirm.html");
        var htmlFile = NIO.readFile(path);
        // Вставляем в полученный текст переменные s%
        var msgText = String.format(htmlFile, verifyUrl);
        System.out.println(msgText);


        Email email = EmailBuilder.startingBlank()
                .from("dnis@mail.ru")
                .to(user.getEmail())
                .withSubject("Регистрация на сайте Logistics.pro")
                .withPlainText("Регистрация на сайте Logistics.pro")
                .withHTMLText(msgText)
                .withEmbeddedImage("logo", new FileDataSource(file))
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
}
