package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    // changed set up before each test since the different functions being called cant
    	// use the inital set up.
    @BeforeEach
    private void setUpPerTest() {
	try {
	    parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException("Failed to set up test mock objects");
	}
    }

    @Test
    @DisplayName("test that ParkingSpotDAO.saveTicket is being called when processing incomming vehicle")
    public void processIncomingVehicleTest() throws Exception {
	// GIVEN
	try {
	    when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	    when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	    
	    when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
	    when(inputReaderUtil.readSelection()).thenReturn(1);
	    when(ticketDAO.checkVehileIsInParking(anyString())).thenReturn(false);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException("Failed to set up test mock objects");
	}

	// WHEN
	parkingService.processIncomingVehicle();

	// THEN
	verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    }
    
    @Test
    @DisplayName("test that next available parking spot is being returned")
    public void getNextParkingNumberIfAvailableTest() {
	// GIVEN
	ParkingType parkingType = ParkingType.BIKE;
	try {	  
	    //vehicle type 1 is car in readSelection()
	    when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
	    when(inputReaderUtil.readSelection()).thenReturn(2);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException("Failed to set up test mock objects");
	}
	
	// WHEN
	ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

	// THEN
	assertThat(result).isNotNull();
	assertThat(result.getParkingType()).isEqualTo(parkingType);
	assertThat(result.isAvailable()).isTrue();
	verify(parkingSpotDAO, times(1)).getNextAvailableSlot(parkingType);
    }

    // moved part of setUpPerTest() instructions direcly in this test since some
    // part of the setup only apply to this test and may disrupt other test
    @Test
    @DisplayName("test that ParkingSpotDAO.updateParking is being called when processing exiting vehicle")
    public void processExitingVehicleTest() {
	// GIVEN
	try {
	    when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	    when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
	    
	    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	    Ticket ticket = new Ticket();
	    ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
	    ticket.setParkingSpot(parkingSpot);
	    ticket.setVehicleRegNumber("ABCDEF");
	    when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	    when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException("Failed to set up test mock objects");
	}
	// WHEN
	parkingService.processExitingVehicle();

	// THEN
	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }
   
}
