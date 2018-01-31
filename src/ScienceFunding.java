import sim.engine.*;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.*;
import sim.util.*;

public class ScienceFunding extends SimState {
    static double powerLevel = 0.8; // W in the document. the statistical power.
    static int sizeOfLandscape = 200; // width and height of square landscape.
    private int numberOfLabs = 100; // how many labs?
    private int numberOfEstablishedTopics = 5; //
    static Bag allLabs; // just in case, bag with all labs
    static int latestId; // track labs to assign ids
    static double effortConstant = 0.2; // constant that determines how much effort reduces research output;
    static double probabilityOfReplication = 0.3; // how often will research be a publication? in %
    static int probabilityOfPublishingNegative = 25; // how often journals publish negative results

    static double costOfApplication = 0.2; // how much you are affected by applying to a grant.

    static double weightOfInnovation = 0.5; // -1 to 1
    static double weightOfRecord = 0.5; // 0 to 1. this + weight of innovation must sum more than 0

    // mutation parameters //

    static double probabilityOfMutationEffort = 0.01;
    static boolean mutateFunding = false; // does the probability of applying for funding evolve?
    static double probabilityOfMutationFunding = 0.01;


    static DoubleGrid2D landscape = new DoubleGrid2D(sizeOfLandscape, sizeOfLandscape, 0.001); // initialize underlying landscape
    static SparseGrid2D labs = new SparseGrid2D(sizeOfLandscape, sizeOfLandscape); // initialize movement of labs
    static IntGrid2D publications = new IntGrid2D(sizeOfLandscape, sizeOfLandscape, 0); // initialize grid of number of publications

    ScienceFunding(long seed){
        super(seed);
    }

    public void start(){
        super.start();
        labs.clear(); // clear labs location

        Bag establishedTopics = new Bag(); // allocate established topics so they don't repeat by chance.
        allLabs = new Bag(); // initialize bag of labs

        for(int i = 0; i < numberOfEstablishedTopics; i++){ // define and allocate established topics
            Double2D establishedTopic;
            int xValue;
            int yValue;
            do {
                xValue = random.nextInt(200);
                yValue = random.nextInt(200);
                establishedTopic = new Double2D(xValue, yValue);
            } while(establishedTopics.contains(establishedTopic));
            establishedTopics.add(establishedTopic);
            //test//
            publications.set(xValue, yValue, 5); // add initial number of publications to established topic. because we know it's zero, just set it. but have to add get method for summing publication.
            // test //
            LandscapeUtils.increaseAndDisperse(landscape, (int) establishedTopic.x, (int) establishedTopic.y, 0.499);
        }

        for(int i = 0; i <  numberOfLabs; i++){ // allocate labs near established topics
            Double2D myTopic = (Double2D) establishedTopics.get(random.nextInt(establishedTopics.size()));
            int myX;
            int myY;
            do{
                int myXNearTopic = random.nextInt(3); // how much will I change the position from established topic?
                int myYNearTopic = random.nextInt(3); // how much will I change the position from established topic?
                if(random.nextBoolean()) { // with a random chance, make it negative
                    myXNearTopic = -1 * myXNearTopic; }
                if(random.nextBoolean()) { // with a random chance, make it negative
                    myYNearTopic = -1 * myYNearTopic; }
                myX = (int) myTopic.x + myXNearTopic;
                myY = (int) myTopic.y + myYNearTopic;
            } while ((myX >= 200 || myX < 0) || (myY >= 200 || myY < 0));

            Lab thisLab = new Lab(i, myX, myY);
            latestId = i;
            if(random.nextDouble() < 0.05){// 5% chance of having a postdoc from beginning.
                thisLab.numberOfPostdocs += 1;
            }
            thisLab.effort = 75;
            allLabs.add(thisLab);
            labs.setObjectLocation(thisLab, myX, myY);
            thisLab.stoppable = schedule.scheduleRepeating(thisLab, 1,1); // schedule the labs after the master
        }

        ScienceMaster theMaster = new ScienceMaster(); // schedule master
        schedule.scheduleRepeating(theMaster, 0, 1);

        Agency theAgency = new Agency();  // initialize and schedule the funding agency after labs.
        schedule.scheduleRepeating(theAgency, 2, 1);

        Globals theGlobals = new Globals();
        schedule.scheduleRepeating(theGlobals, 3, 1); // schedule the global updater after everything else. updates every X.

        LevyWalk.initialize(); // initialize
    }

    public static void main(String[] args){
        {
            doLoop(ScienceFunding.class, args);
            System.exit(0);
        }
    }

    public double getFalseDiscoveryRate(){
        return Globals.falseDiscoveryRate;
    }

    public double getRateOfDiscoveries(){
        return Globals.rateOfDiscovery;
    }

    public double getDiscoveredMean() {
        return Globals.discoveredMean;
    }

    public double[] getDiscoveredDistribution(){
        return Globals.discoveredDistribution;
    }

    public double getDiscoveredStandardDev(){
        return Globals.discoveredStandardDev;
    }

    public double getPublicationMean(){
        return Globals.publicationMean;
    }

    public int[] getPublicationDistribution(){
        return Globals.publicationDistribution;
    }

    public double getPublicationStandardDev(){
        return Globals.publicationStandardDev;
    }

    public double getFundsMean(){
        return Globals.fundsMean;
    }

    public double getFundStandardDev(){
        return Globals.fundStandardDev;
    }

    public double[] getFundsDistribution(){
        return Globals.fundsDistribution;
    }

    public double getFundsGini(){
        return Globals.fundsGini;
    }

    public double getPostdocNumberMean(){
        return Globals.postdocNumberMean;
    }

    public double[] getPostdocNumberDistribution(){
        return Globals.postdocNumberDistribution;
    }

    public double getPostdocNumberGini(){
        return Globals.postdocNumberGini;
    }

    public double getPostdocNumberStandardDev(){
        return Globals.postdocNumberStandardDev;
    }

    public double getPostdocDurationMean(){
        return Globals.postdocDurationMean;
    }

    public double[] getPostdocDurationDistribution(){
        return Globals.postdocDurationDistribution;
    }

    public double getPostdocDurationStandardDev(){
        return Globals.postdocDurationStandardDev;
    }

    public double getPostdocDurationGini(){
        return Globals.postdocDurationGini;
    }
}
