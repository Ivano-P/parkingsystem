package com.parkit.parkingsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
	if ((ticket.getInTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
	    throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
	}

	Date arrivalDate = ticket.getInTime();
	Date exitDate = ticket.getOutTime();

	LocalDateTime inTime = arrivalDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	LocalDateTime outTime = exitDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

	Duration timeParked = Duration.between(inTime, outTime);
	long differenceInMinutes = timeParked.toMinutes();
	double duration = (double) differenceInMinutes / 60;
	
	//added free 30 min parking by setting parking  duration to 0 if duration under 30min.
	if (duration < 0.50) duration = 0.0;

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