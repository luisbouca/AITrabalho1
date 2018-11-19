package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Interface extends Agent{
    private int numAeroports;
    private List<Integer> listaAvioes;

    @Override
    protected void setup() {
        
        Object[] args = getArguments(); 
        numAeroports = (int) args[0];
        listaAvioes = new ArrayList<>();
        
        this.addBehaviour(new Receiver());
        this.addBehaviour(new showStats(this, 5000));
        super.setup();
    }
    
    private class Receiver extends CyclicBehaviour{

        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg!=null){
                try{
                    //Getting communication data packet.
                    JSONObject receivedPacket = new JSONObject(msg.getContent());
                    
                    switch (msg.getPerformative()) {
                        case ACLMessage.INFORM: 
                            System.out.println("RECEBI O PACOTE: "+msg.getSender()+"com a msg, "+receivedPacket.getInt("numAero"));
                            listaAvioes.add(receivedPacket.getInt("numAero"));
                            /*if(listaAvioes.size()==numAeroports){
                                for(Integer ao: listaAvioes){
                                    System.out.println("REPORT: "+ao);
                                }
                                    }*/
                            break;
                            
                        default:
                            break;
                    }
                }catch(Exception ex){
                    System.console().printf("Exception: "+ex.getMessage()); 
                }
                if(msg.getPerformative()==ACLMessage.INFORM){
                    
                }
            }else{
                block();
            }
            
        }
    
}
    
    private class showStats extends TickerBehaviour {

        public showStats(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            
            
            for(int i=0; i<numAeroports; i++){
            //template to search for the airports
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Aiports"+i);
            template.addServices(sd);

            DFAgentDescription[] result;

            try {
                result = DFService.search(myAgent, template);
                AID[] aeroports;
                aeroports = new AID[result.length];
                int numplanes = result.length;
                    aeroports[0] = result[0].getName();
                    System.out.println(aeroports[0].getName());
                    
                JSONObject packet = new JSONObject(); 
                try {
                    packet.put("o","o");
                    sendMessage(ACLMessage.PROPOSE, new AID[]{aeroports[0]}, packet.toString());
                } catch (JSONException ex) {
                    Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            }
            
        }

    }
    private void sendMessage(int performative,AID[] receivers, String packet)
    {
        ACLMessage msg = new ACLMessage(performative);
        msg.setContent(packet);
        for(AID receiver :receivers){
            msg.addReceiver(receiver);
        }
        send(msg);
    }
    
    
}
