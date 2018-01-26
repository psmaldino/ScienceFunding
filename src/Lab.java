import sim.engine.Steppable;
import sim.util.*;
import sim.util.IntBag;
import sim.engine.*;


public class Lab implements Steppable {

    public int labId; // tracks the unique identity of a specific lab
    int topicX; // location
    int topicY; //
    public int numberOfPostdocs; // how many postdocs does this have at the beginning of the cycle?
    public IntBag grants; // how many grants does it have, and how long they have. each grant is a descending counter.
    public int age; // how many cycles has this lab been alive for?
    public double clout; // prestige due to publishing.
    public double effort;
    public Stoppable stoppable;

    public Lab(int labId, int topicX, int topicY){ // create lab at designated location
        this.labId = labId;
        this.topicX = topicX;
        this.topicY = topicY;
        this.numberOfPostdocs = 0;
        this.grants = new IntBag();
        this.age = 0;
        this.clout = 0;
    }

    public void step(SimState state){
        // System.out.println(this.labId);
    }


    public String toString(){
        return "" + labId;
    }
}
