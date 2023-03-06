package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
	parkingSpotDAO = new ParkingSpotDAO();
	parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
	ticketDAO = new TicketDAO();
	ticketDAO.dataBaseConfig = dataBaseTestConfig;
	dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {

    }

    @Test
    public void testParkingACar() throws Exception {
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	parkingService.processIncomingVehicle();
	int parkingNumber = -1;
	boolean parkingSpotAvailable;	
	Connection con = null;
	try {
	    // check that ticket had been saved in DB
	    con = dataBaseTestConfig.getConnection();
	    PreparedStatement ps = con.prepareStatement("SELECT * FROM ticket WHERE VEHICLE_REG_NUMBER = ? ;");
	    ps.setString(1, inputReaderUtil.readVehicleRegistrationNumber());
	    ResultSet rs = ps.executeQuery();
	    boolean hasTicket = rs.next();
	    if (hasTicket) {
		    assertThat(rs.getTimestamp("IN_TIME")).isNotNull();
		    assertThat(rs.getString("VEHICLE_REG_NUMBER")).isEqualTo(inputReaderUtil.readVehicleRegistrationNumber());
		    System.out.println("ticket was correctly saved in DB \n");		   
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Failed to check that ticket had been saved in DB \n");
	} finally {
	    dataBaseTestConfig.closeConnection(con);
	}

	// check that Parking table is updated with availability
	try {
	    // first retreive parking number linked to vehicle reg number.
	    con = dataBaseTestConfig.getConnection();
	    PreparedStatement ps2 = con.prepareStatement("SELECT PARKING_NUMBER FROM ticket WHERE VEHICLE_REG_NUMBER = ? ;");
	    ps2.setString(1, inputReaderUtil.readVehicleRegistrationNumber());
	    ResultSet rs2 = ps2.executeQuery();
	    if (rs2.next()) {
		parkingNumber = rs2.getInt("PARKING_NUMBER");
	    }

	    // then check parking to confirme that parkingspot is now unavailable
	    PreparedStatement ps3 = con.prepareStatement("SELECT AVAILABLE FROM parking WHERE PARKING_NUMBER = ? ;");
	    ps3.setInt(1, parkingNumber);
	    ResultSet rs3 = ps3.executeQuery();
	    boolean hasParking = rs3.next();
	    if (hasParking) {		
		parkingSpotAvailable = rs3.getBoolean("AVAILABLE");
		assertThat(parkingSpotAvailable).isFalse();		
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Failed to check if parking table as updated");
	} finally {
	    dataBaseTestConfig.closeConnection(con);
	}
    }

    // check that the fare generated and out time are populated correctly in the database
    @Test
    public void testParkingLotExit() throws Exception {
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	testParkingACar();
	Thread.sleep(1000);
	parkingService.processExitingVehicle();	
	Connection con = null;
	try {
	    con = dataBaseTestConfig.getConnection();
	    PreparedStatement ps = con
		    .prepareStatement("SELECT OUT_TIME, PRICE FROM ticket WHERE VEHICLE_REG_NUMBER = ? ;");	
	    ps.setString(1, inputReaderUtil.readVehicleRegistrationNumber());
	    ResultSet rs = ps.executeQuery();
	    boolean hasTicketWithOutTimeAndPrice;
	    hasTicketWithOutTimeAndPrice = rs.next();	    
	    if (hasTicketWithOutTimeAndPrice){
		assertThat(rs.getDate("OUT_TIME")).isNotNull();
		assertThat(rs.getDouble("PRICE")).isGreaterThanOrEqualTo(0);
	    }
	    System.out.println("fare generated and out time populated correctly");	    
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Failed to check if outtime and price populated in database");
	} finally {
	    dataBaseTestConfig.closeConnection(con);
	}
    }

}
