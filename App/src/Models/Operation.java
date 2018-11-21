package Models;

import java.util.Date;

public class Operation {

    private Request req;
    private Date date;
    private int type;//-1 - not finished | 0 - waiting for permision | 1 - in process
    private Order order;
    private int trackId;

    public Operation(Request req, int type) {
        date = new Date();
        this.req = req;
        this.type = type;
    }

    public String getMsg() {
        String ret = date.toString();
        switch (type) {
            case 0:
                //requeste e type
                ret = ret+";REQ:"+req.getMsg()+";TIPO:"+type;
                break;
            case 1:
                //request type e track
                ret = ret+";REQ:"+req.getMsg()+";TIPO:"+type+trackId;
                break;
            case 2:
                ret = ret+";REQ:"+req.getMsg()+";ORDER:"+order.getMsg()+";TIPO:"+type+trackId;
                //tudo
                
                break;

        }
        //return date.toString() + ";" + trackId + ";" + req.getMsg() + ";" + order.getMsg();
        return ret;
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

    public int getTackId() {
        return trackId;
    }

    public void setTackId(int trackId) {
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
