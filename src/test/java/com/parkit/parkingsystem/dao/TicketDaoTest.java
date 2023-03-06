package com.parkit.parkingsystem.dao;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Assertions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

//import org.assertj.core.api.Fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

class TicketDaoTest {
    
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private ParkingSpot parkingSpot;
    private Ticket ticket;
    
    
    @BeforeAll
    public static void setUp() throws Exception {    
	dataBasePrepareService = new DataBasePrepareService();
	ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;               
    }

    @BeforeEach
    private void setUpPerTest() {	
        ticket = new Ticket();
        dataBasePrepareService.clearDataBaseEntries();
    }

    //checks that saveTicket() adds ID,parking spot number, in time to db, it also sets outime to null and gets price. 
    @Test
    public void checkThatTicketIsSavedToDateBase() throws Exception{
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
	ticket.setId(2);
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABC1");
	ticket.setPrice(10.0);
	ticket.setInTime(new Date());
	ticket.setOutTime(null);
	ticketDAO.saveTicket(ticket);
	Connection con =null;
	try {
	    con = dataBaseTestConfig.getConnection();
	    PreparedStatement ps = con.prepareStatement("SELECT * FROM ticket WHERE ID = 2 ; ");	    
	    ResultSet rs = ps.executeQuery();
	    boolean ticketSaved = rs.next();
	    if (ticketSaved) {
		int id = rs.getInt("id");
		String vehicleRegNumber = rs.getString("VEHICLE_REG_NUMBER");
		double price = rs.getDouble("PRICE");
		Date inTime =  rs.getDate("IN_TIME");
		Date outTime =  rs.getDate("OUT_TIME");
		
		assertThat(id).isEqualTo(2);
		assertThat(vehicleRegNumber).isEqualTo("ABC1");
		assertThat(price).isEqualTo(10.0);
		assertThat(inTime).isNotNull();
		assertThat(outTime).isNull();	
	    }	
	    dataBaseTestConfig.closeResultSet(rs);
	    dataBaseTestConfig.closePreparedStatement(ps);
	} catch(Exception e) {
	    System.out.println("Error saving ticket to DB" + e );	    
	} finally {
	    dataBaseTestConfig.closeConnection(con);
	}
	
    }
    
    @Test
    public void checkThatTicketDataBaseValuesArePassedToTicketObject() throws Exception {
        // set up test data
        ParkingSpot parkingSpot = new ParkingSpot(2, ParkingType.CAR, true);
        String vehicleRegNumber = "ABC2";
        Date inTime = new Date();
        //inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();

        // create expected ticket object
        Ticket expectedTicket = new Ticket();
        expectedTicket.setParkingSpot(parkingSpot);
        expectedTicket.setVehicleRegNumber(vehicleRegNumber);
        expectedTicket.setPrice(20.0);
        expectedTicket.setInTime(inTime);
        expectedTicket.setOutTime(outTime);

        Connection con = null;
        try {
            // save ticket in test db
            con = dataBaseTestConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)");
            ps.setInt(1, parkingSpot.getId());
            ps.setString(2, vehicleRegNumber);
            ps.setDouble(3, 20.0);
            ps.setTimestamp(4, new Timestamp(inTime.getTime()));
            ps.setTimestamp(5, new Timestamp(outTime.getTime()));
            ps.executeUpdate();

            // retrieve ticket from database
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

            // check that ticket object matches expected values
            assertThat(ticket.getParkingSpot()).isEqualTo(expectedTicket.getParkingSpot());
            assertThat(ticket.getVehicleRegNumber()).isEqualTo(expectedTicket.getVehicleRegNumber());
            assertThat(ticket.getPrice()).isEqualTo(expectedTicket.getPrice());
          
            //comparing values with tolerance of 999 milliseconds because java.sql.Timestanp precision is in nanoseconds where as java.util.Date 
            	//is in milliseconds. since the value from data base is being rounded there should only be off by a max of 999 milliseconds
            long tolerance = 999L; // in milliseconds
            assertThat(Math.abs(ticket.getInTime().getTime() - expectedTicket.getInTime().getTime())).isLessThanOrEqualTo(tolerance);
            assertThat(Math.abs(ticket.getOutTime().getTime() - expectedTicket.getOutTime().getTime())).isLessThanOrEqualTo(tolerance);

            dataBaseTestConfig.closePreparedStatement(ps);
        } catch (Exception e) {
            System.out.println("Error saving or retrieving ticket from DB: " + e);
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
    }
    
