package Models;

import jade.core.AID;

public class Order extends Communication {
    private Flight flight;
    private  

    public Order(String identification, AID sender, AID receiver, Flight flight) {
        super(identification, sender, receiver);
        this.flight = flight;
    }   
    
    public Flight getFlight()
    {
        return this.flight;
    }
   
}