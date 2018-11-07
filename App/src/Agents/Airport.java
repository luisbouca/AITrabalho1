/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import Models.Flight;
import Models.Track;
import Models.Operation;
import jade.core.Agent;
import java.util.Random;
import jade.core.AID;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author user
 */

public class Airport extends Agent{

    private String name;
    private String location;
    private int max_airplanes;
    private List<Track> allocated_tracks = new ArrayList<>();
    private List<AID> allocated_Airplanes = new ArrayList<>();
    private int lat;
    private int lon;
    private List<Flight> allocated_Flights = new ArrayList<>();
    private Operation[] Operations;

    @Override
    protected void setup() {
        Random random = new Random();
        int randomint = random.nextInt(3);
        String[] names=new String[]{"Sá Carneiro","Jesuita","Belem"};
        name = names[randomint];
        String[] locations = new String[]{"Lisboa","Faro","Porto"};
        location = locations[randomint];
        max_airplanes = random.nextInt(((10)-4)+1)-4;
        int numtracks = random.nextInt(((10)-4)+1)-4;
        for(int i = 0; i<numtracks ;i++){
            allocated_tracks.add(new Track("Track"+i,i%2));
        }
        max_airplanes = random.nextInt((10)-4+1)-4;
        
        Object[] args = getArguments();
        int arg1 = (int) args[0]; // this returns the String "1"
        int[] GPS =genGPS(arg1);
        lat = GPS[0];
        lon = GPS[1];
        System.out.println("Sou o aeroporto"+arg1+" tou no lat:"+lat+"|lon:"+lon);
        super.setup(); //To change body of generated methods, choose Tools | Templates.
    }
    
    private int[] genGPS(int number){
        lat = (((number*number) *100)-((new Random().nextInt(5*(number+1)))*number));
        lon = (((number*number) * 100)-((new Random().nextInt(5*(number+1)))*number));
        return new int[]{lat,lon};
    }
    
    
}
