package com.denidove.Logistics.dto;

import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class TaskDto {



    private String cargoName;

    private City startPoint;

    private City destination;

    private Double distance;

    private Double weight;

    private TaskStatus status;

    private static Long idIncrement = 0L; // данное статическое поле сохраняет свое значение внезависимости от сессии

    public TaskDto(String cargoName, City startPoint, City destination,
                   Double distance, Double weight, TaskStatus status) {
        this.cargoName = cargoName;
        this.startPoint = startPoint;
        this.destination = destination;
        this.distance = distance;
        this.weight = weight;
        this.status = status;
    }
}

