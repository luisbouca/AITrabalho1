package Models;

import jade.core.AID;

public class TakeOff {
    private int priority;
    private Track track;

    public TakeOff( int priority, Track track) {
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
