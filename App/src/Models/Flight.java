package Models;

import jade.core.AID;
import java.io.Serializable;
import java.util.Date;
import java.util.Random;

public class Flight implements Serializable{
    private String identification;
    private AID airplane;
    private int passagers;
    private int[] destination;
    private int[] tmpdestination;
    private int[] departure;
    private int duration;
    private Date date;
    private int speed;
    private AID airport;
    private int fuel;
    private int state;
    private int distance;
    private String takeOffTrack;
    private String landingTrack;
    
    public enum Confirmation {
        TakeOff,
        Landing,
        Speed,
        Direction,
        State,
        Destinatio;
    }
    
    
    //private List<> interations; missing interactions Class
    public Flight(String message) {
        String[] messageArray = message.split(";");
        this.identification = messageArray[0];
        this.airplane = new AID(messageArray[1],false);
        this.passagers = Integer.parseInt(messageArray[2]);
        this.destination = new int[]{Integer.parseInt(messageArray[3]),Integer.parseInt(messageArray[4])};
        this.departure = new int[]{Integer.parseInt(messageArray[5]),Integer.parseInt(messageArray[6])};
        this.speed = Integer.parseInt(messageArray[7]);
        this.airport = new AID(messageArray[8],false);
        this.fuel = Integer.parseInt(messageArray[9]);
        state = Integer.parseInt(messageArray[10]);
        date = new Date(messageArray[11]);
        distance = Integer.parseInt(messageArray[12]);
        duration = Integer.parseInt(messageArray[13]);
    }
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
        duration = (int) ((distance/speed)/2);
    }

    public String getMsg() {
        return identification + ";" + airplane.getName().substring(0, 9) + ";" + passagers + ";" + destination[0] + ";"  + destination[1] + ";" + departure[0] + ";" + departure[1] + ";" + speed + ";" + airport.getName().substring(0, 9) + ";" + fuel + ";" + state + ";" + date + ";" + distance + ";" + duration ;
    }

    public String getIdentification() {
        return identification;
    }

    public AID getAirplane() {
        return airplane;
    }

    public void setState(int state) {
        this.state = state;
    }
    
    public int getPassagers() {
        return passagers;
    }

    public int[] getDestination() {
        if(tmpdestination != null){
            return tmpdestination;
        }else{
            return destination;
        }
    }

    public void setTmpdestination(int[] tmpdestination) {
        this.tmpdestination = tmpdestination;
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
    
    
    //Defining tracks, which will be used during takeoff and landing.
    public void setTakeOffTrack(String takeOffTrack)
    {
        this.takeOffTrack = takeOffTrack;
    }
    
    public String getTakeOffTrack()
    {
        return this.takeOffTrack;
    }
    
    
    public void setLandingTrack(String landingTrack)
    {
        this.landingTrack = landingTrack;
    }
    
    public String getLandingTrack()
    {
        return this.landingTrack;
    }
    
}
