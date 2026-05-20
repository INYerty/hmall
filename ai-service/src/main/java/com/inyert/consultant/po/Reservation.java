package com.inyert.consultant.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private Long id;
    private String name;
    private String phone;
    private LocalDateTime communicationTime;
    private String email;
    private String category;
    private String notes;
}
