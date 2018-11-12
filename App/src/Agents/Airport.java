package Agents;

import Models.Flight;
import Models.Track;
import Models.Operation;
import Models.TakeOff;
import jade.core.Agent;
import java.util.Random;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;

public class Airport extends Agent {

    private String name;
    private int[] location = new int[2];//Latitude|Longitude
    private int max_airplanes;
    private List<Track> allocated_tracks = new ArrayList<>();//tracks available
    private List<AID> allocated_Airplanes = new ArrayList<>();//airplanes present here
    private List<Flight> allocated_Flights = new ArrayList<>();
    private List<Operation> Operations = new ArrayList<>();//queue of operations

    public String getAirportName() {
        return name;
    }

    public int[] getLocation() {
        return location;
    }

    public int getMax_airplanes() {
        return max_airplanes;
    }

    public List<Track> getAllocated_tracks() {
        return allocated_tracks;
    }

    public List<AID> getAllocated_Airplanes() {
        return allocated_Airplanes;
    }

    public List<Flight> getAllocated_Flights() {
        return allocated_Flights;
    }

    @Override
    protected void setup() {
        Random random = new Random();
        //get the arguments passed by the controller
        Object[] args = getArguments();
        int arg1 = (int) args[0];

        //select name of this airport
        String[] names = new String[]{"Lisboa", "Faro", "Porto","","","","","","",""};
        name = names[arg1];

        //calculate the maximum number of airplanes with a minimum of 4 and maximum of 10
        max_airplanes = random.nextInt(((10) - 4) + 1) + 4;
        //calculate the number of tracks with a minimum of 1 and maximum of 5
        int numtracks = random.nextInt(((5) - 1) + 1) + 1;
        //initialize the tracks
        for (int i = 0; i < numtracks; i++) {
            allocated_tracks.add(new Track("Track" + i, i % 2));
        }
        //generate the location of the airport from the number of the airport
        location = genGPS(arg1);
        System.out.println("Sou o aeroporto" + arg1 + " tou no lat:" + location[0] + "|lon:" + location[1]);
        //behaviour that will notify each airplane about their location and add them to the list of airplanes
        this.addBehaviour(new getAirplanes(location,arg1));
        super.setup();
    }

    //generates a location from the number suplied
    private int[] genGPS(int number) {
        int lat = (((number * number) * 100) - ((new Random().nextInt(50 * (number + 1))) * number));
        int lon = (((number * number) * 100) - (new Random().nextInt(50) * number * number));
        return new int[]{lat, lon};
    }

    private class getAirplanes extends OneShotBehaviour {

        int[] location;
        int airportid;

        public getAirplanes(int[] location, int airportid) {
            this.location = location;
            this.airportid = airportid;
        }

        @Override
        public void action() {

            //template to search for the airplanes
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(airportid+"Airplanes");
            template.addServices(sd);

            DFAgentDescription[] result;

            try {
                result = DFService.search(myAgent, template);
                AID[] airplanes;
                airplanes = new AID[result.length];
                int numplanes = result.length;
                //when found the airplanes are added to the list and the setAirplanesLocation behaviour is called for each airplane
                ParallelBehaviour pb = new ParallelBehaviour(myAgent, ParallelBehaviour.WHEN_ALL) {

                    @Override
                    public int onEnd() {
                        System.out.println("All Airplanes inquired.");
                        return super.onEnd();
                    }
                };
                myAgent.addBehaviour(pb);

                for (int i = 0; i < numplanes; ++i) {
                    airplanes[i] = result[i].getName();
                    System.out.println(airplanes[i].getName());
                    allocated_Airplanes.add(airplanes[i]);
                    pb.addSubBehaviour(new setAirplanesLocation(location,airplanes[i]));
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }


    private class setAirplanesLocation extends OneShotBehaviour {

        int[] location;
        AID airplane;

        public setAirplanesLocation(int[] location, AID airplane) {
            this.location = location;
            this.airplane = airplane;
        }

        @Override
        public void action() {
            //sends the message with the location to the designated airplane
            ACLMessage message = new ACLMessage(ACLMessage.CFP);//TODO change this maybe
            message.addReceiver(airplane);
            message.setContent(location[0]+","+location[1]);
            myAgent.send(message);

        }

    }
}
