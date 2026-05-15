package com.inyert.consultant;

import com.inyert.consultant.po.Reservation;
import com.inyert.consultant.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class ReservationServiceTest {


    @Autowired
    private ReservationService reservationService;

    //测试添加
    @Test
    public void testInsert(){
        reservationService.addReservation(new Reservation(null,"张三","男","12345678901", LocalDateTime.now(),"上海",580));
    }

    //测试查询
    @Test
    void testFindByPhone(){
        reservationService.queryReservation("12345678901");
    }

}
