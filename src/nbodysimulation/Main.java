package nbodysimulation;

import processing.core.PApplet;

public class Main {

    public static void main(String[] args) {
        String[] processingArgs = {"N Body Simulation"};
        Sketch sketch = new Sketch();
        PApplet.runSketch(processingArgs, sketch);
    }

}
