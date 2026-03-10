package com.denidove.Logistics.postreq;

import com.denidove.Logistics.dto.TaskDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delline")
public class DellineController {

    private final DellineService dellineService;

    public DellineController(DellineService dellineService) {
        this.dellineService = dellineService;
    }

    @PostMapping
    public ResponseEntity<TaskDto> calculate(HttpServletRequest request, @RequestBody TaskDto taskDto) {
        TaskDto result = dellineService.sendRequest(request, taskDto);
        return ResponseEntity.ok(result);
    }
}
