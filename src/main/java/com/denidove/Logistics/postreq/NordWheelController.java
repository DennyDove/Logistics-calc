package com.denidove.Logistics.postreq;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.exceptions.BadRequestException;
import com.denidove.Logistics.exceptions.CalcRequestException;
import com.denidove.Logistics.exceptions.IncorrectDimensionException;
import com.denidove.Logistics.json.NordWheel;
import com.denidove.Logistics.json.NordWheelCities;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

@RestController
public class NordWheelController {

    private  final UserSessionService userSessionService;
    private final TaskService taskService;
    private final NordWheelService nordWheelService;

    public NordWheelController(TaskService taskService, NordWheelService nordWheelService,
                               UserSessionService userSessionService) {
        this.taskService = taskService;
        this.nordWheelService = nordWheelService;
        this.userSessionService = userSessionService;

    }

    @PostMapping("/nordwcalc")
    public ResponseEntity<TaskDto> nordWheelCalc(@RequestBody TaskDto taskDto) {

        //List<City> cities = List.of(City.Moscow, City.Piter, City.Saratov, City.Sochi);

        var delivery = new NordWheel();
        var nordWheelCities = new NordWheelCities();

        Integer startPoint = 0;
        Integer destination = 0;
        Double width = taskDto.getWidth();
        Double length = taskDto.getLength();
        Double height = taskDto.getHeight();

        Double volume = width * length * height;
        Double weight = taskDto.getWeight();

        if(length > 12.9 || width > 2.4 || height > 2.4) throw new IncorrectDimensionException("Весогабаритные характеристики груза превышают допустимые!");
        String errorMsg = ""; // переменная для записи сообщения об ошибке, см. ниже в блоке try-catch

        try {
            //toDo Создать отдельный метод для чистоты кода
            // также попробосоздать создать обработку ошибки, если город не найден
            nordWheelCities = nordWheelService.getCityList();
            var cityList = nordWheelCities.getData();
            for(NordWheelCities.Data c : cityList) {
                if(c.getName().equals(taskDto.getStartPoint())) {
                    startPoint = c.getId();
                }
                if(c.getName().equals(taskDto.getDestination())) {
                    destination = c.getId();
                }
            }
            if(startPoint == 0 || destination == 0) {
                errorMsg = "Указанный город отправления или доставки не обслуживается";
                throw new BadRequestException(errorMsg);
            }

            delivery = nordWheelService.sendRequest(startPoint, destination, weight.floatValue(), volume.floatValue());

            if(delivery.getData().getTotal() != null) {
                Double price = delivery.getData().getTotal();
                Integer days = delivery.getData().getDays();
                //toDo перенести это в сервисный класс
                taskDto.setCompanyName("Nord Wheel");
                taskDto.setCompanyLogo("nordw.jpg");
                taskDto.setPrice(price);
                taskDto.setDays(days);

                // Просто сохраняем состояние запроса пользователя
                taskService.saveToDto("nordw", taskDto);
            }
        } catch (JsonProcessingException j) {
            j.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CalcRequestException("Некорректные параметры запроса. " + errorMsg);
        }

        return ResponseEntity
                //.status(HttpStatus.FOUND)
                //.location(URI.create("http://localhost:8080/products"))
                .ok(taskDto);
    }
}