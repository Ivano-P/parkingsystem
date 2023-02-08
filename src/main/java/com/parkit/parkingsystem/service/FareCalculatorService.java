package com.parkit.parkingsystem.service;

import java.util.Date;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
	if ((ticket.getInTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
	    throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
	}

	Date inHour = ticket.getInTime();
	Date outHour = ticket.getOutTime();

	// TODO: Some tests are failing here. Need to check if this logic is correct
	double duration = outHour.getTime() - inHour.getTime();
	duration /= (60 * 60 * 1000); // converting duration from miliseconds to hours.

	switch (ticket.getParkingSpot().getParkingType()) {
	case CAR: {
	    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
	    break;
	}
	case BIKE: {
	    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
	    break;
	}
	default:
	    throw new IllegalArgumentException("Unkown Parking Type");
	}
    }
}