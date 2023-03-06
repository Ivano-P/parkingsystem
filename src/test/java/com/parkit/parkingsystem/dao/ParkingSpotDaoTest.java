package com.parkit.parkingsystem.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;

class ParkingSpotDaoTest {
	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static DataBasePrepareService dataBasePrepareService;
	private static ParkingSpotDAO parkingSpotDAO;
	private ParkingSpot parkingSpot;

    @BeforeAll
    static void setUp() throws Exception {
	dataBasePrepareService = new DataBasePrepareService();
	parkingSpotDAO = new ParkingSpotDAO();
	parkingSpotDAO.dataBaseConfig = dataBaseTestConfig; 	
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
	dataBasePrepareService.clearDataBaseEntries();
    }

    @BeforeEach
    void setUpPerTest() throws Exception {
	dataBasePrepareService.clearDataBaseEntries();	
    }

    @Test
    void testThatFirstAvailableCarSlotIsOne() {
	//GIVEN
	ParkingType car = ParkingType.CAR;
	
	//WHEN
	parkingSpotDAO.getNextAvailableSlot(car);
	
	//THEN
	assertThat(parkingSpotDAO.getNextAvailableSlot(car)).isEqualTo(1);
    }
    
    @Test
    void testThatFirstAvailableBikeSlotIsFour() {
	//GIVEN
	ParkingType bike = ParkingType.BIKE;
	
	//WHEN
	parkingSpotDAO.getNextAvailableSlot(bike);
	
	//THEN
	assertThat(parkingSpotDAO.getNextAvailableSlot(bike)).isEqualTo(4);
    }
    
    @Test
    @DisplayName("Test that correct parking spot available is returned when there are two occupied car spots ")
    void testThatNextAvailableCarSlotIsThreeWhenTwoCarSpotsAreTaken() {
	//GIVEN
	ParkingType car = ParkingType.CAR;
	
	Connection con = null;
	try {
	    con = dataBaseTestConfig.getConnection();
	    PreparedStatement ps = con.prepareStatement("UPDATE parking SET type = 'CAR', available = false ORDER BY parking_number LIMIT 1");	  
	    //ps.setInt(1, 1);
	    //ps.setString(2, car.toString());
	    //ps.setBoolean(3, false);
	    ps.executeUpdate();
	    dataBaseTestConfig.closePreparedStatement(ps);
	    } catch (Exception e) {
	        fail("Unable to insert parking spots: " + e.getMessage());
	    } finally {
		dataBaseTestConfig.closeConnection(con);
	    }
	
	//WHEN
	int nextAvailableCarSlot = parkingSpotDAO.getNextAvailableSlot(car);
	
	//THEN
	assertThat(nextAvailableCarSlot).isEqualTo(2);
    }
    

    @Test
    @DisplayName("test that db is being populated correctly with ParkingSpot object info")
    void testThatParkingSpotIsBeingPopulationCorrectlyInDb() {
	//GIVEN
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	
	//WHEN
	parkingSpotDAO.updateParking(parkingSpot);
	
	Connection con = null;
	try {
	    con = dataBaseTestConfig.getConnection();
	    PreparedStatement ps = con.prepareStatement("SELECT * FROM parking WHERE PARKING_NUMBER = 1 ; ");	    
	    ResultSet rs = ps.executeQuery();
	    if (rs.next()) {
		//int parkingNumber = rs.getInt("PARING_NUMBER");
		boolean isSpotAvailable = rs.getBoolean("AVAILABLE");
		String vehicleType = rs.getString("TYPE");
		
		//THEN
		assertThat(isSpotAvailable).isFalse();
		assertThat(vehicleType).isEqualTo("CAR");
		System.out.println(vehicleType);
	    }
	}catch(Exception e) {
	    fail("failed to fetch parking data from db " + e.getMessage());
	}
	finally {
	    dataBaseTestConfig.closeConnection(con);
	}
	
    }
}
