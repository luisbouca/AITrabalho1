/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import jade.core.AID;

/**
 *
 * @author user
 */
public class Request extends Comunication{
    private Flight flight;

    public Request(String identification, AID sender, AID receiver, Flight flight) {
        super(identification, sender, receiver);
        this.flight = flight;
    }

    public Flight getFlight() {
        return flight;
    }    
    
}
