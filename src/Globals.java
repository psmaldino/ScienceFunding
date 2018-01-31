import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;

import java.util.Arrays;

public class Globals implements Steppable { // Global agent that updates the stats
    static double falseDiscoveries = 0;
    static double numberOfPublications = 0;
    static double falseDiscoveryRate;
    static double rateOfDiscovery;
    static double discoveredMean;
    static double[] discoveredDistribution;
    static double discoveredStandardDev;
    static double publicationMean;
    static double publicationStandardDev;
    static int[] publicationDistribution;
    static double fundsMean;
    static double fundStandardDev;
    static double[] fundsDistribution;
    static double fundsGini;
    static double postdocNumberMean;
    static double[] postdocNumberDistribution;
    static double postdocNumberGini;
    static double postdocNumberStandardDev;
    static double postdocDurationMean;
    static double[] postdocDurationDistribution;
    static double postdocDurationStandardDev;
    static double postdocDurationGini;

    public void step(SimState state){
        updateGlobals();
    }

    private void updateGlobals(){
        // false discovery //

        falseDiscoveryRate = falseDiscoveries / numberOfPublications; // what's the rate of published articles that are false discoveries?


        // landscape discovery metrics //

        DoubleGrid2D theLandscape = ScienceFunding.landscape;
        double[] landscapeArray = theLandscape.toArray();

        discoveredDistribution = landscapeArray;
            // mean //
        discoveredMean = 0;
        for (double aLandscapeArray1 : landscapeArray) {
            discoveredMean += aLandscapeArray1;
        }
        discoveredMean = discoveredMean / landscapeArray.length;
            // standard dev //
        double sumOfSquaredDevs = 0;

        for (double aLandscapeArray : landscapeArray) {
            double squaredDev = aLandscapeArray - discoveredMean;
            squaredDev *= squaredDev;
            sumOfSquaredDevs += squaredDev;
        }
        sumOfSquaredDevs /= landscapeArray.length;

        discoveredStandardDev =  Math.sqrt(sumOfSquaredDevs);

        // publication metrics //

            // topic publication rate //
        IntGrid2D thePubs = ScienceFunding.publications;
        int[] pubsArray = thePubs.toArray();
        publicationDistribution = pubsArray;
        int exploredTopics = 0; // number of topics with more than 0 publications
        for (int aPubsArray1 : pubsArray) {
            if (aPubsArray1 > 0) {
                exploredTopics++;
            }
        }
        rateOfDiscovery = (double) exploredTopics / pubsArray.length; // rate of discovery: percentage of topics with more than publications.

            // mean //
        publicationMean = 0;
        for (int aPubsArray : pubsArray) {
            publicationMean += aPubsArray;
        }
        publicationMean /= pubsArray.length;

            // standard dev //
        sumOfSquaredDevs = 0;
        for (int aPubsArray : pubsArray) {
            double squaredDev = aPubsArray - publicationMean;
            squaredDev *= squaredDev;
            sumOfSquaredDevs += squaredDev;
        }
        sumOfSquaredDevs /= pubsArray.length;
        publicationStandardDev = Math.sqrt(sumOfSquaredDevs);

        // funds metrics //
        double[] fundsArray = new double[ScienceFunding.allLabs.size()]; // array of total number of years of funding a lab has
        double[] postdocNumberArray = new double[ScienceFunding.allLabs.size()]; // array of number of postdocs lab have
        double[] postdocDurationArray = new double[ScienceFunding.allLabs.size()]; // array of the number of years a lab will have at least one postdoc.
        for(int i = 0; i < ScienceFunding.allLabs.size(); i++){
            Lab aLab = (Lab) ScienceFunding.allLabs.get(i);
            double labTotalFunds = 0;
            int maxGrantSoFar = 0;
            for(int n = 0; n < aLab.grants.size(); n++){
                labTotalFunds += aLab.grants.get(n);
                if(aLab.grants.get(n) > maxGrantSoFar){
                    maxGrantSoFar = aLab.grants.get(n);
                }
            }
            fundsArray[i] = labTotalFunds;
            postdocNumberArray[i] = aLab.grants.size();
            postdocDurationArray[i] = maxGrantSoFar;

        }
            // mean and gini coefficient prep for funds //

        fundsDistribution = fundsArray;
        fundsMean = 0;
        Arrays.sort(fundsArray); // sort array in ascending order
        double giniNumerator = 0; // 2 * sum of i * Yi, i = 1, n = 100;
        double giniDenominator = 0; // n * sum of Yi, i = 1, n = 100;
        for(int i = 0; i < fundsArray.length;i++){
            fundsMean += fundsArray[i];
            giniNumerator += (i + 1) * fundsArray[i];
            giniDenominator += fundsArray[i];
        }
        fundsMean /= fundsArray.length; // mean number of years of funding for labs
        giniNumerator *= 2;
        giniDenominator *= fundsArray.length;

        sumOfSquaredDevs = 0;
        for (double aFundsArray : fundsArray) {
            double squaredDev = aFundsArray - fundsMean;
            squaredDev *= squaredDev;
            sumOfSquaredDevs += squaredDev;
        }
        sumOfSquaredDevs /= fundsArray.length;
        fundStandardDev = Math.sqrt(sumOfSquaredDevs);

            // gini coefficient //

        double giniLeftHand = giniNumerator / giniDenominator; // left hand of gini equation.
        fundsGini = giniLeftHand - ((fundsArray.length + 1) / fundsArray.length); // right hand of gini equation (1 + n) / n

        // postdoc metrics //

        postdocNumberDistribution = postdocNumberArray;
        postdocDurationDistribution = postdocDurationArray;

            // mean and gini of number //

        giniNumerator = 0;
        giniDenominator = 0;
        postdocNumberMean = 0;
        Arrays.sort(postdocNumberArray);
        for(int i = 0; i < postdocNumberArray.length; i++){
            postdocNumberMean += postdocNumberArray[i];
            giniNumerator += postdocNumberArray[i] * (i + 1);
            giniDenominator += postdocNumberArray[i];
        }
        postdocNumberMean /= postdocNumberArray.length;
        giniNumerator *= 2;
        giniDenominator *= postdocNumberArray.length;
        giniLeftHand = giniNumerator / giniDenominator;

        postdocNumberGini = giniLeftHand - ((1 + postdocNumberArray.length) / postdocNumberArray.length);

            // standard dev of number //

        sumOfSquaredDevs = 0;
        for (double aPostdocNumberArray : postdocNumberArray) {
            double squaredDev = aPostdocNumberArray - postdocNumberMean;
            squaredDev *= squaredDev;
            sumOfSquaredDevs += squaredDev;
        }
        sumOfSquaredDevs /= postdocNumberArray.length;
        postdocNumberStandardDev = Math.sqrt(sumOfSquaredDevs);

        // mean and gini of duration //

        giniNumerator = 0;
        giniDenominator = 0;
        postdocDurationMean = 0;
        Arrays.sort(postdocDurationArray);
        for(int i = 0; i < postdocDurationArray.length;i++){
            postdocDurationMean +=  postdocDurationArray[i];
            giniNumerator += postdocDurationArray[i] * (i + 1);
            giniDenominator += postdocDurationArray[i];
        }
        postdocDurationMean /= postdocDurationArray.length;
        giniNumerator *= 2;
        giniDenominator *= postdocDurationArray.length;
        giniLeftHand = giniNumerator / giniDenominator;

        postdocDurationGini = giniLeftHand - ((1 + postdocDurationArray.length) / postdocDurationArray.length);

            // standard dev of duration //

        sumOfSquaredDevs = 0;
        for (double aPostdocDurationArray : postdocDurationArray) {
            double squaredDev = aPostdocDurationArray - postdocDurationMean;
            squaredDev *= squaredDev;
            sumOfSquaredDevs += squaredDev;
        }
        sumOfSquaredDevs /= postdocDurationArray.length;
        postdocDurationStandardDev = Math.sqrt(sumOfSquaredDevs);
    }
}