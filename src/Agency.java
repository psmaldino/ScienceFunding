import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;

public class Agency implements Steppable {
    static Bag thisTurnsApplicants; // renewable bag to store everyone applying for funds

    public void step(SimState state){
        // TODO step resets bag at the end. Don't forget!!
        // TODO how to determine big and small grants? important!
    }
}
