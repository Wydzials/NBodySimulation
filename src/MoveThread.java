import processing.core.PVector;

import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class MoveThread extends Thread {

    private final float speed;
    private final float iterationsPerFrame;
    private final List<Body> bodies;
    private final int startIndex;
    private final int endIndex;

    public MoveThread(List<Body> bodies, float speed, float iterationsPerFrame, int startIndex, int endIndex) {
        this.bodies = bodies;
        this.speed = speed;
        this.iterationsPerFrame = iterationsPerFrame;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public void run() {
        for (int i = startIndex; i < endIndex; i++) {
            float t = speed / iterationsPerFrame;

            bodies.get(i).getVelocity().add(bodies.get(i).getAcceleration().copy().mult(t));
            PVector vt = bodies.get(i).getVelocity().copy().mult(t);

            bodies.get(i).getPosition().add(vt);

        }
    }
}
