package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FareCalculatorService {

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration = (outHour - inHour) / (60 * 60 * 1000);
        double durationInMinute = (outHour - inHour) / (60 * 1000);

        if (durationInMinute<30) {
            ticket.setPrice(0);
            //System.out.println("Gratuit en dessous des 30 premières minutes.");
        } else {
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

        double percentOff = 5;
        double actualPrice = ticket.getPrice();
        //System.out.println(ticket.getVehicleRegNumber());
        if (isAlreadyCame(ticket.getVehicleRegNumber())){
            System.out.println("Prix avant réduction : " + actualPrice);
            System.out.println("Une réduction de : " + (actualPrice*(percentOff/100)) + " est appliqué.");
            ticket.setPrice((actualPrice-(actualPrice*(percentOff/100))));
        }
    }

    private boolean isAlreadyCame(String registration) {
        Boolean isGood = false;
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_REG);
            ResultSet rs = ps.executeQuery();
            while ((rs.next()) && !(isGood)) {
                //System.out.println("Valeur de getString = " + rs.getString( "VEHICLE_REG_NUMBER"));
                //System.out.println("La valeur de isGood = " + isGood);
                 if ((registration.equals(rs.getString("VEHICLE_REG_NUMBER"))) && ((rs.getDate("OUT_TIME") != null ))){
                     //System.out.println(rs.getString("VEHICLE_REG_NUMBER") + " --- " + rs.getDate("OUT_TIME"));
                     //System.out.println("5% ok");
                     isGood = true;
                }
            }
            //System.out.println("boucle fini");
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataBaseConfig.closeConnection(con);
        }

        return isGood;
    }
}