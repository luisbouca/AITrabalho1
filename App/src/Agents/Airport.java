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
        this.addBehaviour(new CheckOperations(this, 10000));
        
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
        List<List<Integer>> what = new ArrayList<List<Integer>>();
        List<AID> airports = new ArrayList<>();
        Random rand = new Random();

        public Receiver() {
        }

        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.CFP) {
                        ACLMessage resp = msg.createReply();
                        resp.setPerformative(jade.lang.acl.ACLMessage.INFORM_IF);
                        if ((allocated_Airplanes.size()+reserved_Spaces.size()) <= max_airplanes) {

                            System.out.println("Sou o aeroporto: " + getLocalName() + "e recebi pedido do: " + msg.getSender().getLocalName());
                            resp.setContent(location[0] + "," + location[1] + "," + msg.getContent());
                        } else {
                            resp.setContent("no");
                        }
                        send(resp);
                } else if (msg.getPerformative() == ACLMessage.INFORM_IF) {
                    numAeroportosProcessados++;
                    System.out.println(getLocalName()+": inquire received"+(numAeroportosProcessados) +","+(arg2-1));
                    if (!msg.getContent().equals("no")) {
                        String[] info = msg.getContent().split(",");
                        List<Integer> sub = new ArrayList<>();
                        sub.add(Integer.parseInt(info[0]));
                        sub.add(Integer.parseInt(info[1]));
                        sub.add(Integer.parseInt(info[2]));
                        airports.add(msg.getSender());
                        what.add(sub);
                    }                    
                    if (numAeroportosProcessados >= arg2-1) {
                        int[] destino = new int[2];
                        int[] origem = {location[0], location[1]};
                        int tmp,rando;
                        rando = rand.nextInt(what.size());
                        List<Integer> please = what.get(rando);
                        tmp = please.get(0);
                        destino[0] = tmp;
                        tmp = please.get(1);
                        destino[1] = tmp;
                        tmp = please.get(2);
                        int passengers = rand.nextInt((100 - 50 + 1) + 50);
                        Flight flight = new Flight(String.valueOf(arg1), allocated_Airplanes.get(tmp), passengers, destino, origem, 100, airports.get(rando), 50);
                        for(int i = 0; i<Operations.size();i++){
                            Operation op = Operations.get(i);
                            if(op.getRequest().getReceiver().equals(allocated_Airplanes.get(tmp))&& op.getRequest().getSender().equals(getAID())){
                                Operations.get(i).setType(0);
                                Operations.get(i).setFlight(flight);
                                break;
                            }
                        }
                        //To airport destination
                        ACLMessage msg1 = new ACLMessage();
                        msg1.setPerformative(ACLMessage.CONFIRM);
                        msg1.setContent(""+flight.getAirplane());
                        msg1.addReceiver(airports.get(rando));
                        send(msg1);
                        //to airplane
                        ACLMessage msg2 = new ACLMessage();
                        msg2.setPerformative(ACLMessage.INFORM);
                        msg2.setContent(flight.getMsg());
                        msg2.addReceiver(allocated_Airplanes.get(tmp));
                        send(msg2);
                        numAeroportosProcessados = 0;
                        what.removeAll(what);
                        System.out.println("Airport"+arg1+" assigned Airport"+flight.getAirport().getLocalName()+" to airplane"+tmp+"::"+flight.getAirplane().getLocalName());
                    }
                }else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                    reserved_Spaces.add(new AID(msg.getContent()));
                    System.out.println(getLocalName()+" reserved 1 space");
                }
            }else{
                block();
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
            message.setContent(location[0] + "," + location[1]);
            myAgent.send(message);
        }
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
                    ACLMessage message = new ACLMessage(ACLMessage.CFP);//TODO change this maybe
                    //message.addReceiver(airplane);
                    message.setContent(String.valueOf(i));
                    Request req = new Request(arg1+"REQ"+new Random().nextInt(1000000),getAID(),allocated_Airplanes.get(i),0);
                    Operation op = new Operation(req,-1);
                    Operations.add(op);
                    for (int j = 0; j < arg2; j++) {
                        if (j != arg1) {
                            DFAgentDescription template = new DFAgentDescription();
                            ServiceDescription sd = new ServiceDescription();
                            sd.setType("Aiports" + j);
                            template.addServices(sd);

                            DFAgentDescription[] result;
                            
                            try {
                                result = DFService.search(myAgent, template);
                                AID airports;
                                if (result.length != 0) {
                                    airports = result[0].getName();
                                    message.addReceiver(airports);
                                }

                            } catch (FIPAException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    myAgent.send(message);
                    i = lista.size();
                }
            }

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
            //ativate Takeoff operation
            if(!takeoff.isEmpty()){
                Operation opOriginal = takeoff.get(new Random().nextInt(takeoff.size()));
                Operation op = opOriginal;
                ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
                Order order = new Order(STATE_READY, getAID(), op.getRequest().getReceiver(), op.getRequest().getFlight());
                op.setOrder(order);
                op.setType(1);
                Operations.set(Operations.indexOf(opOriginal),op);
                msg.setContent("");
                msg.addReceiver(op.getRequest().getReceiver());
                send(msg);
            }

            //ativate Landing operation
        }

    }
}
