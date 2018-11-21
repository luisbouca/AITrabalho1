/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

import jade.core.AID;
/**
 *
 * @author user
 */
public class Slot {
    private AID airplane;
    private int type;//0-in storage,1-reserved,2-unused    

    public Slot(AID airplane,boolean storage) {
        this.airplane = airplane;
        if (storage) {
            type = 0;
        }else{
            type = 1;
        }
    }

    public AID getAirplane() {
        return airplane;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
}
