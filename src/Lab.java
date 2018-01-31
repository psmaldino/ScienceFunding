import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Double2D;
import sim.util.IntBag;
import sim.engine.*;


class Lab implements Steppable {

    private final int labId; // tracks the unique identity of a specific lab
    int topicX; // location
    int topicY; //
    int numberOfPostdocs; // how many postdocs does this have at the beginning of the cycle?
    IntBag grants; // how many grants does it have, and how long they have. each grant is a descending counter.
    int age; // how many cycles has this lab been alive for?
    double clout; // prestige due to publishing.
    double effort; // effort put into research. 1 to 100. initialized at 75 at the beginning of simulation.
    Stoppable stoppable; // stoppable to kill the lab
    double probabilityOfApplying = 0.2; // probability of applying for a grant each cycle
    private double utility; // utility for applying to grants

    Lab(int labId, int topicX, int topicY){ // create lab at designated location
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
        if(state.schedule.getSteps() != 0) { // don't do this the first turn.
            checkFunding(); // update your funding status and fire postdocs without funding
        }
        updateTopic(state, ScienceFunding.labs);
        doResearch(state, ScienceFunding.publications, ScienceFunding.landscape);
        updateFunding(); // update your funding after a year of research.
    }

    private void checkFunding(){
        this.numberOfPostdocs = grants.size(); // for each active grant, add a postdoc for this year.
    }

    private void updateTopic(SimState state, SparseGrid2D landscape){
        Double2D newLocation = LevyWalk.getNewLocation(new Double2D(this.topicX, this.topicY), state); // levy walk to somewhere else
        this.topicX = (int) newLocation.x; // set your location to the new location
        this.topicY = (int) newLocation.y;
        landscape.setObjectLocation(this, this.topicX, this.topicY);
    }

    private void doResearch(SimState state, IntGrid2D publications, DoubleGrid2D landscape){
        boolean appliedToGrant = applyToGrant(state, landscape); // did you apply to a grant this turn?

        int numberOfResearchers = 1 + this.numberOfPostdocs; // how many will attempt to do research this turn? PI (1) + every postdoc

        for(int i = 0; i < numberOfResearchers; i++) { // loop through members of the lab
            double  probabilityOfResearch = 1 - (ScienceFunding.effortConstant * Math.log10(this.effort)); // calculate probability of doing research this turn

            if(appliedToGrant && i == 0) { // if you're the PI and you applied for a grant, get a reduced probability of doing research.
                probabilityOfResearch = probabilityOfResearch * ScienceFunding.costOfApplication; // multiplied by constant.
            }

            if (state.random.nextDouble() < probabilityOfResearch) { // if it does research after probability roll,
                boolean replication = false; // is it a replication?
                if (state.random.nextDouble() < ScienceFunding.probabilityOfReplication && publications.get(this.topicX, this.topicY) > 0) { // check if replication (left) and if there's anything to replicate (right)
                    replication = true;
                }

                boolean hypothesisTruth; // is hypothesis true?
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
                        labIsRight = true; // you think it's negative when it's negative
                        publishingEffect = false; // you publish negative
                    }
                }

                if (publishingEffect) { // if it's publishing a positive result
                    Globals.numberOfPublications++; // add one to publication counter

                    int currentPublicationsTopic = publications.get(this.topicX, this.topicY); // how many publications are there in this topic?
                    publications.set(this.topicX, this.topicY, currentPublicationsTopic + 1); // add a publication to this topic
                    LandscapeUtils.increaseAndDisperse(landscape, this.topicX, this.topicY, 0.001); // increase by 0.001 every time you publish

                    if(!labIsRight){
                        Globals.falseDiscoveries++;
                    } // if you're publishing a false discovery, add 1 to globals tracker

                    if (replication) { // if it's a replication'
                        this.clout += 0.5; // add 0.5 to your publication payoff
                    } else {
                        this.clout += 1; // it it's a novel result, add 1 to your publication payoff
                    }

                }
                if (!publishingEffect && (state.random.nextInt(100) < ScienceFunding.probabilityOfPublishingNegative)) { // if you're publishing negative, roll for negative publication. if you get it, publish.
                    Globals.numberOfPublications++;
                    if(!labIsRight){
                        Globals.falseDiscoveries++;
                    } // if you're publishing a false discovery, add 1 to globals.
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
        // calculate utility //
        double thisRate = landscape.get(this.topicX, this.topicY);
        double innovativeness = 1 - ((Math.log10(thisRate / 0.001)) / (Math.log10(0.5 / 0.001))); // innovativeness as per document of model description.
        double relativeRecord = this.clout / ScienceMaster.highestPublication; // your record in relationship with best record
        this.utility = ScienceFunding.weightOfInnovation * innovativeness + ScienceFunding.weightOfRecord * relativeRecord; // utility as per model description. update your utility only if you're applying. agency will add the error.
        //

        // apply for funding to agency //

        if(state.random.nextDouble() < this.probabilityOfApplying){
            Agency.thisTurnsApplicants.add(this); // add myself to the bag of applicants for money
            return true; // spit out boolean for logic flow
        } else { return false;}
        //
    }

    private void updateFunding(){  // reduce each grant's year by a year, and clear the ones that run out.
        for(int i = 0; i < this.grants.size(); i++){ // loop through grants, diminish them by one, if it's 0 delete it.
            int myGrant = this.grants.get(i);
            myGrant--; // diminish by one year for the year just past.
            if(myGrant <= 0){
                this.grants.remove(myGrant); // remove yourself if you ran out.
            }
        }

    }

    public double getUtility() {
        return utility;
    }

    public void setUtility(double newUtility){
        this.utility = newUtility;
    }

    public double getClout(){
        return clout;
    }

    public int getNumberOfPostdocs(){
        return numberOfPostdocs;
    }

    public int getAge(){
        return age;
    }

    public int getLabId(){
        return labId;
    }

    public Double2D getLocation(){
        return new Double2D(topicX, topicY);
    }

    public int[] getGrants(){
        return grants.toArray();
    }
}
