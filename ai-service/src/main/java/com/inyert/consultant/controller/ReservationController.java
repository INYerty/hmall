package com.inyert.consultant.controller;

import com.inyert.consultant.po.Reservation;
import com.inyert.consultant.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping("/add")
    public Map<String, Object> addReservation(
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam String category,
            @RequestParam(required = false) String notes
    ) {
        try {
            // 将日期和时间组合成 LocalDateTime
            String dateTimeStr = date + "T" + time;
            LocalDateTime communicationTime = LocalDateTime.parse(dateTimeStr, 
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // 创建预约对象 - 包含前端表单的所有字段
            Reservation reservation = new Reservation(
                    null,                    // id 自动增长
                    name,                    // 姓名
                    phone,                   // 电话
                    communicationTime,       // 沟通时间（date + time组合）
                    email,                   // 邮箱
                    category,                // 购物类别
                    notes                    // 备注信息
            );

            reservationService.addReservation(reservation);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "预约成功！我们会尽快与你联系。");
            response.put("success", true);
            response.put("data", reservation);
            return response;

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "预约失败：" + e.getMessage());
            response.put("success", false);
            return response;
        }
    }

    @GetMapping("/query")
    public Map<String, Object> queryReservation(@RequestParam String phone) {
        try {
            Reservation reservation = reservationService.queryReservation(phone);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", reservation);
            response.put("success", true);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            response.put("success", false);
            return response;
        }
    }
}