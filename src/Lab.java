import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
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
    public double probabilityOfApplying = 0.2; // probability of applying for a grant each cycle
    public double utility;

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
            if(this.topicX >= 200){
                this.topicX = 199;
            }
            if(this.topicX < 0){
                this.topicX = 0;
            }
            if(this.topicY >= 200){
                this.topicY = 199;
            }
            if(this.topicY < 0){
                this.topicY = 0;
            }
        }
        landscape.setObjectLocation(this, this.topicX, this.topicY);
    }

    private void doResearch(SimState state, IntGrid2D publications, DoubleGrid2D landscape){
        boolean appliedToGrant = applyToGrant(state, landscape);

        int numberOfResearchers = 1 + this.numberOfPostdocs; // how many will attempt to do research this turn? PI (1) + every postdoc

        for(int i = 0; i < numberOfResearchers; i++) { // loop through members of the lab
            double  probabilityOfResearch = 1 - (ScienceFunding.effortConstant * Math.log10(this.effort)); // calculate probability of doing research this turn

            if(appliedToGrant && i == 0) { // if you're the PI and you applied for a grant, get a reduced probability of doing research.
                probabilityOfResearch = probabilityOfResearch * ScienceFunding.costOfApplication; // multiplied by constant.
            }

            if (state.random.nextDouble() < probabilityOfResearch) { // if it does research after probability roll,
                boolean replication = false; // is it a replication?
                if (state.random.nextInt(100) < ScienceFunding.probabilityOfReplication && publications.get(this.topicX, this.topicY) > 0) { // check if replication (left) and if there's anything to replicate (right)
                    replication = true;
                }

                boolean hypothesisTruth; // is hypothesis true?
                // roll for truth
                hypothesisTruth = state.random.nextDouble() < landscape.get(this.topicX, this.topicY); // value on landscape is the probability that it's true

                double labFalsePositive = ScienceFunding.powerLevel / (1 + (1 - ScienceFunding.powerLevel) * this.effort); // calculate false positive rate. this is always the same, so maybe it's worth it to put it on a parameter.

                boolean labIsRight; // does the lab correctly detect truth of hypothesis?
                boolean publishingEffect; // positive = true; negative = false;

                if (hypothesisTruth) { // if the hypothesis is true
                    if (state.random.nextDouble() < ScienceFunding.powerLevel) { // with probability powerLevel, you detect positive effects
                        labIsRight = true; // is correct in thinking that it's positive
                        publishingEffect = true; // will publish positive
                    } else { // else, you think it's negative when it's positive.
                        labIsRight = false; // is wrong in thinking that it's negative
                        publishingEffect = false; // will publish negative
                    }
                } else { // if the hypothesis is false
                    if (state.random.nextDouble() < labFalsePositive) { // with probability false positive rate, you detect a positive effect
                        labIsRight = false; // you think it's positive when it's negative
                        publishingEffect = true; // you publish positive
                    } else {
                        labIsRight = false; // you think it's negative when it's negative
                        publishingEffect = false; // you publish negative
                    }
                }

                if (publishingEffect) { // if it's publishing a positive result
                    int currentPublicationsTopic = publications.get(this.topicX, this.topicY); // how many publications are there in this topic?
                    publications.set(this.topicX, this.topicY, currentPublicationsTopic + 1); // add a publication to this topic
                    LandscapeUtils.increaseAndDisperse(landscape, this.topicX, this.topicY, 0.001); // increase by 0.001 every time you publish

                    if (replication) { // if it's a replication'
                        this.clout += 0.5; // add 0.5 to your publication payoff
                    } else {
                        this.clout += 1; // it it's a novel result, add 1 to your publication payoff
                    }

                }
                if (!publishingEffect && (state.random.nextInt(100) < ScienceFunding.probabilityOfPublishingNegative)) { // if you're publishing negative, roll for negative publication. if you get it, publish.
                    int currentPublicationsTopic = publications.get(this.topicX, this.topicY); // how many publications are there in this topic?
                    publications.set(this.topicX, this.topicY, currentPublicationsTopic + 1); // add a publication to this topic
                    LandscapeUtils.increaseAndDisperse(landscape, this.topicX, this.topicY, 0.001); // increase by 0.001 every time you publish
                    if (replication) { // if it's a replication, add 0.5 to your payoffs.
                        this.clout += 0.5;
                    } else { // if it's not a replication, add 1
                        this.clout += 1;
                    }
                }
            }
        }
    }

    private boolean applyToGrant(SimState state, DoubleGrid2D landscape){

        // apply for funding to agency //

        if(state.random.nextDouble() < this.probabilityOfApplying){
            // calculate utility //
            double thisRate = landscape.get(this.topicX, this.topicY);
            double innovativeness = 1 - ((Math.log10(thisRate / 0.001)) / (Math.log10(0.5 / 0.001))); // innovativeness as per document of model description.
            double relativeRecord = this.clout / ScienceMaster.highestPublication; // your record in relationship with best record
            this.utility = ScienceFunding.weightOfInnovation * innovativeness + ScienceFunding.weightOfRecord * relativeRecord +  state.random.nextGaussian(); // utility as per model description. update your utility only if you're applying.
            //

            Agency.thisTurnsApplicants.add(this); // add myself to the bag of applicants for money
            return true; // spit out boolean for logic flow
        } else { return false;}
        //
    }

    public String toString(){
        return "" + labId;
    }
}