    // TODO: recheck with mentor, test passing but rs.next() is false.
    @Test
    public void checkThatPriceAndOuttimeAreBeingPopulatedInDb() {	
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
	ticket.setParkingSpot(parkingSpot);
	ticket.setId(1);
	ticket.setPrice(15.0);
	ticket.setInTime(new Date());	
	ticket.setOutTime(new Date());
	
	
	
	Connection con = null;
	try {	    
	    con = dataBaseTestConfig.getConnection();	
	    
	    //setting id, parking number, vehicle reg number and in time in bd for a ticket in db
	    PreparedStatement ps1 = con.prepareStatement("INSERT INTO ticket (PARKING_NUMBER, ID, VEHICLE_REG_NUMBER ,IN_TIME) VALUES (1, 1, 'FALSE1','2023-03-01 10:00:00');");
	    ps1.executeUpdate();
	    
	    ticketDAO.updateTicket(ticket);
	    
	    //checking that updateTicket has populated price and out time in db.
	    PreparedStatement ps2 = con.prepareStatement("SELECT PRICE, OUT_TIME FROM ticket WHERE ID = 1 ;");	    
	    ResultSet rs = ps2.executeQuery();
	    boolean ticketHasPiceAndOutTime = rs.next();
	    if (ticketHasPiceAndOutTime) {	
		double price = rs.getDouble("PRICE");
		Date outTime = rs.getDate("OUT_TIME");
		
		assertThat(price).isEqualTo(15.0);
		assertThat(outTime).isNotNull();	
	    }	
	    dataBaseTestConfig.closeResultSet(rs);
	    dataBaseTestConfig.closePreparedStatement(ps1);
	    dataBaseTestConfig.closePreparedStatement(ps2);
	} catch(Exception e) {
	    System.out.println("Error saving ticket to DB" + e );	    
	} finally {
	    dataBaseTestConfig.closeConnection(con);
	}
    }
    
    @Test
    public void chackRecurringCustomerTest() {
	String vehicleRegNumber1 = "ABC5";
	String vehicleRegNumber2 = "ABC6";
	
	Connection con = null;
	try {
	    con = dataBaseTestConfig.getConnection();
	    PreparedStatement ps1 = con.prepareStatement("INSERT INTO ticket (PARKING_NUMBER, ID, VEHICLE_REG_NUMBER ,IN_TIME, OUT_TIME) VALUES (1, 1, 'ABC5','2023-03-01 10:00:00', '2023-03-01 10:32:00');");
	    ps1.executeUpdate();
	    
	    PreparedStatement ps2 = con.prepareStatement("INSERT INTO ticket (PARKING_NUMBER, ID, VEHICLE_REG_NUMBER ,IN_TIME) VALUES (2, 2, 'ABC6','2023-03-01 10:00:00');");
	    ps2.executeUpdate();
	    
	    dataBaseTestConfig.closePreparedStatement(ps1);
	}catch(Exception e) {
	    System.out.println("Error updating ticket in db" + e );	    
	} finally {	    
	    dataBaseTestConfig.closeConnection(con);
	}		
	boolean isReccuringCustumer = ticketDAO.checkRecurringCustomer(vehicleRegNumber1);	
	assertThat(isReccuringCustumer).isTrue();
	
	boolean isReccuringCustumer2 = ticketDAO.checkRecurringCustomer(vehicleRegNumber2);	
	assertThat(isReccuringCustumer2).isFalse();
    }
    
    @Test
    @Disabled
    public void checkVehicleIsInParkingTest() {
	Assertions.fail("not yet implimented");
    }

}
