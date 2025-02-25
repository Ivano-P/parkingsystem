package com.parkit.parkingsystem.constants;

public class DBConstants {
    public static final String GET_NEXT_PARKING_SPOT = "select min(PARKING_NUMBER) from parking where AVAILABLE = true and TYPE = ?";
    public static final String UPDATE_PARKING_SPOT = "update parking set available = ? where PARKING_NUMBER = ?";
    
    public static final String SAVE_TICKET = "insert into ticket(ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?,?)"; //added id, to set id when inserting ticket in db
    public static final String UPDATE_TICKET = "update ticket set PRICE=?, OUT_TIME=? where ID=?";    
    public static final String GET_TICKET = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? order by t.IN_TIME desc limit 1";
    
    //will be considered recurring if there is a ticket with this registration number, in-time and out-time
    public static final String CHECK_IF_RECURRING_CUSTOMER = "SELECT * FROM ticket WHERE VEHICLE_REG_NUMBER = ? AND IN_TIME IS NOT NULL AND OUT_TIME IS NOT NULL"; 
    //if there is ticket with registration number and in-time but no out-time, vehicle is in parking
    public static final String CHECK_IF_VEHICLE_ALREADY_IN_PARKING = "SELECT * FROM ticket WHERE VEHICLE_REG_NUMBER = ? AND IN_TIME IS NOT NULL AND OUT_TIME IS NULL";
}
