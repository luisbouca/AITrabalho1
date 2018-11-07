/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

import java.util.Date;
import jade.core.AID;

/**
 *
 * @author user
 */
public class Comunication {
    private String identification;
    private AID sender;
    private AID receiver;
    private Date date;

    public Comunication(String identification, AID sender, AID receiver) {
        this.identification = identification;
        this.sender = sender;
        this.receiver = receiver;
        date = new Date();
    }

    public String getIdentification() {
        return identification;
    }

    public AID getSender() {
        return sender;
    }

    public AID getReceiver() {
        return receiver;
    }

    public Date getDate() {
        return date;
    }
}
