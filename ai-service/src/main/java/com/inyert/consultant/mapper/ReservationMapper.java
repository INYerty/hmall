package com.inyert.consultant.mapper;


import com.inyert.consultant.po.Reservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReservationMapper {
    // 添加预约信息
    @Insert("insert into reservation(name, phone, communication_time, email, category, notes) values (#{name},#{phone},#{communicationTime},#{email},#{category},#{notes})")
    void insert(Reservation reservation);

    //根据手机号查询预约信息
    @Select("select * from reservation where phone = #{phone} order by id desc limit 1")
    Reservation findByPhone(String phone);

}
