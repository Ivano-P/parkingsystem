package com.parkit.parkingsystem.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
//import org.assertj.core.api.Fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @BeforeAll
    public static void setUp() throws Exception {
	dataBasePrepareService = new DataBasePrepareService();
	ticketDAO = new TicketDAO();
	ticketDAO.dataBaseConfig = dataBaseTestConfig;
    }

    @BeforeEach
    private void setUpPerTest() {
	dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
	dataBasePrepareService.clearDataBaseEntries();
    }

    // checks that saveTicket() adds ID,parking spot number, in time to db, it also
    // sets outime to null and gets price.
    @Test
    @DisplayName("Test that ticketDAO.saveTicket populates db correclty with ticket info")
    public void testThatTicketIsSavedToDateBase() throws Exception {
	// GIVEN
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
	Ticket ticket = new Ticket();
	ticket.setId(2);
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("ABC1");
	ticket.setPrice(10.0);
	ticket.setInTime(new Date());

	// WHEN
	ticketDAO.saveTicket(ticket);
	Ticket ticketFromDB = new Ticket();
	ticketFromDB = ticketDAO.getTicket("ABC1");

	int id = ticketFromDB.getId();
	String vehicleRegNumber = ticketFromDB.getVehicleRegNumber();
	double price = ticketFromDB.getPrice();
	Date inTime = ticketFromDB.getInTime();
	Date outTime = ticketFromDB.getOutTime();
	// THEN
	assertThat(id).isEqualTo(2);
	assertThat(vehicleRegNumber).isEqualTo("ABC1");
	assertThat(price).isEqualTo(10.0);
	assertThat(inTime).isNotNull();
	assertThat(outTime).isNull();
    }

    @Test
    @DisplayName("test that ticketDAO.getTicket is passing ticket db info to ticket object")
    public void testThatTicketDataBaseValuesArePassedToTicketObject() throws Exception {
	// GIVEN
	// set up test data
	ParkingSpot parkingSpot = new ParkingSpot(2, ParkingType.CAR, true);
	String vehicleRegNumber = "ABC2";
	Date inTime = new Date();
	inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
	Date outTime = new Date();
	// create expected ticket object
	Ticket expectedTicket = new Ticket();
	expectedTicket.setParkingSpot(parkingSpot);
	expectedTicket.setVehicleRegNumber(vehicleRegNumber);
	expectedTicket.setPrice(20.0);
	expectedTicket.setInTime(inTime);
	expectedTicket.setOutTime(outTime);

	ticketDAO.saveTicket(expectedTicket);
	ticketDAO.updateTicket(expectedTicket);
	// WHEN
	// retrieve ticket from database
	Ticket ticketFromDb = ticketDAO.getTicket(vehicleRegNumber);
	// THEN
	// check that ticket object matches expected values
	assertThat(ticketFromDb.getParkingSpot()).isEqualTo(expectedTicket.getParkingSpot());
	assertThat(ticketFromDb.getVehicleRegNumber()).isEqualTo(expectedTicket.getVehicleRegNumber());
	assertThat(ticketFromDb.getPrice()).isEqualTo(expectedTicket.getPrice());

	// comparing values with tolerance of 999 milliseconds because
	// java.sql.Timestanp precision is in nanoseconds where as java.util.Date is in
	// milliseconds. (java.sql.Timestanp round up to second)
	long tolerance = 999L; // in milliseconds
	assertThat(Math.abs(ticketFromDb.getInTime().getTime() - expectedTicket.getInTime().getTime()))
		.isLessThanOrEqualTo(tolerance);
	assertThat(Math.abs(ticketFromDb.getOutTime().getTime() - expectedTicket.getOutTime().getTime()))
		.isLessThanOrEqualTo(tolerance);
    }

    @Test
    @DisplayName("test that ticketDAO.update is population db with price and out time")
    public void testkThatPriceAndOuttimeAreBeingPopulatedInDb() {
	// GIVEN
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
	Ticket ticket = new Ticket();
	ticket.setParkingSpot(parkingSpot);
	ticket.setId(1);
	ticket.setVehicleRegNumber("ABC2");
	ticket.setInTime(new Date());

	ticketDAO.saveTicket(ticket);

	// WHEN
	ticket.setPrice(15.0);
	ticket.setOutTime(new Date());
	ticketDAO.updateTicket(ticket);

	Ticket ticketFromDb = new Ticket();
	ticketFromDb = ticketDAO.getTicket("ABC2");

	double price = ticketFromDb.getPrice();
	Date outTime = ticketFromDb.getOutTime();

	// THEN
	assertThat(price).isEqualTo(15.0);
	assertThat(outTime).isNotNull();

    }

    @Test
    @DisplayName("test that custumer is considered recurring when vehicle has beed in and out of parking")
    public void testThatCustumerHasAlreadyBeenInAndOutOfParking() {
	// GIVEN
	String vehicleRegNumber = "ABC5";
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
	Ticket ticket = new Ticket();
	ticket.setParkingSpot(parkingSpot);
	ticket.setId(1);
	ticket.setPrice(1);
	ticket.setVehicleRegNumber(vehicleRegNumber);
	ticket.setInTime(new Date());
	ticket.setOutTime(new Date());

	ticketDAO.saveTicket(ticket);
	ticketDAO.updateTicket(ticket);

	// WHEN
	boolean isReccuringCustumer = ticketDAO.checkRecurringCustomer(vehicleRegNumber);

	// THEN
	assertThat(isReccuringCustumer).isTrue();
    }

    @Test
    @DisplayName("test that custumer is not considered recurring when there is no ticket with in and out time")
    public void testThatCustumerHasNotBeenInAndOutOfParking() {
	// GIVEN
	String vehicleRegNumber = "ABC5";
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
	Ticket ticket = new Ticket();
	ticket.setParkingSpot(parkingSpot);
	ticket.setId(1);
	ticket.setVehicleRegNumber(vehicleRegNumber);
	ticket.setInTime(new Date());

	ticketDAO.saveTicket(ticket);
	// WHEN
	boolean isReccuringCustumer = ticketDAO.checkRecurringCustomer(vehicleRegNumber);
	// THEN
	assertThat(isReccuringCustumer).isFalse();
    }

    @Test
    public void testIfVehicleIsInParkingTest() {
	// GIVEN
	String vehicleRegNumber = "ABC6";
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
	Ticket ticket = new Ticket();
	ticket.setParkingSpot(parkingSpot);
	ticket.setId(1);
	ticket.setVehicleRegNumber(vehicleRegNumber);
	ticket.setInTime(new Date());

	ticketDAO.saveTicket(ticket);

	// WHEN
	boolean vehicleIsInParking = ticketDAO.checkVehileIsInParking(vehicleRegNumber);

	// THEN
	assertThat(vehicleIsInParking).isTrue();
    }

    @Test
    @DisplayName("test if vehicle is considered not in parking one out time is registered")
    public void testCaseWhenVehickeIsNotInParking() {
	// GIVEN
	String vehicleRegNumber = "ABC7";
	parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
	Ticket ticket = new Ticket();
	ticket.setParkingSpot(parkingSpot);
	ticket.setId(1);
	ticket.setPrice(1);
	ticket.setVehicleRegNumber(vehicleRegNumber);
	ticket.setInTime(new Date());
	ticket.setOutTime(new Date());

	ticketDAO.saveTicket(ticket);
	ticketDAO.updateTicket(ticket);

	// WHEN
	boolean vehicleIsInParking = ticketDAO.checkVehileIsInParking(vehicleRegNumber);

	// THEN
	assertThat(vehicleIsInParking).isFalse();
    }

}
