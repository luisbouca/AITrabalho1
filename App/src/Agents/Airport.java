package Agents;

import Models.Flight;
import Models.Track;
import Models.Operation;
import Models.Order;
import Models.Request;
import Models.TakeOff;
import jade.core.Agent;
import java.util.Random;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Airport extends Agent {

    private String name;
    private int[] location = new int[2];//Latitude|Longitude
    int arg2, arg1;
    private int max_airplanes;
    private List<Track> allocated_tracks = new ArrayList<>();//tracks available
    private List<AID> allocated_Airplanes = new ArrayList<>();//airplanes present here
    private List<AID> reserved_Spaces = new ArrayList<>();// airplanes that will come here
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
        arg1 = (int) args[0];
        arg2 = (int) args[1];

        //select name of this airport
        String[] names = new String[]{"Lisboa", "Faro", "Porto", "test1", "teste2", "teste3", "teste4", "teste5", "teste6", "teste7"};
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
        
        // add to the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Aiports" + arg1);
        sd.setName(getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        //behaviour that will notify each airplane about their location and add them to the list of airplanes
        this.addBehaviour(new getAirplanes(location, arg1));
        //receiver behaviour that will handle every message received
        this.addBehaviour(new Receiver());
        //ticker behaviour, will handle the queue and flight assignment
        this.addBehaviour(new CheckOperations(this, 5000));
        
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
            sd.setType(airportid + "Airplanes");
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
                        return super.onEnd();
                    }
                };
                myAgent.addBehaviour(pb);

                for (int i = 0; i < numplanes; ++i) {
                    airplanes[i] = result[i].getName();
                    System.out.println(airplanes[i].getName());
                    allocated_Airplanes.add(airplanes[i]);
                    pb.addSubBehaviour(new setAirplanesLocation(location, airplanes[i]));
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }

    private class Receiver extends CyclicBehaviour {

        private int numAeroportosProcessados = 0;
        List<String> what = new ArrayList<>();
        List<AID> airports = new ArrayList<>();
        Random rand = new Random();

        public Receiver() {
        }

        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                try{
                    //Getting communication data packet.
                    JSONObject receivedPacket = new JSONObject(msg.getContent());

                    switch (msg.getPerformative()) {
                        case ACLMessage.CFP: 
                            JSONObject packet = new JSONObject();
                            if ((allocated_Airplanes.size()+reserved_Spaces.size()) <= max_airplanes) {

                                System.out.println("Sou o aeroporto: " + getLocalName() + "e recebi pedido do: " + msg.getSender().getLocalName());
                                packet.put("state",1);
                                packet.put("lat",location[0]);
                                packet.put("lon",location[1]);
                                packet.put("Airplane",receivedPacket.getInt("Airplane"));
                            } else {
                                packet.put("state",0);
                            }
                            sendMessage(ACLMessage.INFORM_IF,new AID[]{msg.getSender()},packet.toString());
                            break;
                        case ACLMessage.INFORM_IF:
                            if(receivedPacket.has("state") && receivedPacket.has("lat") && receivedPacket.has("lon") &&receivedPacket.has("Airplane")){
                                numAeroportosProcessados++;
                                System.out.println(getLocalName()+": inquire received"+(numAeroportosProcessados) +","+(arg2-1));
                                if (receivedPacket.getInt("state") == 1) {
                                    receivedPacket.put("Airport",msg.getSender().toString());
                                    what.add(receivedPacket.toString());
                                    airports.add(msg.getSender());
                                }
                                if (numAeroportosProcessados >= arg2-1) {
                                    int[] destino = new int[2];
                                    int[] origem = new int[]{location[0], location[1]};
                                    int index = rand.nextInt(what.size());
                                    JSONObject please = new JSONObject(what.get(index));
                                    destino[0] = please.getInt("lat");
                                    destino[1] = please.getInt("lon");
                                    int passengers = rand.nextInt((100 - 50 + 1) + 50);
                                    Flight flight = new Flight(String.valueOf(arg1), allocated_Airplanes.get(please.getInt("Airplane")), passengers, destino, origem, 100, airports.get(index), 50);
                                    for(int i = 0; i<Operations.size();i++){
                                        Operation op = Operations.get(i);
                                        if(op.getRequest().getReceiver().equals(allocated_Airplanes.get(please.getInt("Airplane")))&& op.getRequest().getSender().equals(getAID())){
                                            Operations.get(i).setType(0);
                                            Operations.get(i).setFlight(flight);
                                            i = Operations.size();
                                        }
                                    }
                                    //To airport destination
                                    JSONObject packet1 = new JSONObject();
                                    packet1.put("Airplane",flight.getAirplane().toString());
                                    sendMessage(ACLMessage.CONFIRM,new AID[]{airports.get(index)},packet1.toString());
                                    //to airplane
                                    JSONObject packet2 = new JSONObject();
                                    packet2.put("Flight",flight.getMsg());
                                    sendMessage(ACLMessage.INFORM,new AID[]{allocated_Airplanes.get(please.getInt("Airplane"))},packet2.toString());
                                    numAeroportosProcessados = 0;
                                    what.removeAll(what);
                                }
                            }   
                            break;
                        case ACLMessage.CONFIRM:
                            //Checks confirmation message type.
                            if(receivedPacket.has(Flight.Confirmation.TakeOff.toString())){
                                //Gets track id that was allocated to a flight
                                String track_id = receivedPacket.getString(Flight.Confirmation.TakeOff.toString());
                                
                                //Trying to get track object.
                                Track track = allocated_tracks.stream().filter(x -> x.getId().equals(track_id)).findAny().orElse(null);
                                
                                //If object exits.
                                if(track != null){
                                    //Updates his state.
                                    int index = allocated_tracks.indexOf(track);
                                    track.setState(0);
                                    allocated_tracks.set(index, track);
                                }
                            }else if(receivedPacket.has(Flight.Confirmation.Landing.toString())){
                                //Gets track id that was allocated to a flight
                                String track_id = receivedPacket.getString(Flight.Confirmation.Landing.toString());
                                
                                //Trying to get track object.
                                Track track = allocated_tracks.stream().filter(x -> x.getId().equals(track_id)).findAny().orElse(null);
                                
                                //If object exits.
                                if(track != null){
                                    //Updates his state.
                                    int index = allocated_tracks.indexOf(track);
                                    track.setState(0);
                                    allocated_tracks.set(index, track);
                                }
                            }else if(receivedPacket.has("Airplane")){
                                reserved_Spaces.add(new AID(receivedPacket.getString("Airplane")));
                                System.out.println(getLocalName()+" reserved 1 space");
                            }   
                            break;
                        case ACLMessage.INFORM:

                        default:
                            break;
                    }
                }catch(Exception ex){
                    System.console().printf("Exception: "+ex.getMessage()); 
                }
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
            try {
                JSONObject packet = new JSONObject(); 
                packet.put("lat",location[0]);
                packet.put("lon",location[1]);
                sendMessage(ACLMessage.CFP,new AID[]{airplane},packet.toString());
            } catch (JSONException ex) {
                Logger.getLogger(Airport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    //Sending an confirmation message.
    private void sendMessage(int performative,AID[] receivers, String packet)
    {
        ACLMessage msg = new ACLMessage(performative);
        msg.setContent(packet);
        for(AID receiver :receivers){
            msg.addReceiver(receiver);
        }
        send(msg);
    }

    private class CheckOperations extends TickerBehaviour {

        public CheckOperations(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            //assign flight to free airplane
            List<Integer> lista = new ArrayList<>();
            lab1: for (int i = 0; i < allocated_Airplanes.size(); i++) {
                lista.add(0);
                for (int j = 0; j < Operations.size(); j++) {
                    if (allocated_Airplanes.get(i).equals(Operations.get(j).getRequest().getReceiver())) {
                        lista.set(i, 1);
                        continue lab1;
                    }
                }
            }
            for (int i = 0; i < lista.size(); i++) {
                System.out.println(allocated_Airplanes.get(i).getLocalName()+":"+lista.get(i));
            }

            for (int i = 0; i < lista.size(); i++) {
                if (lista.get(i) == 0) {
                    try {
                        JSONObject packet = new JSONObject(); 
                        packet.put("Airplane",String.valueOf(i));
                        
                        Request req = new Request(arg1+"REQ"+new Random().nextInt(1000000),getAID(),allocated_Airplanes.get(i),0);
                        Operation op = new Operation(req,-1);
                        Operations.add(op);
                        AID[] airports = new AID[arg2-1];
                        int tmp = 0;
                        for (int j = 0; j < arg2; j++) {
                            if (j != arg1) {
                                DFAgentDescription template = new DFAgentDescription();
                                ServiceDescription sd = new ServiceDescription();
                                sd.setType("Aiports" + j);
                                template.addServices(sd);

                                DFAgentDescription[] result;
                                result = DFService.search(myAgent, template);
                                if (result.length != 0) {
                                    airports[tmp] = result[0].getName();
                                }
                                tmp++;
                            }
                        }
                        sendMessage(ACLMessage.CFP,airports,packet.toString());
                        i = lista.size();
                    } catch (Exception e) {
                        Logger.getLogger(Airport.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
            
            //Iterate over all tracks, looking free track.
            for(Track track:allocated_tracks)
            {
                //If we get a track that is available.
                if(track.getState() == 0)
                {
                    //sort operations into 2 lists
                    List<Operation> takeoff = new ArrayList<>();
                    List<Operation> landing = new ArrayList<>();
                    for(int i = 0; i<Operations.size();i++){
                        if(Operations.get(i).getRequest().getType()== 0){
                            if(Operations.get(i).getType()==0){
                                takeoff.add(Operations.get(i));
                            }
                        }else{
                            if(Operations.get(i).getType()==0){
                                landing.add(Operations.get(i));
                            }
                        }
                    }
                    
                    //json object, that will send information to agent.
                    JSONObject packet = new JSONObject(); 
                    //Checks if the track is allocated for takeoffs or landings.
                    if(track.getType() == 0)
                    {
                        //ativate Takeoff operation
                        if(!takeoff.isEmpty()){
                            Operation opOriginal = takeoff.get(new Random().nextInt(takeoff.size()));
                                Operation op = opOriginal;
                                
                                    Order order = new Order(STATE_READY, getAID(), op.getRequest().getReceiver(), op.getRequest().getFlight());
                                        order.setType(Order.Type.TakeOff);
                                
                                op.setOrder(order);
                                op.setType(1);
                            Operations.set(Operations.indexOf(opOriginal),op);
                            
                            try
                            {
                                //Changing track state to occupied.
                                track.setState(1);
                                //Creating a packet that will be sendend to an agent.
                                packet.put(order.getType().toString(), track.getId());
                                sendMessage(ACLMessage.CONFIRM,new AID[]{op.getRequest().getReceiver()},packet.toString());
                            }
                            catch(Exception ex)
                            {
                                 System.console().printf("Exception: "+ex.getMessage());
                            }
                        }
                    }
                    else
                    {
                        if(!landing.isEmpty()){
                            //ativate Landing operation
                            Operation opOriginal = landing.get(new Random().nextInt(landing.size()));
                                Operation op = opOriginal;

                                    Order order = new Order(STATE_READY, getAID(), op.getRequest().getReceiver(), op.getRequest().getFlight());
                                        order.setType(Order.Type.Landing);

                                op.setOrder(order);
                                op.setType(1);
                            Operations.set(Operations.indexOf(opOriginal),op);

                            try
                            {
                                //Changing track state to occupied.
                                track.setState(1);
                                //Creating a packet that will be sendend to an agent.
                                packet.put(order.getType().toString(), track.getId());
                                sendMessage(ACLMessage.CONFIRM,new AID[]{op.getRequest().getReceiver()},packet.toString());
                            }
                            catch(Exception ex)
                            {
                                System.console().printf("Exception: "+ex.getMessage());
                            }
                        }
                    }
                        
                }
            }
        }

    }
}
