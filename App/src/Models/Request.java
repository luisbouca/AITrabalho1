package Models;

import jade.core.AID;

public class Request extends Communication{
    private Flight flight;
    private int type;//o - take off, 1- Landing, 2- Weather

    public Request(String identification, AID sender, AID receiver, Flight flight, int type) {
        super(identification, sender, receiver);
        this.flight = flight;
        this.type = type;
    }
}
