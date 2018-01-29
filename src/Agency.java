import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.DoubleBag;

import java.util.Comparator;

public class Agency implements Steppable {
    static Bag thisTurnsApplicants; // renewable bag to store everyone applying for funds. labs insert themselves into the bags during their steps.
    public int budget = 20; // how many grants can it fund each cycle?. settable parameter. TODO temporary code!
    public double proportionOfBigGrants = 0.25; // proportion of total grants awarded to big grants.

    public Agency(){
        thisTurnsApplicants = new Bag(); // initialize bag along with funding agency
    }

    public void step(SimState state){
        // TODO how to determine big and small grants? important! this is TEMPORARY CODE. need to replace it with final agency behavior.

        // add error to utilities //
            // calculate standard deviation to generate the gaussian distribution to add the error //
        DoubleBag utilities = new DoubleBag();
        for(int i = 0; i < ScienceFunding.allLabs.size(); i++){ // get a bag of all the utilities to calculate standard deviation
            Lab aLab = (Lab) ScienceFunding.allLabs.get(i);
            utilities.add(aLab.getUtility());
        }

        double meanOfUtilities = 0;
        for(int i = 0; i < utilities.size(); i++) {
            meanOfUtilities += utilities.get(i);
        }
        meanOfUtilities = meanOfUtilities / utilities.size(); // mean of utilities

        double deviation = 0;
        for(int i = 0; i < utilities.size(); i++) { // sum of deviations from the mean
            deviation += deviation + (utilities.get(i) - meanOfUtilities);
        }
        deviation = deviation / utilities.size();
        double standardDeviation = Math.sqrt(deviation);

        // loop through labs changing their utilities to add a gaussian distributed error //
        for(int i = 0; i < ScienceFunding.allLabs.size(); i++) { // sum of deviations from the mean
            Lab aLab = (Lab) ScienceFunding.allLabs.get(i);
            aLab.setUtility(aLab.getUtility() + (state.random.nextGaussian() * standardDeviation)); // add a gaussian with standard deviation of standard deviation of utilities.
        }





            thisTurnsApplicants.sort(Comparator.comparing(Lab::getUtility)); // order labs according to their utility
        for(int i = 0; i < this.budget; i++) {
            if(thisTurnsApplicants.size() == 0){ // fail safe for if there are fewer applicants than there are grants
                break;
            }
            Lab bestLab = (Lab) thisTurnsApplicants.pop(); // get lab with highest utility
            if (state.random.nextDouble() < this.proportionOfBigGrants) { // win a big grant, yay! TODO all this is temporary.
                bestLab.grants.add(3); // add a postdoc for 3 years
            } else {
                bestLab.grants.add(1); // add a postdoc for 1 year
            }
        }

        thisTurnsApplicants = new Bag(); // clear the bag
    }
}
