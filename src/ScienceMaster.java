import sim.engine.*;
import sim.field.grid.SparseGrid2D;
import sim.util.*;
import java.util.Comparator;

import java.util.Comparator;

public class ScienceMaster implements Steppable { // It who determines the life and death of science labs
    static double highestPublication; // the highest publication record measured for t-1.

    public void step(SimState state){
        updateHighest();

        Lab dyingLab = killALab(state, ScienceFunding.allLabs, ScienceFunding.labs);
        createALab(state, ScienceFunding.allLabs, dyingLab, ScienceFunding.labs);
    }

    private Lab killALab(SimState state, Bag allLabs, SparseGrid2D spaceLabs){
        Bag chosenLabs = new Bag(); // bag to store the labs that are chosen to participate in the drawing
        for(int i = 0; i < 10; i++){// chose 10 random labs
            Lab aLab;
            do{
                aLab = (Lab) allLabs.get(state.random.nextInt(allLabs.size()));
            } while(chosenLabs.contains(aLab));
            chosenLabs.add(aLab);
        }
        chosenLabs.sort((Comparator<Lab>) (L1, L2) -> { // order them according to age
            int L1Age = L1.age;
            int L2Age = L2.age;
            return Integer.compare(L1Age, L2Age);
        });
        Lab dyingLab = (Lab) chosenLabs.pop(); // get the oldest one
        dyingLab.stoppable.stop(); // remove from schedule (die!!)
        spaceLabs.remove(dyingLab); // remove it from epistemic landscape
        return dyingLab;
    }

    private void createALab(SimState state, Bag allLabs, Lab dyingLab, SparseGrid2D spaceLabs){
        Bag ticketBag = new Bag(); // create an empty bag

        for(int i = 0; i < allLabs.size(); i++){
            Lab thisLab = (Lab) allLabs.get(i); // get a lab
            int numberOfPostdocs = thisLab.numberOfPostdocs;
            if(numberOfPostdocs == 0){
                continue;
            }
            for(int n = 0; n < numberOfPostdocs; n++){ // for each postdoc, add the lab to the ticketbag. if it's 0, then don't add anything
                ticketBag.add(thisLab);
            }
        }

        Lab reproducedLab = (Lab) ticketBag.get(state.random.nextInt(ticketBag.size())); // grab a random one from the ticket bag. more postdocs, more chances.

        ScienceFunding.latestId++; // update id for new lab
        Lab newLab = new Lab(ScienceFunding.latestId, reproducedLab.topicX, reproducedLab.topicY); // create new lab before mutation

        // topic mutation //

        do {
            int Xvariation = state.random.nextInt(2); // move 2 steps max
            int Yvariation = state.random.nextInt(2); // move 2 steps max
            if (state.random.nextBoolean()) { // positive or negative?
                Xvariation = Xvariation * -1;
            }
            if (state.random.nextBoolean()) { // positive or negative?
                Yvariation = Yvariation * -1;
            }
            newLab.topicX = newLab.topicX + Xvariation;
            newLab.topicY = newLab.topicY + Yvariation;
        } while((newLab.topicX >= 200 || newLab.topicX < 0) || (newLab.topicY >= 200 || newLab.topicY < 0)); // cap both at 0 - 199.

        // methodology mutation //

        newLab.effort = reproducedLab.effort; // copy methodology.

        if(state.random.nextDouble() < ScienceFunding.probabilityOfMutationEffort) { // roll for mutation
            do {double effortMutation = state.random.nextGaussian(); // draw from a gaussian with stand dev of 1. cap final value at 1 - 100.
            reproducedLab.effort += effortMutation;}
            while(reproducedLab.effort > 100 || reproducedLab.effort < 1);

        }

        // applying to funding mutation //

        if(ScienceFunding.mutateFunding && state.random.nextDouble() < ScienceFunding.probabilityOfMutationFunding){ // depends on global parameter
            do{
                double mutation = state.random.nextGaussian() * 0.001; // 1% of variability
                reproducedLab.probabilityOfApplying += mutation;
            } while(reproducedLab.probabilityOfApplying < 0.0001 || reproducedLab.probabilityOfApplying > 1); // cap at 0.0001 - 1.
        }

        // kill old lab and add new lab //

        ScienceFunding.allLabs.remove(dyingLab); // remove old lab from list of all labs
        ScienceFunding.allLabs.add(newLab); // add new lab to list of all labs
        newLab.stoppable = state.schedule.scheduleRepeating(newLab,0, 1); // add new lab to schedule and allocate stoppable to kill in the future
        spaceLabs.setObjectLocation(newLab, newLab.topicX, newLab.topicY); // add new lab to epistemic landscape
    }

    private void updateHighest(){ // look through bag for the highest one
        double highestYet = 0;
        for(int i = 0; i < ScienceFunding.allLabs.size(); i++){
            Lab aLab = (Lab) ScienceFunding.allLabs.get(i);
            double record = aLab.clout;
            if(record > highestYet){
                highestYet = record;
            }
        }
        highestPublication = highestYet;
    }
}
