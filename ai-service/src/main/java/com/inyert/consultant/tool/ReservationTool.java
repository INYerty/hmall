package com.inyert.consultant.tool;

import com.inyert.consultant.po.Reservation;
import com.inyert.consultant.service.ReservationService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReservationTool {

    @Autowired
    private ReservationService reservationService;
    @Tool("添加志愿指导服务信息")
    public void insertReservation(
            @P("考生姓名") String name,
            @P("考生性别") String gender,
            @P("考生电话") String phone,
            @P("沟通时间:格式为yyyy-MM-dd'T'HH:mm") String communicationTime,
            @P("考生所在省份") String province,
            @P("考生预估分数") Integer estimatedScore
    ){
        reservationService.addReservation(new Reservation(null,name,gender,phone,LocalDateTime.parse(communicationTime),province,estimatedScore));
    }

    @Tool("查询志愿指导服务信息")
    public Reservation queryReservation(@P("考生电话") String phone){
        return reservationService.queryReservation(phone);
    }
}
