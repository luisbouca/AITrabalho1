package Models;

import jade.core.AID;
import java.util.Date;
import java.util.Random;

public class Flight {
    private String identification;
    private AID airplane;
    private int passagers;
    private int[] destination;
    private int[] departure;
    private int duration;
    private Date date;
    private int speed;
    private AID airport;
    private int fuel;
    private int state;
    private float distance;
    //private List<> interations; missing interactions Class

    public Flight(String idAirport, AID airplane, int passagers, int[] destination, int[] departure, int speed, AID airport, int fuel) {
        this.identification = idAirport+"Airport:"+ new Random().nextInt(100000);
        this.airplane = airplane;
        this.passagers = passagers;
        this.destination = destination;
        this.departure = departure;
        this.speed = speed;
        this.airport = airport;
        this.fuel = fuel;
        state = 0;
        date = new Date();
        distance = (int) Math.sqrt(((Math.pow((destination[0] - departure[0]), 2)) + (Math.pow((destination[1] - departure[1]), 2))));
        duration = (int) (distance/speed);
    }

    public String getMsg() {
        return identification + "," + airplane + "," + passagers + "," + destination[0] + ","  + destination[1] + "," + departure + "," + duration + "," + date + "," + speed + "," + airport + "," + fuel + "," + state + "," + distance ;
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

    public AID getAirport() {
        return airport;
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
