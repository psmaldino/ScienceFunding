import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.util.Bag;
import sim.util.IntBag;

public class Globals implements Steppable { // Global agent that updates the stats
    static double falseDiscoveries = 0;
    static double numberOfPublications = 0;
    static double falseDiscoveryRate;
    static double rateOfDiscovery;

    public Globals(){
    }

    public void step(SimState state){
        updateGlobals();
    }

    private void updateGlobals(){
        // TODO very rudimentary globals. Have to define them later. //

        // false discovery //

        falseDiscoveryRate = falseDiscoveries / numberOfPublications; // what's the rate of published articles that are false discoveries?

        // landscape discovery rate //

        IntGrid2D thePubs = ScienceFunding.publications;
        int[] pubsArray = thePubs.toArray();
        int exploredTopics = 0; // number of topics with more than 0 publications

        for(int i = 0; i < pubsArray.length; i++){
            if(pubsArray[i] > 0){
                exploredTopics++;
            }
        }
        rateOfDiscovery = (double) exploredTopics / pubsArray.length; // rate of discovery: percentage of topics with more than publications.
    }
}