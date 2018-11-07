/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author user
 */
import jade.core.AID;
import java.util.Date;

public class Flight {
    private String identification;
    private AID airplane;
    private int passagers;
    private int[] destination;
    private int[] departure;
    private int duration;
    private Date date;
    private int speed;
    private String location;
    private int fuel;
    private int state;
    private float distance;
    //private List<> interations; missing interactions Class

    public Flight(String idAirport, AID airplane, int passagers, int[] destination, int[] departure, int speed, String location, int fuel) {
        this.identification = idAirport+"RandomString";
        this.airplane = airplane;
        this.passagers = passagers;
        this.destination = destination;
        this.departure = departure;
        this.speed = speed;
        this.location = location;
        this.fuel = fuel;
        state = 0;
        date = new Date();
        distance = (int) Math.sqrt(((Math.pow((destination[0] - departure[0]), 2)) + (Math.pow((destination[1] - departure[1]), 2))));
        duration = (int) (distance/speed);
    }

    public String getIdentification() {
        return identification;
    }

    public AID getAirplane() {
        return airplane;
    }

    public int getPassagers() {
        return passagers;
    }

    public int[] getDestination() {
        return destination;
    }

    public int[] getDeparture() {
        return departure;
    }

    public int getDuration() {
        return duration;
    }

    public Date getDate() {
        return date;
    }

    public int getSpeed() {
        return speed;
    }

    public String getLocation() {
        return location;
    }

    public int getFuel() {
        return fuel;
    }

    public int getState() {
        return state;
    }

    public float getDistance() {
        return distance;
    }
    
    
}
