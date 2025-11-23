package com.denidove.Logistics.postreq;

import com.denidove.Logistics.dto.TaskDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;


public interface VozService {

    // Отправка POST-запроса на сервис Vozavoz
    public TaskDto sendRequest(HttpServletRequest request, TaskDto taskDto) throws JsonProcessingException;

    // Получение абсолютного сетевого пути из текушей среды
    public String getBaseUrl(HttpServletRequest request);
}