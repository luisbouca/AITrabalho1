package Models;

import jade.core.AID;

public class Request extends Communication{
    private Flight flight;
    private int type;//o - take off, 1- Landing, 2- Weather

    public Request(String identification, AID sender, AID receiver, int type) {
        super(identification, sender, receiver);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }
    
}