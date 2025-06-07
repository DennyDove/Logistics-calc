package com.denidove.Logistics.entities;

import com.denidove.Logistics.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String cargoName;

    @Column
    private String startPoint;

    @Column
    private String destination;

    @Column
    private Double distance;

    @Column
    private Double weight;

    @Column
    private Double price;

    @ManyToOne
    private User user;

    @Column
    private Timestamp timeDate;

    @Column
    private TaskStatus status;

    public Task(Timestamp timeDate) {
        this.timeDate = timeDate;
    }
}
