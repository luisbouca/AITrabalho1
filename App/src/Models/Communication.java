package Models;

import java.util.Date;
import jade.core.AID;

public class Communication {
    private String identification;
    private AID sender;
    private AID receiver;
    private Date date;

    public Communication(String identification, AID sender, AID receiver) {
        this.identification = identification;
        this.sender = sender;
        this.receiver = receiver;
        date = new Date();
    }

    public String getIdentification() {
        return identification;
    }

    public AID getSender() {
        return sender;
    }

    public AID getReceiver() {
        return receiver;
    }

    public Date getDate() {
        return date;
    }
}
