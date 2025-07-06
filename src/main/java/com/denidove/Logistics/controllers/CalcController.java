package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.exceptions.CalcRequestException;
import com.denidove.Logistics.exceptions.DellineRequestException;
import com.denidove.Logistics.exceptions.IncorrectDimensionException;
import com.denidove.Logistics.postreq.DellineService;
import com.denidove.Logistics.postreq.VozService;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Controller
public class CalcController {

    private final UserService userService;
    private  final UserSessionService userSessionService;
    private final TaskService taskService;
    private final VozService vozService;
    private final DellineService dellineService;

    public CalcController(TaskService taskService, UserService userService, VozService vozService,
                          DellineService dellineService, UserSessionService userSessionService) {
        this.userService = userService;
        this.taskService = taskService;
        this.vozService = vozService;
        this.dellineService = dellineService;
        this.userSessionService = userSessionService;

    }

    @PostMapping("/calc")
    public String logisticCalc(Model model, @ModelAttribute("task") TaskDto taskDto, UserDto userDto) {

        model.addAttribute("task", taskDto);

        List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Vologda);

        //toDO сделать ещё один end-point "/order-auth" там данные будут отправляться через JavaScript
        // @ModelAttribute("task") в данном случае уже не работает т.к. из формы удалено th:object
        // Только th:object работает с @ModelAttribute("task")
        var delivery = new LinkedHashMap<String, LinkedHashMap>();
        var delivery2 = new LinkedHashMap<String, LinkedHashMap>();

        String startPoint = taskDto.getStartPoint().getName();
        String destination = taskDto.getDestination().getName();
        Double width = taskDto.getWidth();
        Double length = taskDto.getLength();
        Double height = taskDto.getHeigth();

        String volume = String.valueOf(width * length * height);
        String weight = String.valueOf(taskDto.getWeight());
        String oversizedVolume = volume;
        String oversizedWeight = weight;

        // Просто сохраняем состояние запроса пользователя
        taskService.saveToDto(taskDto);

        String price = "";
        String price2 = "";

        try {
            price = "нет результатов";
            userSessionService.getLogisticPrice().add(price);
            delivery = vozService.sendRequest(startPoint, destination, length.floatValue(), width.floatValue(),
                    height.floatValue(), Float.valueOf(weight), volume);

            if(delivery.get("response").get("basePrice") != null) {
                price = delivery.get("response").get("basePrice").toString();
                userSessionService.getLogisticPrice().set(0, price);
            }
        } catch (JsonProcessingException j) {
            j.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CalcRequestException("Bad request 404!");
        }

        try {
            price2 = "нет результатов";
            userSessionService.getLogisticPrice().add(price2);
            delivery2 = dellineService.sendRequest(startPoint, destination, length, width, height, volume, weight,
                    oversizedWeight, oversizedVolume);
            if(delivery2.get("data").get("price") != null) {
                price2 = delivery2.get("data").get("price").toString();
                userSessionService.getLogisticPrice().set(1, price2);
            }
        } catch (JsonProcessingException j) {
            j.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DellineRequestException("Bad parameters!");
        }

        Task task = new Task();
        task.setCargoName(taskDto.getCargoName());
        task.setStartPoint(taskDto.getStartPoint().getName());
        task.setDestination(taskDto.getDestination().getName());
        task.setWeight(taskDto.getWeight());
        task.setPrice(Double.parseDouble(price2));

        task.setUser(userSessionService.getSecurityUser().getUser());
        Long orderId = taskService.save(task);

        model.addAttribute("cities", cities);
        model.addAttribute("user", userDto);
        model.addAttribute("price", price);
        model.addAttribute("price2", price2);
        //toDo Сделать кнопку "Сохранить расчет"

        SecurityUser user = userSessionService.getSecurityUser();
        var userInit = user.getInitials();
        model.addAttribute("userInit", userInit);

        return "index_auth.html";
    }

}