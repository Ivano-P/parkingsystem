package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    private boolean isRecurringCustomer;
    private Date inTime;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
        inTime = new Date();
        
    }

    @Test
    @DisplayName("test that care fare is calculated correctly")
    public void calculateFareCarTest(){
	//GIVEN
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);        
        //WHEN
        fareCalculatorService.calculateFare(ticket, isRecurringCustomer);       
        //THEN
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    @DisplayName("test that bike fare is calcularted correctly")
    public void calculateFareBikeTest(){
	//GIVEN
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);      
        //WHEN
        fareCalculatorService.calculateFare(ticket, isRecurringCustomer);
        //THEN
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    @DisplayName("Test that exception is triggerd if vehicle type is not car or bike")
    public void calculateFareUnkownTypeTest(){
	//GIVEN
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        
        //WHEN
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
   
        //THEN
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket, isRecurringCustomer));
    }

    @Test
    @DisplayName("test that exception is triggered if in time is after outtime (vehicle that hasn't exited is entering parking)")
    public void calculateFareBikeWithFutureInTimeTest(){
	//GIVEN
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        
        //WHEN
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        //THEN
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket, isRecurringCustomer));
    }

    @Test
    @DisplayName("test bike fare with less than one hour parking time")
    public void calculateFareBikeWithLessThanOneHourParkingTimeTest(){
	//GIVEN
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        //WHEN
        fareCalculatorService.calculateFare(ticket, isRecurringCustomer);
        
        //THEN
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
    }

    @Test
    @DisplayName("test car fare with less than one hour parking time")
    public void calculateFareCarWithLessThanOneHourParkingTimeTest(){
	//GIVEN
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        //WHEN 
        fareCalculatorService.calculateFare(ticket, isRecurringCustomer);
        //THEN
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    @DisplayName("test that care is calculated correcly when custumer if parked for more than 24h")
    public void calculateFareCarWithMoreThanADayParkingTimeTest(){
	//GIVEN
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        //WHEN
        fareCalculatorService.calculateFare(ticket, isRecurringCustomer);        
        //THEN
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }
    
    @Test
    @DisplayName("test that fare is free if vehicle exited within 30 min")
    public void checkThatFareIsFreeWithLessThanThirtyMinParkingTimeTest() {
	//GIVEN
        inTime.setTime( System.currentTimeMillis() - (20 * 60 * 1000) );//20 mins parking time should be free.
        Date outTime = new Date(); 
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
      
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        //WHEN
        fareCalculatorService.calculateFare(ticket, isRecurringCustomer);
        //THEN
	assertEquals (0.0, ticket.getPrice());
	
    }

    @Test
    @DisplayName("test that five percent discount is applied if vehicle has previous ticket with in-time and out-time ")
    public void checkThatFivePerCentDiscountIsAppliedIfIsRecurringCustumerTest() {
	//GIVEN
        inTime.setTime( System.currentTimeMillis() - (60 * 60 * 1000) );//60 min parking at bike rate should be 1.
        Date outTime = new Date(); 
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        isRecurringCustomer = true;
        //WHEN
        fareCalculatorService.calculateFare(ticket, isRecurringCustomer);
        //THEN
	assertEquals(0.95, ticket.getPrice());
    }
}
