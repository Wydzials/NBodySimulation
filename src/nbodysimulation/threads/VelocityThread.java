package nbodysimulation.threads;

import nbodysimulation.Body;
import processing.core.PVector;

import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class VelocityThread extends Thread {

    private final float G;
    private final float SOFTENING;
    private final List<Body> bodies;
    private final int startIndex;
    private final int endIndex;

    public VelocityThread(List<Body> bodies, float G, float SOFTENING, int startIndex, int endIndex) {
        this.bodies = bodies;
        this.G = G;
        this.SOFTENING = SOFTENING;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public void run() {
        for (int i = startIndex; i < endIndex; i++) {
            Body body = bodies.get(i);
            body.setAcceleration(new PVector(0, 0, 0));

            for (int j = 0; j < bodies.size(); j++) {
                Body secBody = bodies.get(j);
                if (i != j) {
                    PVector posA = body.getPosition().copy();
                    PVector posB = secBody.getPosition().copy();

                    float softenedR = (float) sqrt(pow(posA.dist(posB), 2) + pow(SOFTENING, 2));
                    float force = G * secBody.getMass() / (float) pow(softenedR, 3);

                    PVector acceleration = (posB.sub(posA)).mult(force);
                    body.getAcceleration().add(acceleration);
                }
            }
        }
    }
}
