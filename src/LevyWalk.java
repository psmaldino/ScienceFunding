import sim.engine.SimState;
import sim.util.*;

/**
 * This class implements Levy walks based on the probability function p(l) = l^(-u), where l is the
 * number of steps in the same direction and u ³ 1 (if u ² 1, p(l) > 1 for l > 0).  A maximum length,
 * l, is set and then p(l) is normalized to sum to 1 for a given maximum length.  More to come.
 * 
 * @author jcschank
 *
 */

class LevyWalk {
	static final int maxLength = 10; //longest walk permitted
	static double[] lengthProbabilities; //A vector of the probabilities for each length of walk in a given
								  //direction. It is a summation vector so that random.nextDouble()
								  //can be used to fined a random length.
	static final double u = 2.5; // Exponent for the probability equation above. Assumed positive.
	
	@SuppressWarnings("ManualArrayCopy")
    static void calculateSumProbVector(){
		lengthProbabilities = new double[maxLength];
		double total = 0;
		for(int i=0; i<maxLength;i++){
			lengthProbabilities[i]=Math.pow((double)(i+1),-u); //where i+1 is the length, which is
			                         // a minimum of 1 step
			total+=lengthProbabilities[i];
		}
		if(total <= 0){
			System.err.println("total = 0");
			System.exit(0); // the program quits
		}
		for(int i=0; i<maxLength;i++){
			lengthProbabilities[i]=lengthProbabilities[i]/total; //normalize
		}
		double[] working = new double[maxLength];
		
		for(int i=0; i<maxLength;i++){
			working[i]=0;
		}
		
		working[0]=lengthProbabilities[0];
		for(int i=1; i<maxLength;i++){
			working[i]=lengthProbabilities[i]+working[i-1];
		}


		for(int i=0; i<maxLength;i++){
			lengthProbabilities[i]=working[i]; //reload the summed normalized vector
		}
		lengthProbabilities[maxLength-1]=1; //make sure there are no rounding errors
	}
	
	static void initialize(){
		calculateSumProbVector();
	}
	
	/**
	 * This method returns a random walk length.  It can be used in two ways.  It can be interpreted
	 * as the number of steps an agent takes in a given direction or, if the walk occurs in one step,
	 * it is the distance moved in a given direction.  The former interpretation is best for most
	 * agent-based simulations.  Note also that this can be used in conjunction with OneStepRandomWalk
	 * so that one is not stuck with only Brownian motion random walks.  Thus, there can be, for
	 * example, zigzag Levy walks.
	 */

	static int getLength(SimState state){
		final double r = state.random.nextDouble();
		for(int i=0; i< maxLength; i++){
			double x = lengthProbabilities[i];
			if(r<=x){
				return i+1; //the length of the walk;
			}
		}
		System.err.println("Error in getLength: maxLength returned.");
		return maxLength;
	}


	// this part was coded by Pablo Contreras //

	static Double2D getNewLocation(Double2D currentLocation, SimState state){
	    int direction = state.random.nextInt(8); // get a random direction. 0 : up, 1: top-right, clockwise until 7: top-left.
        int oldX = (int) currentLocation.x;
        int oldY = (int) currentLocation.y;

        int newX = oldX;
        int newY = oldY;

        if(direction == 0 || direction == 1 || direction == 7){ // cases where it moves upwards
            int stepLength = getLength(state);
            newY = oldY + stepLength;
            if(newY >= 200){newY = 199;} // capped at 200

        }

        if(direction == 3 || direction == 4 || direction == 5){ // cases where it moves downwards
            int stepLength = getLength(state);
            newY = oldY - stepLength;
            if(newY < 0){newY = 0;}
        }

        if(direction == 1 || direction == 2 || direction == 3){ // cases where you move right
            int stepLength = getLength(state);
            newX = oldX + stepLength;
            if(newX >= 200){newX = 199;}
        }

        if(direction == 5 || direction == 6 || direction == 7){ // cases where you move left
            int stepLength = getLength(state);
            newX = oldX - stepLength;
            if(newX < 0){newX = 0;}
        }

        return new Double2D(newX, newY);
	}

}
