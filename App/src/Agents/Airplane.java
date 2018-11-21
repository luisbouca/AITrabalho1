package Agents;

import Models.Flight;
import Models.Order;
import Models.Request;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import jade.lang.acl.ACLMessage;
import jade.tools.gui.AIDAddressList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.json.JSONArray;

import org.json.JSONObject;
import org.json.JSONTokener;

public class Airplane extends Agent {

    private String make;
    private String model;
    private Flight flight;
    private int max_passanger;
    private int max_speed;//km/s(1 unidade = 10km)(1s = 1h)
    private int max_fuel;
    private int safety_area;
    private int[] location = new int[2];//Latitude|Longitude //Current agent location.

    private BlockingQueue<Neighbour> neighbours = new ArrayBlockingQueue<>(1000); //List of neighbours agents which may result in collision. 

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

    public int[] getLocation() {
        return location;
    }

    @Override
    protected void setup() {

        Random random = new Random();

        //get the arguments passed by the controller
        Object[] args = getArguments();
        int arg1 = (int) args[0];

        //choses the make of the airplane
        String[] makes = new String[]{"Ford", "Renaut", "Honda"};
        int makeint = random.nextInt(3);
        make = makes[makeint];
        //choses the model of the airplane
        String[] models = new String[]{"XPto27", "QUerTy", "MLPokn"};
        int modelint = random.nextInt(3);
        model = models[modelint];
        makeint++;
        modelint++;
        //generates the maximum number of passengers with a min of 50 and max of 200
        max_passanger = random.nextInt((200) - (50) + 1) + 50;
        //generates the maximum speed with a min of 3 and max of 10
        max_speed = random.nextInt((10) - (3) + 1) + 3;
        //generates the fuel capacity with a min of 50 and max of 100
        max_fuel = random.nextInt((100) - (50) + 1) + 50;
        //generates the safety area from the maximum speed
        safety_area = max_speed * 5;

        //yellow Pages sign
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(arg1 + "Airplanes");
        sd.setName(getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        this.addBehaviour(new Receiver());
        this.addBehaviour(new movePlane(this, 2000));

        super.setup();
    }

    //behabiour in charge of handling messages that the airplane receives
    private class Receiver extends CyclicBehaviour {

        @Override
        public void action() {

            ACLMessage msg = receive();
            if (msg != null) {
                if (msg.getPerformative() != ACLMessage.FAILURE) {
                    JSONObject receivedPacket = null;
                    try //Try catch for messsages that are not null or that cannot be converted to json.
                    {
                        //Getting communication data packet.
                        receivedPacket = new JSONObject(msg.getContent());
                    } catch (Exception ex) {
                    }
                    //Getting communication data packet.
                    switch (msg.getPerformative()) {
                        case ACLMessage.CFP:
                            try {
                                if (receivedPacket.has("lat") && receivedPacket.has("lon")) {
                                    System.out.println("received location");
                                    location[0] = receivedPacket.getInt("lat");
                                    location[1] = receivedPacket.getInt("lon");
                                }
                            } catch (JSONException ex) {
                                Logger.getLogger(Airplane.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                        case ACLMessage.INFORM:
                            try {
                                if (receivedPacket != null) {
                                    if (receivedPacket.has("Flight")) {

                                        System.out.println("received flight");
                                        flight = new Flight(receivedPacket.getString("Flight"));
                                        System.out.println("Informação do voo: " + flight.getDestination()[0] + "," + flight.getDestination()[1]);

                                    } else if (receivedPacket.has(Request.Type.Information.toString())) { //Receiving informations from another airplanes
                                        JSONObject subPacket = receivedPacket.getJSONObject(Request.Type.Information.toString());

                                        if (subPacket.has("location")) //If there is any location attribute.
                                        {
                                            //Converting string to json array, in order to extract location latitude and longitude.
                                            JSONArray locationArray = (JSONArray) new JSONObject(new JSONTokener("{arr:" + subPacket.getString("location") + "}")).get("arr");
                                            //Checking if lat or long is <= than safety_area.
                                            //if(((locationArray.getInt(0) - getLocation()[0]) <= safety_area) || ((locationArray.getInt(1) - getLocation()[1]) <= safety_area))

                                            //if((int) Math.sqrt(((Math.pow((locationArray.getInt(0) - getLocation()[0]), 2)) + (Math.pow((locationArray.getInt(1) - getLocation()[1]), 2))))+0.5 <= safety_area)
                                            if (true) {
                                                //Building Neighbour object.
                                                Neighbour neighbour = new Neighbour();
                                                neighbour.setAID(msg.getSender());

                                                if (subPacket.has("speed")) {
                                                    neighbour.setSpeed(subPacket.getInt("speed"));
                                                }
                                                if (subPacket.has("fuel")) {
                                                    neighbour.setSpeed(subPacket.getInt("fuel"));
                                                }
                                                if (subPacket.has("location")) {
                                                    neighbour.setLocation(new int[]{locationArray.getInt(0), locationArray.getInt(1)});
                                                }
                                                if (subPacket.has("destination")) {
                                                    //Converting string to json array, in order to extract destination latiture and longitude.
                                                    JSONArray destinationArray = (JSONArray) new JSONObject(new JSONTokener("{arr:" + subPacket.getString("location") + "}")).get("arr");
                                                    neighbour.setDestination(new int[]{destinationArray.getInt(0), destinationArray.getInt(1)});
                                                }
                                                neighbours.put(neighbour);
                                            }
                                        }
                                    }
                                } else {
                                    //Sending Requests information to all flights.
                                    //Getting message from AMS agent.
                                    jade.util.leap.Iterator it = ((Result) getContentManager().extractContent(msg)).getItems().iterator();
                                    while (it.hasNext()) //Iterating over a result list, that was retrieved by AMS Agent.
                                    {
                                        //Gets current agent aid
                                        AID aid = (AID) it.next();
                                        if (aid.getName().startsWith("Airplane") && !aid.getName().equals(getAID().getName())) //Sending request information, to all agents that are not this current agent or an airport agent.
                                        {
                                            sendMessage(ACLMessage.INFORM, new AID[]{aid}, new JSONObject().put(Request.Type.Information.toString(), new JSONObject().put("speed", flight.getSpeed()).put("location", Arrays.toString(getLocation())).put("fuel", flight.getFuel()).put("destination", Arrays.toString(flight.getDestination()))).toString());
                                        }
                                    }

                                }
                            } catch (Codec.CodecException ex) {
                                Logger.getLogger(Airplane.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (OntologyException ex) {
                                Logger.getLogger(Airplane.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (JSONException ex) {
                                Logger.getLogger(Airplane.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Airplane.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;

                        case ACLMessage.CONFIRM:

                            try {
                                //Checks if it is a takeoff order
                                System.out.println("received permission");
                                if (receivedPacket.has(Order.Type.TakeOff.toString())) {
                                    //Setting track to flight
                                    flight.setTakeOffTrack(receivedPacket.getString(Order.Type.TakeOff.toString()));
                                    flight.setState(1); //Changing flight status.

                                    JSONObject packet = new JSONObject();
                                    packet.put(Flight.Confirmation.TakeOff.toString(), receivedPacket.getString(Order.Type.TakeOff.toString()));
                                    //Sending a confirmation message to airport.
                                    sendMessage(ACLMessage.CONFIRM, new AID[]{msg.getSender()}, packet.toString());

                                    System.out.println("Sou o aviao: " + getLocalName() + " com destino a: " + flight.getDestination()[0] + ", " + flight.getDestination()[1]);
                                    //change container
                                    ContainerID destination = new ContainerID();
                                    destination.setName("Air");
                                    System.out.println(getLocalName() + " -> Moving to Container " + destination.getName());
                                    doMove(destination);
                                } else if (receivedPacket.has(Order.Type.Landing.toString())) { //Checks if it is a landing order.
                                    //Setting landing track to fligh.
                                    flight.setLandingTrack(receivedPacket.getString(Order.Type.Landing.toString()));

                                    //Sending a confirmation message to airport.
                                    sendMessage(ACLMessage.CONFIRM, new AID[]{msg.getSender()}, new JSONObject().put(Flight.Confirmation.Landing.toString(), receivedPacket.getString(Order.Type.Landing.toString())).toString());
                                    //change container
                                    ContainerID destination = new ContainerID();
                                    location = flight.getDestination();
                                    destination.setName("Container" + flight.getAirport().getName().substring(8, 9));
                                    System.out.println(getLocalName() + " -> Moving to Container " + destination.getName());
                                    doMove(destination);
                                    System.out.println("Atterrou");
                                }
                            } catch (JSONException ex) {

                            }
                            break;
                        case ACLMessage.REQUEST:

                            break;
                        default:
                            block();
                            break;
                    }
                }
            }
        }
    }

    //Sending an message.
    private void sendMessage(int performative, AID[] receivers, String packet) {
        ACLMessage msg = new ACLMessage(performative);
        msg.setContent(packet);
        for (AID receiver : receivers) {
            msg.addReceiver(receiver);
        }
        send(msg);

    }

    private class movePlane extends TickerBehaviour {

        double distanceTemp;

        public movePlane(Agent a, long period) {
            super(a, period);

        }

        @Override
        protected void onTick() {
            if (flight != null) {
                if (flight.getState() == 1) {
                    distanceTemp = Math.sqrt(((Math.pow((flight.getDestination()[0] - location[0]), 2)) + (Math.pow((flight.getDestination()[1] - location[1]), 2))));
                    System.out.println("distance=" + distanceTemp);
                    if (distanceTemp < safety_area) {
                        //Faz pedido de atterragem
                        System.out.println("In RANGE");
                        try {
                            JSONObject packet = new JSONObject();
                            packet.put("flight", flight.getMsg());
                            sendMessage(ACLMessage.REQUEST, new AID[]{flight.getAirport()}, packet.toString());
                            flight.setState(3); //Changing flight status.
                        } catch (JSONException ex) {
                            Logger.getLogger(Airplane.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        try {
                            //Requesting AMS to get a list of agents in this current container.
                            requestAMS();
                            //If there is any agent that is dangerously closer.
                            if (neighbours.size() > 0) {
                                negotiate(); //Starts a negotiation.
                            } else { //Moving normally this airplane.
                                moving();
                            }

                        } catch (Exception ex) {
                            System.console().printf("Exception: " + ex.getMessage());
                        }
                    }
                }
            }

        }

        private void negotiate() {
            try {
                //Checking which agent is closely to this agent.

                while (neighbours.iterator().hasNext()) //It could be more than one agent.
                {
                    Neighbour obj = neighbours.take();

                    System.out.println("agent location" + obj.location[0] + "/" + obj.location[1]);
                    
                    int distPlaneX = location[0] - obj.getLocation()[0];
                    int distPlaneDestX = location[0] - obj.getDestination()[0];
                    int distDestX = location[0] - flight.getDestination()[0];
                    
                    int distPlaneY = location[1] - obj.getLocation()[1];
                    int distPlaneDestY = location[1] - obj.getDestination()[1];
                    int distDestY = location[1] - flight.getDestination()[1];
                    
                    int distPlaneToPlaneDestY = obj.getLocation()[1] - obj.getDestination()[1];
                    int distDestToPlaneDestY = flight.getDestination()[1] - obj.getDestination()[1];
                    
                    if (distPlaneX == 0) {//neighbour in same X as us
                        if (distPlaneDestX == 0) {//neighbour destination in same X as us
                            if (distDestX == 0) {//our destination is in same X as us
                                if (distPlaneY >0) {//neighbour is below us
                                    if (distPlaneDestY==0) {//neighbour destination in same possition as us
                                        if (distDestY >0) {//our destination is below us
                                            //colision suggest right or left
                                        }
                                    }else if(distPlaneDestY>0){//neighbour destination below us
                                        if(distDestY >0 ){//our destination is below us
                                            if(distPlaneToPlaneDestY<0 && distDestToPlaneDestY<0){//neighbour destination above neighbour and our destination
                                                //colision suggest right or left
                                            }
                                        }
                                    }else{//neighbour destination above us
                                        if(distDestY >0){//our destination is below us
                                            //colision suggest right or left
                                            //not 100% true
                                        }
                                    }
                                }else if(distPlaneY<0){//neighbour is above us
                                    if (distPlaneDestY==0) {//neighbour destination in same possition as us
                                        if (distDestY <0) {//our destination is above us
                                            //colision suggest right or left
                                        }
                                    }else if(distPlaneDestY>0){//neighbour destination below us
                                        if(distDestY <0){//our destination is above us
                                            //colision suggest right or left
                                            //not 100% true
                                        }
                                    }else{//neighbour destination above us
                                        if(distDestY <0 ){//our destination is above us
                                            if(distPlaneToPlaneDestY>0 && distDestToPlaneDestY>0){//neighbour destination below neighbour and our destination
                                                //colision suggest right or left
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (distPlaneDestX > 0) {//neighbour destination is to our left
                            if (distDestX > 0) {//our destination is to our left
                                if (distPlaneY>0) {//neighbour below us
                                    if (distDestToPlaneDestY<0) {//neighbour destination sbove our destination
                                        //colision suggest top or bottom
                                    }
                                }else if (distPlaneY<0) {//neighbour above us
                                    if (distDestToPlaneDestY>0) {//neighbour destination below our destination
                                        //colision suggest top or bottom
                                    }
                                }
                            }
                        } else if (distDestX < 0) {//neighbour and our destination is to our right
                            if (distPlaneY>0) {//neighbour below us
                                if (distDestToPlaneDestY<0) {//neighbour destination sbove our destination
                                    //colision suggest top or bottom
                                }
                            }else if (distPlaneY<0) {//neighbour above us
                                if (distDestToPlaneDestY>0) {//neighbour destination below our destination
                                    //colision suggest top or bottom
                                }
                            }
                        }
                    } else if (distPlaneX > 0) {//neighbour to our left
                        if (distPlaneDestX == 0) {//neighbour destination in same X as us
                            if (distDestX > 0) {//our destination is to our left
                                //possible collision Scene 2
                            }
                        } else if (distPlaneDestX > 0) {//neighbour destination is to our left
                            if (distDestX > 0) {//our destination is to our left
                                //possible collision Scene 2
                            }
                        } else if (distDestX == 0) {//our destination is in same X as us
                            //possible collision Scene 2
                        } else if (distDestX > 0) {//our destination is to our left
                            //possible collision Scene 2
                        } else {
                            //possible collision Scene 2
                        }
                    } else//neighbour to our right
                    if (distPlaneDestX == 0) {//neighbour destination in same X as us
                        if (distDestX > 0) {//our destination is to our left
                            //possible collision Scene 2 inverted
                        }
                    } else if (distPlaneDestX > 0) {//neighbour destination is to our left
                        if (distDestX == 0) {//our destination is in same X as us
                            //possible collision Scene 2 inverted
                        } else if (distDestX > 0) {//our destination is to our left
                            //possible collision Scene 2 inverted
                        } else {
                            //possible collision Scene 2 inverted
                        }
                    } else if (distDestX < 0) {//our destination is to our right
                        //possible collision Scene 2 inverted
                    }

                }
            } catch (Exception ex) {
                System.console().printf("Exception: " + ex.getMessage());
            }
        }

        private void moving() {
            //Moving normally.
            location = getOptimalNextLocation(flight.getDestination(), location);
            System.out.println("I am: " + getLocalName() + " My new location is: " + location[0] + ", " + location[1]);
        }

        private int[] getOptimalNextLocation(int[] destino, int[] localizacaoAtual) {
            int x1, x2, y1, y2, width, height;
            int[] localizacao = new int[2];
            int speed = flight.getSpeed();
            x1 = localizacaoAtual[0];
            y1 = localizacaoAtual[1];
            x2 = destino[0];
            y2 = destino[1];
            float ratio = (float) (y2 - y1) / (x2 - x1);//calculates the ratio between x and y for the line between the points
            width = x2 - x1;//gets difrence between the points in the x axis
            height = y2 - y1;//gets difrence between the points in the y axis
            int yratio = (int) ((ratio * speed) + 0.5);// calculates the ratio with speed
            // if there is no diference between the points in the X axis
            System.out.println("width = " + width + "|heigh=" + height + "|yratio = " + yratio + "|speed=" + speed + "|ratio =" + ratio);
            if (width == 0) {
                // if the current location is the destination
                if (height == 0) {
                    // location stays the same
                    localizacao[0] = x1;
                    localizacao[1] = y1;
                    // if the destination is directly below the current location
                } else if (height < 0) {

                    localizacao[0] = x1;
                    if ((y1 - yratio) > 0) {
                        localizacao[1] = y1 - yratio;
                    } else {
                        localizacao[1] = 0;
                    }
                    // if the destination is directly above the current location
                } else {
                    localizacao[0] = x1;
                    if ((y1 + yratio) > 0) {
                        localizacao[1] = y1 + yratio;
                    } else {
                        localizacao[1] = 0;
                    }
                }
            } else if (width < 0) {
                // if the destination is directly to the left of the current location
                if (height == 0) {
                    if ((x1 - (speed - yratio)) > 0) {
                        localizacao[0] = x1 - (speed - yratio);
                    } else {
                        localizacao[0] = 0;
                    }
                    localizacao[1] = y1;
                    // if the destination is to the left and below the current location
                } else if (height < 0) {
                    if ((x1 - (speed - yratio)) > 0) {
                        localizacao[0] = x1 - (speed - yratio);
                    } else {
                        localizacao[0] = 0;
                    }
                    if ((y1 - yratio) > 0) {
                        localizacao[1] = y1 - yratio;
                    } else {
                        localizacao[1] = 0;
                    }
                    // if the destination is to the left and above the current location
                } else {
                    if ((x1 - (speed - yratio)) > 0) {
                        localizacao[0] = x1 - (speed - yratio);
                    } else {
                        localizacao[0] = 0;
                    }
                    if ((y1 + yratio) > 0) {
                        localizacao[1] = y1 + yratio;
                    } else {
                        localizacao[1] = 0;
                    }
                }
            } else // if the destination is directly to the right of the current location
             if (height == 0) {
                    if ((x1 + (speed - yratio)) > 0) {
                        localizacao[0] = x1 + (speed - yratio);
                    } else {
                        localizacao[0] = 0;
                    }
                    localizacao[1] = y1;
                    // if the destination is to the left and below the current location
                } else if (height < 0) {
                    if ((x1 + (speed - yratio)) > 0) {
                        localizacao[0] = x1 + (speed - yratio);
                    } else {
                        localizacao[0] = 0;
                    }
                    if ((y1 - yratio) > 0) {
                        localizacao[1] = y1 - yratio;
                    } else {
                        localizacao[1] = 0;
                    }
                    // if the destination is to the left and above the current location
                } else {
                    if ((x1 + (speed - yratio)) > 0) {
                        localizacao[0] = x1 + (speed - yratio);
                    } else {
                        localizacao[0] = 0;
                    }
                    if ((y1 + yratio) > 0) {
                        localizacao[1] = y1 + yratio;
                    } else {
                        localizacao[1] = 0;
                    }
                }
            return localizacao;
        }

        private void requestAMS() throws Codec.CodecException {
            try {
                QueryAgentsOnLocation ca = new QueryAgentsOnLocation();
                ca.setLocation(here()); //Getting current agent location.

                Action actExpr = new Action(getAMS(), ca);

                myAgent.getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);
                myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());

                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(getAMS());
                request.setOntology(JADEManagementOntology.getInstance().getName());
                request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                myAgent.getContentManager().fillContent(request, actExpr);
                send(request);

            } catch (Exception ex) {
                System.console().printf("Exception: " + ex.getMessage());
            }
        }

    }

    private class Neighbour {

        private AID aid;
        private int speed;
        private int[] location;
        private int[] destination;
        private int fuel;

        private Neighbour() {

        }

        public void setAID(AID aid) {
            this.aid = aid;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public void setLocation(int[] location) {
            this.location = location;
        }

        public void setDestination(int[] destination) {
            this.destination = destination;
        }

        public AID getAID() {
            return this.aid;
        }

        public int getSpeed() {
            return this.speed;
        }

        public int[] getLocation() {
            return this.location;
        }

        public int[] getDestination() {
            return this.location;
        }
    }

}
