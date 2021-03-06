package Models;

import java.util.Date;

public class Operation {
    
    private Request req;
    private Date date;
    private int type;
    private Order order;
    private int trackId; 
    
    public Operation(Request req,int type) {
        date = new Date();
        this.req = req;
        this.type = type;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
    
    public Request getRequest() {
        return req;
    }

    public Order getOrder() {
        return order;
    }

    public int getTackId(){
        return trackId;
    }

    public void setTackId(int trackId){
        this.trackId = trackId;
    }

    public int getType() {
        return type;
    }

    public void setType(int i) {
        type = i;
    }
    
    public void setFlight(Flight i) {
        req.setFlight(i);
    }
    
    
    
    
}
