package com.denidove.Logistics.dto;

import com.denidove.Logistics.enums.City;
import com.denidove.Logistics.enums.TaskStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseDto {

    private Long id;
    private String cargoName;
}

