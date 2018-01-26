import sim.engine.*;
import sim.field.grid.SparseGrid2D;
import sim.util.*;

public class ScienceMaster implements Steppable { // It who determines the life and death of science labs

    public void step(SimState state){
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
        Lab dyingLab = (Lab) chosenLabs.get(state.random.nextInt(chosenLabs.size())); // chose one at random
        dyingLab.stoppable.stop(); // remove from schedule (die!!)
        spaceLabs.remove(dyingLab);
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
        int topicMutationX = state.random.nextInt(5); // mutate X
        if(state.random.nextBoolean()){
            topicMutationX = topicMutationX * -1;
        }
        int topicMutationY = state.random.nextInt(5); // mutate Y
        if(state.random.nextBoolean()){
            topicMutationY = topicMutationY * -1;
        }
        newLab.topicX += topicMutationX;
        newLab.topicY += topicMutationY;
        newLab.effort = reproducedLab.effort; // copy methodology. NEED TO CHANGE THIS FOR GAUSSIAN
        ScienceFunding.allLabs.remove(dyingLab); // remove it from list
        ScienceFunding.allLabs.add(newLab);
        newLab.stoppable = state.schedule.scheduleRepeating(newLab,0, 1); // allocate stoppable to kill in the future
        spaceLabs.setObjectLocation(newLab, newLab.topicX, newLab.topicY);
    }
}
