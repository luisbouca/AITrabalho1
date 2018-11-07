/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

/**
 *
 * @author user
 */
public class Track {
    private String id;
    private int type;
    private int state;

    public Track(String id, int type) {
        this.id = id;
        this.type = type;
        state = 0;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public int getType() {
        return type;
    }
    
    
    
    
}
