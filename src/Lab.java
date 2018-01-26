import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.IntBag;
import sim.engine.*;
import java.lang.Math.*;


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
        age++;
        updateTopic(state, ScienceFunding.labs);
        doResearch(state, ScienceFunding.publications, ScienceFunding.landscape);
    }

    private void updateTopic(SimState state, SparseGrid2D landscape){
        // CHANGE FOR LEVY FLIGHT //
        if(state.random.nextInt(100) >= 50) { // 10% chance
            int Xvariation = state.random.nextInt(3);
            int Yvariation = state.random.nextInt(3);
            if (state.random.nextBoolean()) {
                Xvariation = Xvariation * -1;
            }
            if (state.random.nextBoolean()) {
                Yvariation = Yvariation * -1;
            }
            this.topicX = this.topicX + Xvariation;
            this.topicY = this.topicY + Yvariation;
        }
        landscape.setObjectLocation(this, this.topicX, this.topicY);
    }

    private void doResearch(SimState state, IntGrid2D publications, DoubleGrid2D landscape){
        int numberOfResearchers = 1 + this.numberOfPostdocs; // how many will attempt to do research this turn? PI (1) + every postdoc

        double probabilityOfResearch = 1 - (ScienceFunding.effortConstant * Math.log10(this.effort)); // calculate probability of doing research this turn

        if(state.random.nextDouble() < probabilityOfResearch) { // if it does research after probability roll,
            boolean replication = false; // is it a replication?
            if (state.random.nextInt(100) < ScienceFunding.probabilityOfReplication && publications.get(this.topicX, this.topicY) > 0) { // check if replication (left) and if there's anything to replicate (right)
                replication = true;
            }

            boolean hypothesisTruth; // is hypothesis true?

            double probabilityOfTruth = landscape.get(this.topicX, this.topicY); // base rate at this time
            System.out.println(probabilityOfTruth);
            if(state.random.nextDouble() < probabilityOfTruth) { // roll for truth
                hypothesisTruth = true;
            } else {hypothesisTruth = false;}

        }
    }


    public String toString(){
        return "" + labId;
    }
}
