package Models;

import jade.core.AID;

public class Landing{
    private int priority;
    private Track track;

    public Landing(int priority, Track track) {
        
        this.priority = priority;
        this.track = track;
    }

    public int getPriority() {
        return priority;
    }

    public Track getTrack() {
        return track;
    }
    
    
}
