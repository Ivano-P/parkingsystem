package com.parkit.parkingsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean isRecurringCustomer) {
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
	    if (isRecurringCustomer) {
		ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR*Fare.FIVE_PERCENT_DISCOUNT_MULTIPLIER);//adds 5% discount is vehicle had already used parking
		break;
	    }else {
		ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
		break;
	    }	    
	}
	case BIKE: {
	    if (isRecurringCustomer) {
		ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * Fare.FIVE_PERCENT_DISCOUNT_MULTIPLIER);//adds 5% discount is vehicle had already used parking
		break;
	    }else {
		ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
		break;
	    }	    
	}
	default:
	    throw new IllegalArgumentException("Unkown Parking Type");
	}
    }
}