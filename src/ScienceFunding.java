import sim.engine.*;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.*;
import sim.util.*;

public class ScienceFunding extends SimState {
    static double powerLevel = 0.8; // W in the document. the statistical power.
    static int sizeOfLandscape = 200; // width and height of square landscape.
    public int numberOfLabs = 100; // how many labs?
    public int numberOfEstablishedTopics = 5; //
    static Bag allLabs; // just in case, bag with all labs
    static int latestId; // track labs to assign ids
    static double effortConstant = 0.2; // constant that determines how much effort reduces research output;
    static int probabilityOfReplication = 33; // how often will research be a publication? in %
    static int probabilityOfPublishingNegative = 25; // how often journals publish negative results

    static double costOfApplication = 0.2; // how much you are affected by applying to a grant.

    static double weightOfInnovation = 0.5; // -1 to 1
    static double weightOfRecord = 0.5; // 0 to 1. must sum more than 0


    static DoubleGrid2D landscape = new DoubleGrid2D(sizeOfLandscape, sizeOfLandscape, 0.001); // initialize underlying landscape
    static SparseGrid2D labs = new SparseGrid2D(sizeOfLandscape, sizeOfLandscape); // initialize movement of labs
    static IntGrid2D publications = new IntGrid2D(sizeOfLandscape, sizeOfLandscape, 0); // initialize grid of number of publications

    public ScienceFunding(long seed){
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
            int myXNearTopic = random.nextInt(5); // how much will I change the position from established topic?
            if(random.nextBoolean()) {// with a random chance, make it negative
                myXNearTopic = -1 * myXNearTopic;
            }
            int myYNearTopic = random.nextInt(5); // how much will I change the position from established topic?
            if(random.nextBoolean()) {// with a random chance, make it negative
                myYNearTopic = -1 * myYNearTopic;
            }
            int myX = (int) myTopic.x + myXNearTopic;
            if(myX >= 200){ // ceiling at 200
                myX = 199;
            }
            if(myX < 0){// floor at 0
                myX = 0;
            }
            int myY = (int) myTopic.y + myYNearTopic;
            if(myY >= 200){
                myY = 199;
            }
            if (myY < 0){
                myY = 0;
            }
            Lab thisLab = new Lab(i, myX, myY);
            latestId = i;
            if(random.nextInt(100) >= 95){// 5% chance of having a postdoc from beginning.
                thisLab.numberOfPostdocs += 1;
            }
            thisLab.effort = 75;
            allLabs.add(thisLab);
            labs.setObjectLocation(thisLab, myX, myY);
            thisLab.stoppable = schedule.scheduleRepeating(thisLab, 1,1);
        }

        ScienceMaster theMaster = new ScienceMaster();
        schedule.scheduleRepeating(theMaster, 0, 1);
    }


    public static void main(String[] args){
        {
            doLoop(ScienceFunding.class, args);
            System.exit(0);
        }
    }
}
