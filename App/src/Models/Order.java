package Models;

import jade.core.AID;

public class Order extends Communication {

    public enum Type {
        TakeOff,
        Landing,
        Speed,
        Direction,
        State,
        Destinatio;
    }
    private Type type;
    private Flight flight;
    
    public Order(String identification, AID sender, AID receiver, Flight flight) {
        super(identification, sender, receiver);
        this.flight = flight;
    }   
    public Flight getFlight()
    {
        return this.flight;
    }
    
    public void setType(Type type)
    {
        this.type = type;
    }
    public Type getType()
    {
        return this.type;
    }
    
   
}