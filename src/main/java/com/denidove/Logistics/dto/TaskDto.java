package com.denidove.Logistics.dto;

import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.nio.DoubleBuffer;

@Getter
@Setter
@NoArgsConstructor
public class TaskDto {

    private Long id;
    private String companyName;
    private String companyLogo;
    private String cargoName;
    private String startPoint;
    private String destination;
    private Double distance;
    private Double length;
    private Double width;
    private Double height;
    private Double weight;
    private Double price;
    private Integer days;
    private TaskStatus status;

    private static Long idIncrement = 0L; // данное статическое поле сохраняет свое значение внезависимости от сессии

    public TaskDto(String companyName, String cargoName, String startPoint, String destination,
                   Double distance, Double weight, Double price, Integer days, TaskStatus status) {
        this.companyName = companyName;
        this.cargoName = cargoName;
        this.startPoint = startPoint;
        this.destination = destination;
        this.distance = distance;
        this.weight = weight;
        this.price = price;
        this.days = days;
        this.status = status;
    }
}

