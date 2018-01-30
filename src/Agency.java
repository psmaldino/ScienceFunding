import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.DoubleBag;

import java.util.Comparator;

public class Agency implements Steppable {
    static Bag thisTurnsApplicants; // renewable bag to store everyone applying for funds. labs insert themselves into the bags during their steps.
    public int budget = 5; // budget available measured in big grants. 1 big grant (5 years) = 4 small grants (1 year)
    public double proportionOfBigGrants = 0.5; // proportion of total grants awarded to big grants.
    public double evaluationError = 0.001; // standard deviation of error in utility.

    public Agency(){
        thisTurnsApplicants = new Bag(); // initialize bag along with funding agency
    }

    public void step(SimState state){
        // add error to utilities of applicants //

        // loop through labs changing their utilities to add a gaussian distributed error //
        for(int i = 0; i < thisTurnsApplicants.size(); i++) { // sum of deviations from the mean
            Lab aLab = (Lab) thisTurnsApplicants.get(i);
            aLab.setUtility(aLab.getUtility() + (state.random.nextGaussian() * 0.001)); // add a gaussian with standard deviation of 1% of maximum value.
        }

        // distribute funds based on utility //

        thisTurnsApplicants.sort(Comparator.comparing(Lab::getUtility)); // order labs according to their utility

        for(int i = 0; i < this.budget; i++) {
            if(thisTurnsApplicants.size() == 0){ // fail safe for if there are fewer applicants than there are grants
                break;
            }
            if (state.random.nextDouble() < this.proportionOfBigGrants) { // win a big grant, yay! Only one lab gets 5 years of funding.
                Lab bestLab = (Lab) thisTurnsApplicants.pop(); // get lab with highest utility
                bestLab.grants.add(5); // add a postdoc for 5 years
            } else { // 4 small grants are given. 4 labs get 1 year of funding
                for(int n = 0; n < 4; i++) {
                    if(thisTurnsApplicants.size() == 0){ // fail safe for if there are fewer applicants than there are grants
                        break;
                    }
                    Lab bestLab = (Lab) thisTurnsApplicants.pop(); // get lab with highest utility
                    bestLab.grants.add(1); // add a postdoc for 1 year
                }
            }
        }

        thisTurnsApplicants = new Bag(); // clear the bag
    }
}
