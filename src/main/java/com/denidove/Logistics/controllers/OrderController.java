package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.email.EmailService;
import com.denidove.Logistics.email.SimpleMailService;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.security.jwt.JwtService;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final TaskService taskService;
    private final UserSessionService userSessionService;
    private final EmailService emailService;
    private final SimpleMailService simpleMailService;
    private final JwtService jwtService;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    public OrderController(TaskService taskService, EmailService emailService,
                           SimpleMailService simpleMailService, UserSessionService userSessionService,
                           JwtService jwtService) {
        this.taskService = taskService;
        this.emailService = emailService;
        this.simpleMailService = simpleMailService;
        this.userSessionService = userSessionService;
        this.jwtService = jwtService;
    }

    @PostMapping("/api/order")
    public ResponseEntity<?> saveOrder(@RequestParam(value = "key", required = true) String key,
                                             HttpServletRequest request) {

        //toDo закомментили, т.к. ChatGPT рекомендовал: пусть Spring Security сам возвращает 401, а контроллер этим вообще не занимается.
        /*
        if(!isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }*/

        Task task = new Task();
        User user = userSessionService.getSecurityUser().getUser();

        //toDo Исправить, чтобы находила конкретную компанию - key
        var taskDto = userSessionService.getTaskDto(user.getLogin(), key); //toDo get(key) - DONE!

        task.setCompanyName(taskDto.getCompanyName());
        task.setCompanyLogo(taskDto.getCompanyLogo());
        task.setCargoName(taskDto.getCargoName());
        task.setStartPoint(taskDto.getStartPoint());
        task.setDestination(taskDto.getDestination());
        task.setWeight(taskDto.getWeight());
        task.setPrice(taskDto.getPrice());

        task.setUser(user);
        Long orderId = taskService.save(task);
        taskDto.setId(orderId);

        var guestId = userSessionService.getGuestIdFromCookie(request);
        userSessionService.clearGuestData(guestId); // Для обнуления данных в форме

        /*
        String msgTopic = "Заказ на сайте Logistics.pro";
        String rawText = """
                        <p>Добрый день, %s!</p>
                        <p>Ваш заказ № %s оформлен в работу</p>
                        <b> %s </b> руб., минимальный срок доставки: %s дн.
                        <br>
                        <p>С уважением,</p>
                        Команда ООО "Логистик Плюс"
                        """;
        var msgText = String.format(rawText, user.getName(), orderId, taskDto.getPrice(), taskDto.getDays());*/
        //emailService.sendOrderEmail(taskDto, user);
        try {
            simpleMailService.sendOrderEmail(taskDto, user);
            simpleMailService.notifyLogisticCompany(taskDto, user);
        } catch (Exception e) {
            log.error("Ошибка при отправке e-mail уведомлений: {}", e.getMessage());
        }

        return ResponseEntity.ok().body(taskDto);
    }

    // Важно!
    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }
}
