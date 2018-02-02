import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.Arrays;

class Globals implements Steppable { // Global agent that updates the stats
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

    public void step(SimState state) {
        updateGlobals();
    }

    private void updateGlobals() {
        // false discovery rate //

        falseDiscoveryRate = falseDiscoveries / numberOfPublications; // what's the rate of published articles that are false discoveries?

        // landscape discovery mean, sdev //

        double[] landscapeArray = ScienceFunding.landscape.toArray();
        discoveredDistribution = landscapeArray;
        discoveredMean = calculateMean(landscapeArray);
        discoveredStandardDev = calculateStandardDev(landscapeArray, discoveredMean);

        // publication metrics //
        // topic publication rate //

        int[] pubsArray = ScienceFunding.publications.toArray();
        publicationDistribution = pubsArray;
        int exploredTopics = 0; // number of topics with more than 0 publications
        for (int aPubsArray1 : pubsArray) {
            if (aPubsArray1 > 0) {
                exploredTopics++;
            }
        }
        rateOfDiscovery = (double) exploredTopics / pubsArray.length; // rate of discovery: proportion of topics with more than publications.

        // mean and s //

        publicationMean = calculateMean(pubsArray);
        publicationStandardDev = calculateStandardDev(pubsArray, publicationMean);

        // funds metrics //

        double[] fundsArray = new double[ScienceFunding.allLabs.size()]; // array of total number of years of funding a lab has
        double[] postdocNumberArray = new double[ScienceFunding.allLabs.size()]; // array of number of postdocs lab have
        double[] postdocDurationArray = new double[ScienceFunding.allLabs.size()]; // array of the number of years a lab will have at least one postdoc.

        for (int i = 0; i < ScienceFunding.allLabs.size(); i++) { // populate the arrays
            Lab aLab = (Lab) ScienceFunding.allLabs.get(i);
            double labTotalFunds = 0;
            int maxGrantSoFar = 0;
            for (int n = 0; n < aLab.grants.size(); n++) {
                labTotalFunds += aLab.grants.get(n);
                if (aLab.grants.get(n) > maxGrantSoFar) {
                    maxGrantSoFar = aLab.grants.get(n);
                }
            }
            fundsArray[i] = labTotalFunds;
            postdocNumberArray[i] = aLab.grants.size();
            postdocDurationArray[i] = maxGrantSoFar;

        }

        // mean, gini, sdev //

        fundsDistribution = fundsArray;
        double[] fundsResults = meanAndGini(fundsArray);
        fundsMean = fundsResults[0];
        fundsGini = fundsResults[1];
        fundStandardDev = calculateStandardDev(fundsArray, fundsMean);

        // postdoc metrics //

        postdocNumberDistribution = postdocNumberArray; // distributions populate
        postdocDurationDistribution = postdocDurationArray;

        // number of postdocs mean, gini, sdev //

        double[] postdocNumberResults = meanAndGini(postdocNumberArray);
        postdocNumberMean = postdocNumberResults[0];
        postdocNumberGini = postdocNumberResults[1];
        postdocNumberStandardDev = calculateStandardDev(postdocNumberArray, postdocNumberMean);

        // number of years you will have at least 1 postdoc mean, gini, sdev//

        double[] postdocDurationResults = meanAndGini(postdocDurationArray);
        postdocDurationMean = postdocDurationResults[0];
        postdocDurationGini = postdocDurationResults[1];
        postdocDurationStandardDev = calculateStandardDev(postdocDurationArray, postdocDurationMean);
    }

    private double calculateMean(double[] array) {
        double mean = 0;
        for (double aDouble : array) {
            mean += aDouble;
        }
        return mean / array.length;
    }

    private double calculateMean(int[] array) {
        double mean = 0;
        for (int anInt : array) {
            mean += (double) anInt;
        }
        return mean / array.length;
    }

    private double[] meanAndGini(double[] array) { // [0] is mean, [1] is gini coef
        double giniNumerator = 0;
        double giniDenominator = 0;
        double mean = 0;
        Arrays.sort(array);
        for (int i = 0; i < array.length; i++) {
            mean += array[i];
            giniNumerator += array[i] * (i + 1);
            giniDenominator += array[i];
        }
        mean /= array.length;
        giniNumerator *= 2;
        giniDenominator *= array.length;
        double giniLeftHand = giniNumerator / giniDenominator;

        double giniIndex = giniLeftHand - ((1 + array.length) / array.length);
        return new double[]{mean, giniIndex};
    }

    private double calculateStandardDev(double[] array, double mean) {
        double sumOfSquaredDevs = 0;
        for (double aDouble : array) {
            double squaredDev = aDouble - mean;
            squaredDev *= squaredDev;
            sumOfSquaredDevs += squaredDev;
        }
        sumOfSquaredDevs /= array.length;
        return Math.sqrt(sumOfSquaredDevs);
    }

    private double calculateStandardDev(int[] array, double mean) {
        double sumOfSquaredDevs = 0;
        for (int anInt : array) {
            double squaredDev = ((double) anInt) - mean;
            squaredDev *= squaredDev;
            sumOfSquaredDevs += squaredDev;
        }
        sumOfSquaredDevs /= array.length;
        return Math.sqrt(sumOfSquaredDevs);
    }
}