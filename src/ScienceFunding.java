import sim.engine.*;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.*;
import sim.util.*;

public class ScienceFunding extends SimState {
    public double powerLevel = 0.8; // W in the document. the statistical power.
    static int sizeOfLandscape = 200; // width and height of square landscape.
    public int numberOfLabs = 100; // how many labs?
    public int numberOfEstablishedTopics = 5; //
    static Bag allLabs; // just in case, bag with all labs
    static int latestId; // track labs to assign ids


    static DoubleGrid2D landscape = new DoubleGrid2D(sizeOfLandscape, sizeOfLandscape); // initialize underlying landscape
    static SparseGrid2D labs = new SparseGrid2D(sizeOfLandscape, sizeOfLandscape); // initialize movement of labs

    public ScienceFunding(long seed){
        super(seed);
    }

    public void start(){
        super.start();
        labs.clear(); // clear labs location
        landscape.setTo(0.001); // initialize landscape to minimum rate of 0.001

        Bag establishedTopics = new Bag(); // allocate established topics so they don't repeat by chance.
        allLabs = new Bag(); // initialize bag of labs

        for(int i = 0; i < numberOfEstablishedTopics; i++){ // define and allocate established topics
            Double2D establishedTopic;
            do {
                int xValue = random.nextInt(200);
                int yValue = random.nextInt(200);
                establishedTopic = new Double2D(xValue, yValue);
            } while(establishedTopics.contains(establishedTopic));
            establishedTopics.add(establishedTopic);
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
