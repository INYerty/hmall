package com.inyert.consultant.service.impl;

import com.inyert.consultant.mapper.ReservationMapper;
import com.inyert.consultant.po.Reservation;
import com.inyert.consultant.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReservationImpl implements ReservationService {

    @Autowired
    private ReservationMapper reservationMapper;
    @Override
    public void addReservation(Reservation reservation) {
        reservationMapper.insert(reservation);
    }

    @Override
    public Reservation queryReservation(String phone) {
        return reservationMapper.findByPhone(phone);
    }
}
