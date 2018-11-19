package Agents;

import Models.Flight;
import Models.Order;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.tools.gui.AIDAddressList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Airplane extends Agent {

    private String make;
    private String model;
    private Flight flight;
    private int max_passanger;
    private int max_speed;//km/s(1 unidade = 10km)(1s = 1h)
    private int max_fuel;
    private int safety_area;
    private int[] location = new int[2];//Latitude|Longitude

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
                    try {
                        //Getting communication data packet.
                        JSONObject receivedPacket = new JSONObject(msg.getContent());
                        switch (msg.getPerformative()) {
                            case ACLMessage.CFP:
                                if (receivedPacket.has("lat") && receivedPacket.has("lon")) {
                                    System.out.println("received location");
                                    location[0] = receivedPacket.getInt("lat");
                                    location[1] = receivedPacket.getInt("lon");
                                }
                                break;
                            case ACLMessage.INFORM:
                                if (receivedPacket.has("Flight")) {
                                    System.out.println("received flight");
                                    flight = new Flight(receivedPacket.getString("Flight"));
                                    System.out.println("Informação do voo: " + flight.getDestination()[0] + "," + flight.getDestination()[1]);
                                }
                                break;
                            case ACLMessage.CONFIRM:
                                //Checks if it is a takeoff order
                                System.out.println("received permission");
                                if (receivedPacket.has(Order.Type.TakeOff.toString())) {
                                    //Setting track to flight
                                    flight.setTakeOffTrack(receivedPacket.getString(Order.Type.TakeOff.toString()));
                                    flight.setState(1); //Changing flight status.

                                    //Sending a confirmation message to airport.
                                    sendMessage(ACLMessage.CONFIRM, new AID[]{msg.getSender()}, new JSONObject().put(Flight.Confirmation.TakeOff.toString(), receivedPacket.getString(Order.Type.TakeOff.toString())).toString());

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
                                    destination.setName("Container" + flight.getAirport().getName().substring(8,9));
                                    System.out.println(getLocalName() + " -> Moving to Container " + destination.getName());
                                    doMove(destination);
                                    System.out.println("Atterrou");
                                }
                                break;
                            default:
                                block();
                                break;
                        }
                    } catch (Exception ex) {
                        System.console().printf("Exception: " + ex.getMessage());
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
                        //Continua a movimentar-se

                        location = getOptimalNextLocation(flight.getDestination(), location);
                        System.out.println("I am: " + getLocalName() + " My new location is: " + location[0] + ", " + location[1]);
                    }
                }
            }

        }

        // chose next location of the plane
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
            {
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
            }
            return localizacao;
        }

    }
}
