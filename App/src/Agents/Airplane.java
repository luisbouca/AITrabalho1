/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import jade.core.Agent;
import java.util.Random;

/**
 *
 * @author user
 */

public class Airplane extends Agent{

    private String make;
    private String model;
    private int max_passanger;
    private int max_speed;//km/s(1 unidade = 10km)(1s = 1h)
    private int max_fuel;
    private int safety_area;
    private int lat;
    private int lon;

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public int getMax_passanger() {
        return max_passanger;
    }

    public int getMax_speed() {
        return max_speed;
    }

    public int getMax_fuel() {
        return max_fuel;
    }

    public int getSafety_area() {
        return safety_area;
    }

    public int[] getGPS() {
        return new int[]{lat,lon};
    }
    
    @Override
    protected void setup() {
        Random random = new Random();
        String[] makes=new String[]{"Ford","Renaut","Honda"};
        int makeint = random.nextInt(3);
        make = makes[makeint];
        String[] models = new String[]{"XPto27","QUerTy","MLPokn"};
        int modelint = random.nextInt(3);
        model = models[modelint];
        makeint++;
        modelint++;
        max_passanger = random.nextInt((200)-(50)+1)-(50);
        max_speed = random.nextInt((10)-(3)+1)-(3);
        max_fuel = random.nextInt((100)-(50)+1)-(50);
        safety_area = max_speed *5;
        super.setup();
    }

    
    

    
    
    
}
