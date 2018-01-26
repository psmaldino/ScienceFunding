import sim.display.*;
import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.gui.SimpleColorMap;

import javax.swing.*;
import java.awt.*;

public class ScienceFundingWithUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;
    ValueGridPortrayal2D landscapePortrayal = new ValueGridPortrayal2D();
    SparseGridPortrayal2D labsPortrayal = new SparseGridPortrayal2D();

    public static void main(String[] args) {
        ScienceFundingWithUI vid = new ScienceFundingWithUI();
        Console c = new Console(vid);
        c.setVisible(true);
    }

    public ScienceFundingWithUI() {
        super(new ScienceFunding(System.currentTimeMillis()));
    }

    public ScienceFundingWithUI(SimState state){
        super(state);
    }
    public static String getName(){
        return "Science Funding";
    }

    public void start(){
        super.start();
        setupPortrayals();
    }

    public void load(SimState state){
        super.load(state);
        setupPortrayals();
    }

    public void setupPortrayals(){
        ScienceFunding scienceFunding = (ScienceFunding) state;

        landscapePortrayal.setField(scienceFunding.landscape);
        labsPortrayal.setField(scienceFunding.labs);
        labsPortrayal.setPortrayalForAll(
                new LabelledPortrayal2D(
                        new OvalPortrayal2D(),
                1.0, null, Color.black, false));

        SimpleColorMap cm = new SimpleColorMap();
        cm.setLevels(0.001, 0.5, new Color(0,0,0,0), new Color(255,0,0,150));


        landscapePortrayal.setMap(cm);
        display.reset();
        display.setBackdrop(Color.white);
        display.repaint();
    }

    public void init(Controller c) {
        super.init(c);

        display = new Display2D(200, 200, this);
        display.setClipping(true);

        displayFrame = display.createFrame();

        displayFrame.setTitle("Test for science funding");
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);
        display.attach(landscapePortrayal, "Landscape");
        display.attach(labsPortrayal, "labs");
    }

        public void quit(){
            super.quit();
            if(displayFrame != null){
                displayFrame.dispose();
            }
            displayFrame = null;
            display = null;
        }
}
