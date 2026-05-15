package com.inyert.consultant.service;

import com.inyert.consultant.po.Reservation;

public interface ReservationService {
    //添加预约信息
    void addReservation(Reservation reservation);

    //查询预约信息
    Reservation queryReservation(String phone);
}
