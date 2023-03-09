package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public boolean saveTicket(Ticket ticket) {
	try (Connection con = dataBaseConfig.getConnection();
		PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET)) {
	    // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
	    ps.setInt(1, ticket.getId());
	    ps.setInt(2, ticket.getParkingSpot().getId());
	    ps.setString(3, ticket.getVehicleRegNumber());
	    ps.setDouble(4, ticket.getPrice());
	    ps.setTimestamp(5, new Timestamp(ticket.getInTime().getTime()));
	    ps.setTimestamp(6, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
	    return ps.execute();
	} catch (Exception ex) {
	    logger.error("Error fetching next available slot", ex);
	}
	return false;
    }

    public Ticket getTicket(String vehicleRegNumber) {
	Ticket ticket = null;
	ResultSet rs = null;
	try (Connection con = dataBaseConfig.getConnection();
		PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET)) {
	    // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
	    ps.setString(1, vehicleRegNumber);
	    rs = ps.executeQuery();
	    if (rs.next()) {
		ticket = new Ticket();
		ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
		ticket.setParkingSpot(parkingSpot);
		ticket.setId(rs.getInt(2));
		ticket.setVehicleRegNumber(vehicleRegNumber);
		ticket.setPrice(rs.getDouble(3));
		ticket.setInTime(rs.getTimestamp(4));
		ticket.setOutTime(rs.getTimestamp(5));
	    }
	    dataBaseConfig.closeResultSet(rs);
	} catch (Exception ex) {
	    logger.error("Error fetching next available slot", ex);
	} finally {
	    try {
		if (rs != null) {
		    rs.close();
		}
	    } catch (Exception ex) {
		logger.error("Error closing result set", ex);
	    }
	}
	return ticket;
    }

    public boolean updateTicket(Ticket ticket) {
	try (Connection con = dataBaseConfig.getConnection();
		PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET)) {
	    ps.setDouble(1, ticket.getPrice());
	    ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
	    ps.setInt(3, ticket.getId());
	    ps.execute();
	    return true;
	} catch (Exception ex) {
	    logger.error("Error saving ticket info", ex);
	}
	return false;
    }

    // check the DB for a ticket with the same registration number and an in and out
    // time registered. -> custommer has already used the parking
    public boolean checkRecurringCustomer(String vehicleRegNumber) {
	try (Connection con = dataBaseConfig.getConnection();
		PreparedStatement ps = con.prepareStatement(DBConstants.CHECK_IF_RECURRING_CUSTOMER)) {
	    ps.setString(1, vehicleRegNumber);
	    ResultSet rs = ps.executeQuery();
	    boolean hasTicketWithInAndOutTime = rs.next();
	    if (hasTicketWithInAndOutTime) {
		return true;
	    } else {
		return false;
	    }
	} catch (Exception ex) {
	    logger.error("Error checking if previous ticket with entry and exit date exist", ex);
	}
	return false;
    }

    // check the DB for a ticket with the same registration number and has an
    // in-time but no out-time
    public boolean checkVehileIsInParking(String vehicleRegNumber) {
	ResultSet rs = null;
	try (Connection con = dataBaseConfig.getConnection();
		PreparedStatement ps = con.prepareStatement(DBConstants.CHECK_IF_VEHICLE_ALREADY_IN_PARKING)) {
	    ps.setString(1, vehicleRegNumber);
	    rs = ps.executeQuery();
	    if (rs.next()) {
		return true;
	    } else {
		return false;
	    }
	} catch (Exception ex) {
	    logger.error("Error checking if vehicle already in parking", ex);
	} finally {
	    try {
		if (rs != null) {
		    rs.close();
		}
	    } catch (Exception ex) {
		logger.error("Error closing result set", ex);
	    }
	}
	return false;
    }

}
