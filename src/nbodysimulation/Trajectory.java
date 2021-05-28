package nbodysimulation;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.LinkedList;
import java.util.List;

public class Trajectory {
    private final int TRAJECTORY_LENGTH = 100;

    private List<PVector> trajectory = new LinkedList<>();

    public void draw(PApplet sketch) {
        for (int i = 1; i < trajectory.size(); i++) {
            PVector a = trajectory.get(i - 1);
            PVector b = trajectory.get(i);
            float grey = PApplet.map(i, 0, TRAJECTORY_LENGTH, 1, 255);
            sketch.stroke(grey);
            sketch.line(a.x, a.y, b.x, b.y);
        }
    }

    public void update(PVector position) {
        trajectory.add(position.copy());
        if (trajectory.size() > TRAJECTORY_LENGTH) {
            trajectory.remove(0);
        }
    }

    public void clear() {
        trajectory.clear();
    }
}
