package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.email.EmailService;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.entities.User;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final TaskService taskService;
    private final UserSessionService userSessionService;
    private final EmailService emailService;

    public OrderController(TaskService taskService, EmailService emailService, UserSessionService userSessionService) {
        this.taskService = taskService;
        this.emailService = emailService;
        this.userSessionService = userSessionService;
    }

    @PostMapping("/order")
    public ResponseEntity<TaskDto> saveOrder(@RequestParam String key) {
        Task task = new Task();
        User user = userSessionService.getSecurityUser().getUser();
        var taskDto = userSessionService.getTaskDto().get(key);
        task.setCompanyName(taskDto.getCompanyName());
        task.setCompanyLogo(taskDto.getCompanyLogo());
        task.setCargoName(taskDto.getCargoName());
        task.setStartPoint(taskDto.getStartPoint());
        task.setDestination(taskDto.getDestination());
        task.setWeight(taskDto.getWeight());
        task.setPrice(taskDto.getPrice());

        boolean authStatus = userSessionService.getAuthStatus();

        task.setUser(user);
        Long orderId = taskService.save(task);
        taskDto.setId(orderId);

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

        emailService.sendOrderEmail(taskDto, user); //, msgTopic, msgText);

        userSessionService.getTaskDto().clear();

        return ResponseEntity.ok().body(taskDto);
    }
}
