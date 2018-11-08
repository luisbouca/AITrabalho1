package Models;

import java.util.Date;

public class Operation {
    private Request req;
    private Date date;
    private int priority;
    private Order order;


    public Operation(Request req,int priority,Order order) {
        date = new Date();
        this.req = req;
        this.priority = priority;
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public int getPriority() {
        return priority;
    }
    
    
    
    
    
}
