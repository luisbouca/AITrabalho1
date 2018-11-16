package Agents;

import Models.Flight;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.Random;

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
                if (msg.getPerformative() == ACLMessage.CFP) {
                    String[] locationString = msg.getContent().split(",");
                    location[0] = Integer.parseInt(locationString[0]);
                    location[1] = Integer.parseInt(locationString[1]);
                } else if (msg.getPerformative() == ACLMessage.INFORM) {
                    flight = new Flight(msg.getContent());
                    System.out.println("Informação do voo: " + flight.getDestination()[0] + "," + flight.getDestination()[1]);
                } else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                    System.out.println("Sou o aviao: " + getLocalName() + " com destino a: " + flight.getDestination()[0] + ", " + flight.getDestination()[1]);
                    flight.setState(1);
                    ContainerID destination = new ContainerID();
                    destination.setName("Air");
                    System.out.println(getLocalName() + " -> Moving to Container " + destination.getName());
                    doMove(destination);
                }
            } else {
                block();
            }
        }
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
                    distanceTemp=Math.sqrt(((Math.pow((flight.getDestination()[0] - location[0]), 2)) + (Math.pow((flight.getDestination()[1] - location[1]), 2))));
                    if(distanceTemp<safety_area){
                        //Faz pedido de atterragem
                    }else{
                        //Continua a movimentar-se
                    
                        
                        
                        
                        
                    location = getOptimalNextLocation(flight.getDestination(), location); 
                    System.out.println("I am: "+getLocalName()+" My new location is: "+location[0]+", "+location[1]); 
                    }
                }
            }

        }

        private int[] getOptimalNextLocation(int[] destino, int[] localizacaoAtual) {
            int x1, x2, y1, y2, width;
            int[]localizacao = new int[2];
            float test=0;
            int speed = 2;
            x1 = localizacaoAtual[0];
            y1 = localizacaoAtual[1];
            x2 = destino[0];
            y2 = destino[1];
            float ratio = (float)(y2 - y1) / (x2 - x1);
            width = x2 - x1;
            if(width<0){
                localizacao[0] = x1 - speed;
                localizacao[1] = (int) (y1 - (ratio * speed)); 
            }else{
                localizacao[0] = x1 + speed;
                localizacao[1] = (int) (y1 + (ratio * speed)); 
            }
            localizacaoAtual[1]=(int) test;
            // chose next location of the plane
            return localizacao;
        }

    }
}
