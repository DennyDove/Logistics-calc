package com.denidove.Logistics.controllers;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.dto.UserDto;
import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.exceptions.CalcRequestException;
import com.denidove.Logistics.services.PostRequestService;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserService;
import com.denidove.Logistics.services.UserSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;

@Controller
public class CalcController {

    private final UserService userService;
    private  final UserSessionService userSessionService;
    private final TaskService taskService;
    private final PostRequestService postRequestService;

    public CalcController(TaskService taskService, UserService userService,
                          PostRequestService postRequestService, UserSessionService userSessionService) {
        this.userService = userService;
        this.taskService = taskService;
        this.userSessionService = userSessionService;
        this.postRequestService = postRequestService;
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
        Double heigth = taskDto.getHeigth();

        String volume = String.valueOf(width * length * heigth);
        String weight = String.valueOf(taskDto.getWeight());
        String oversizedVolume = volume;
        String oversizedWeight = weight;

        // Просто сохраняем состояние запроса пользователя
        taskService.saveToDto(taskDto);

        try {
            delivery = postRequestService.sendVozovozRequest(startPoint, destination, volume, weight);
        } catch (JsonProcessingException j) {
            j.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CalcRequestException("Bad request 404!");
        }

        try {
            delivery2 = postRequestService.sendDelLineRequest(startPoint, destination, length, width, heigth, volume, weight,
                    oversizedWeight, oversizedVolume);
        } catch (JsonProcessingException j) {
            j.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CalcRequestException("Bad request 404!");
        }

        //String price = delivery.get("response").get("basePrice").toString();
        String price2 = delivery2.get("data").get("price").toString();

        // Просто сохраняем состояние запроса пользователя
        //taskService.saveToDto(taskDto);

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
        //model.addAttribute("price", price);
        model.addAttribute("price2", price2);
        //toDo Сделать кнопку "Сохранить расчет"

        SecurityUser user = userSessionService.getSecurityUser();
        var userInit = user.getInitials();
        model.addAttribute("userInit", userInit);

        return "index_auth.html";
    }

}