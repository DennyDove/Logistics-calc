package com.denidove.Logistics.postreq;

import com.denidove.Logistics.dto.TaskDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.client.HttpStatusCodeException;

public interface DellineService {

    // Отправка POST-запроса на сервис Delline
    public TaskDto sendRequest(HttpServletRequest request, TaskDto taskDto);

    // Получение абсолютного сетевого пути из текушей среды - в новой архитектуре не используется
    //public String getBaseUrl(HttpServletRequest request);

}

