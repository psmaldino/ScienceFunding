import sim.field.grid.DoubleGrid2D;
import sim.util.*;
import static java.lang.Math.pow;

class LandscapeUtils {
    // implements static methods to manipulate the landscape //

    static void increaseAndDisperse(DoubleGrid2D landscape, int originalCellX, int originalCellY, double amount){ // includes dispersal. takes cell, adds amount, and then adds dispersing amount to neighboring cells until it completely disperses (increase <= 0.001
        Double originalValue = landscape.get(originalCellX, originalCellY);
        Double2D originalCell = new Double2D(originalCellX, originalCellY);
        landscape.set(originalCellX, originalCellY, (originalValue + amount));
        Bag previousChanges = new Bag();
        previousChanges.add(originalCell);
        changeNeighbors(landscape, originalCell, originalCell, amount, previousChanges);

    }

    static void changeNeighbors(DoubleGrid2D landscape, Double2D originalCell, Double2D thisCell, double originalAmount, Bag previousChanges) {
        // recursive function. takes the landscape, the cell originally changed and the cell to change along with a Bag to avoid stack overflow and the original amount changed.
        // it loops through the neighbors of a cell and makes them change their value according to the distance to the original cell.
        // each of those neighbors calls the same function on their neighbors, until the value is equal to 0.001 (min value).
        // after the function is called on some value, it is added to the bag "previousChanges". the function is only called on cells not previously changed. this avoids infinite recursion.

        IntBag neighborsX = new IntBag(); // allocate the bags needed by getMooreNeighbors
        IntBag neighborsY = new IntBag();
        DoubleBag neighborsValues = new DoubleBag();

        landscape.getMooreNeighbors((int) thisCell.x, (int) thisCell.y, 1, 0, false, neighborsValues, neighborsX, neighborsY);
        for (int i = 0; i < neighborsX.size(); i++) {
            Double2D thisNeighbor = new Double2D(neighborsX.get(i), neighborsY.get(i));
            double thisNeighborValue = landscape.get((int) thisNeighbor.x, (int) thisNeighbor.y);
            if ((thisNeighbor.x == originalCell.x) && (thisNeighbor.y == originalCell.y)) { // if this neighbor is the original cell
                continue;
            }
            if (previousChanges.contains(thisNeighbor)) { // if the cell was changed before
                continue;
            }
            Double newValue = getValueWithDispersal(landscape, originalCell, thisNeighbor, originalAmount); // get the new value for the cell
            if (newValue != thisNeighborValue) { // if it changed above precision level
                landscape.set(neighborsX.get(i), neighborsY.get(i), newValue); // change the value in the landscape
                previousChanges.add(thisNeighbor); // add the cell to the previously changed cells to avoid infinite recursion
                changeNeighbors(landscape, originalCell, thisNeighbor, originalAmount, previousChanges); // recursively call function
            } else { // if it didn't change because change was over precision level
                landscape.set(neighborsX.get(i), neighborsY.get(i), thisNeighborValue); // set the same value;
            }
        }
    }

    static double getValueWithDispersal(DoubleGrid2D landscape, Double2D originalCell, Double2D thisCell, double originalAmount) { // returns amount to be added after dispersion. only for initial allocation

        double oldValue = landscape.get((int) thisCell.x, (int) thisCell.y);
        double eucDistance = originalCell.distance(thisCell); //euclidean distance
        double newValue = pow(originalAmount, eucDistance); // original amount to the power of the euclidean distance
        if(newValue >= 0.00000001){ // maximum precision to avoid recursion
            newValue += oldValue;
        } else {
            newValue = oldValue;
        }
        if(newValue >= 0.5){
            return 0.5;
        } else{return newValue;}
    }
}
