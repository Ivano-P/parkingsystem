package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
	    if (rs.next()) {
		System.out.println("ticket was correctly saved in DB");
	    } else {
		System.out.println("error adding ticket to DB");
	    }
	    // assert that in time is not null and ticket regnumber = input number
	    // assertTrue(rs.next());

	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Failed to check that ticket had been saved in DB");
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

	    // check parking to confirme that parkingspot is now unavailable
	    PreparedStatement ps3 = con.prepareStatement("SELECT AVAILABLE FROM parking WHERE PARKING_NUMBER = ? ;");
	    ps3.setInt(1, parkingNumber);
	    ResultSet rs3 = ps3.executeQuery();
	    if (rs3.next()) {
		// assertFalse(rs3.getBoolean(1));
		parkingSpotAvailable = rs3.getBoolean("AVAILABLE");
		assertFalse(parkingSpotAvailable);
		// TODO: creer une liste avec l'heure d'entré et bool avalable slot et tester contre une liste ou on passe les valeures réels attendu.
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Failed to check if parking table as updated");
	} finally {
	    dataBaseTestConfig.closeConnection(con);
	}
    }

    @Test
    public void testParkingLotExit() throws Exception {
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	testParkingACar();
	parkingService.processExitingVehicle();
	// TODO: check that the fare generated and out time are populated correctly in the database
	Connection con = null;
	try {
	    con = dataBaseTestConfig.getConnection();
	    PreparedStatement ps = con
		    .prepareStatement("SELECT OUT_TIME, PRICE FROM ticket WHERE VEHICLE_REG_NUMBER = ? ;");
	    ps.setString(1, inputReaderUtil.readVehicleRegistrationNumber());
	    ResultSet rs = ps.executeQuery();
	    assertTrue(rs.next());
	    // TODO:assert out_time is not null and price is => 0.
	    System.out.println(rs.getDate("OUT_TIME"));
	    System.out.println(rs.getDouble("PRICE"));
	    
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Failed to check if outtime and price populated in database");
	} finally {
	    dataBaseTestConfig.closeConnection(con);
	}
    }

}
