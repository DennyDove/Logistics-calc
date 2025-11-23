package com.denidove.Logistics.email;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.UserSessionService;
import com.denidove.Logistics.utils.NIO;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.servlet.http.HttpServletRequest;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;

@Service
public class SimpleMailService {

    private final UserSessionService userSessionService;

    public SimpleMailService(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    public void sendOrderEmail(TaskDto taskDto, User user) throws Exception {

    var compLogoPath = String.format("/static/images/%s", taskDto.getCompanyLogo());
    var logoPath = "/static/images/logo.jpg";

    //toDo дописать комментарий
    // Загружаем не файл, а ресурс методом getResourceAsStream() иначе в пакетном jar-файлк будет выдаваться ошибка ...
    var htmlFile = NIO.readResource("/static/email_order.html");

    // Вставляем в полученный текст переменные s%
    var msgText = String.format(htmlFile, user.getName(), taskDto.getId(), taskDto.getCargoName(), taskDto.getStartPoint(), taskDto.getDestination(),
            taskDto.getPrice(), taskDto.getDays());
    System.out.println(msgText);

    Email email = EmailBuilder.startingBlank()
            .from("dnis@mail.ru")
            .to(user.getEmail())
            .withSubject("Заказ на сайте Skylar")
            .withPlainText("Заказ на сайте Skylar")
            .withHTMLText(msgText)
            .withEmbeddedImage("company_logo", new ByteArrayDataSource(SimpleMailService.class.getResourceAsStream(compLogoPath), "image/jpeg"))
            .withEmbeddedImage("logo", new ByteArrayDataSource(SimpleMailService.class.getResourceAsStream(logoPath), "image/jpeg"))
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

    public void notifyLogisticCompany(TaskDto taskDto, User user) throws Exception {

        var logoPath = "/static/images/logo.jpg";

        //toDo дописать комментарий
        // Загружаем не файл, а ресурс методом getResourceAsStream() иначе в пакетном jar-файлк будет выдаваться ошибка ...
        var htmlFile = NIO.readResource("/static/email_notify_company.html");


        // Вставляем в полученный текст переменные s%
        var msgText = String.format(htmlFile, taskDto.getCompanyName(), taskDto.getId(), taskDto.getCargoName(),
                taskDto.getStartPoint(), taskDto.getDestination(), taskDto.getLength(), taskDto.getWidth(), taskDto.getHeight(),
                taskDto.getWeight(), taskDto.getPrice(), taskDto.getDays(), user.getName(), user.getPhone());
        //System.out.println(msgText);

        Email email = EmailBuilder.startingBlank()
                .from("dnis@mail.ru")
                //.to("List of recipients","fedor.sklyar@gmail.com", "d.v.golub@yandex.ru", "dnis@mail.ru")
                //.ccMultiple
                .toMultiple("d.v.golub@yandex.ru", "fedor.sklyar@gmail.com")
                .withSubject("Новый заказ на сайте Skylar")
                .withPlainText("Новый заказ на сайте Skylar")
                .withHTMLText(msgText)
                .withEmbeddedImage("logo", new ByteArrayDataSource(SimpleMailService.class.getResourceAsStream(logoPath), "image/jpeg"))
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

    public void sendRegEmail(User user, String randomCode) throws Exception {

        var logoPath = "/static/images/logo.jpg";

        String siteUrl = userSessionService.getSiteUrl(user.getLogin());
        String verifyUrl = siteUrl + "/verify?code=" + randomCode;

        //toDo дописать комментарий
        // Загружаем не файл, а ресурс методом getResourceAsStream() иначе в пакетном jar-файлк будет выдаваться ошибка ...
        var htmlFile = NIO.readResource("/static/email_confirm.html");
        // Вставляем в полученный текст переменные s%
        var msgText = String.format(htmlFile, verifyUrl);
        System.out.println(msgText);


        Email email = EmailBuilder.startingBlank()
                .from("dnis@mail.ru")
                .to(user.getEmail())
                .withSubject("Регистрация на сайте Skylar")
                .withPlainText("Регистрация на сайте Skylar")
                .withHTMLText(msgText)
                .withEmbeddedImage("logo", new ByteArrayDataSource(SimpleMailService.class.getResourceAsStream(logoPath), "image/jpeg"))
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

    public void sendLoginEmail(User user, String randomCode) throws Exception {

        var logoPath = "/static/images/logo.jpg";

        //toDo дописать комментарий
        // Загружаем не файл, а ресурс методом getResourceAsStream() иначе в пакетном jar-файлк будет выдаваться ошибка ...
        var htmlFile = NIO.readResource("/static/login_confirm.html");
        // Вставляем в полученный текст переменные s%
        var msgText = String.format(htmlFile, user.getName(), randomCode);
        System.out.println(msgText);


        Email email = EmailBuilder.startingBlank()
                .from("dnis@mail.ru")
                .to(user.getEmail())
                .withSubject("Одноразовый пароль для входа в сервис Skylar")
                .withPlainText("Одноразовый пароль для входа в сервис Skylar")
                .withHTMLText(msgText)
                .withEmbeddedImage("logo", new ByteArrayDataSource(SimpleMailService.class.getResourceAsStream(logoPath), "image/jpeg"))
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

    public void sendResetEmail(String siteUrl, User user, String token) throws Exception {

        //toDo было и стало выдавать null:
        //String siteUrl = userSessionService.getResetSiteUrl(user.getLogin());

        //toDo создать отдельный /reset - ???
        String resetUrl = siteUrl + "/reset?token=" + token; // token генерируется в контроллере VerifyController, метод @PostMapping("/reset-mail")

        var logoPath = "/static/images/logo.jpg";

        //toDo дописать комментарий
        // Загружаем не файл, а ресурс методом getResourceAsStream() иначе в пакетном jar-файлк будет выдаваться ошибка ...
        var htmlFile = NIO.readResource("/static/reset_template.html");
        // Вставляем в полученный текст переменные s%
        var msgText = String.format(htmlFile, resetUrl);
        System.out.println(msgText);


        Email email = EmailBuilder.startingBlank()
                .from("dnis@mail.ru")
                .to(user.getEmail())
                .withSubject("Восстановление пароля на сервисе Skylar")
                .withPlainText("Восстановление пароля на сервисе Skylar")
                .withHTMLText(msgText)
                .withEmbeddedImage("logo", new ByteArrayDataSource(SimpleMailService.class.getResourceAsStream(logoPath), "image/jpeg"))
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

    //toDo удалить - это критическач уязвимость!
    /*
    public void sendNewPassword(User user, String newPassword) throws Exception {

        var logoPath = "/static/images/logo.jpg";

        // Загружаем не файл, а ресурс методом getResourceAsStream() иначе в пакетном jar-файлк будет выдаваться ошибка ...
        var htmlFile = NIO.readResource("/static/email_newpass.html");
        // Вставляем в полученный текст переменные s%
        var msgText = String.format(htmlFile, user.getName(), newPassword);
        System.out.println(msgText);


        Email email = EmailBuilder.startingBlank()
                .from("dnis@mail.ru")
                .to(user.getEmail())
                .withSubject("Восстановление пароля на сервисе Skylar")
                .withPlainText("Восстановление пароля на сервисе Skylar")
                .withHTMLText(msgText)
                .withEmbeddedImage("logo", new ByteArrayDataSource(SimpleMailService.class.getResourceAsStream(logoPath), "image/jpeg"))
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
    }*/

}
