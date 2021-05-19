import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class Sketch extends PApplet {

    private final float SOFTENING = 10;
    private final float G = 6.67f * pow(10, -11) * 1_000_000_000;

    private List<Body> bodies = new ArrayList<>();
    private Set<Character> pressedKeys = new HashSet<>();

    private float scale = 1;
    private float speed = 1;
    private float iterationsPerFrame = 1;
    private float posX = 0;
    private float posY = 0;

    public void settings() {
        size(1500, 1000);
    }

    public void setup() {
        frameRate(60);

        textSize(20);
        textFont(createFont("Ubuntu Mono", 20, true));

        BodyCreator creator = new BodyCreator();
        creator.generateRandom(20);

        //creator.readFromFile("data/bodies.txt");
        bodies.addAll(creator.getBodies());
    }


    public void draw() {
        handlePressedKeys();

        background(0);
        drawText();

        translate(width / 2f, height / 2f);
        scale(scale);
        translate(posX, posY);

        bodies.forEach(body -> body.draw(this, true));

        for (int i = 0; i < iterationsPerFrame; i++) {
            calculateVelocities();
            moveBodies();
            handleInelasticCollisions();
        }
    }

    private void drawText() {
        float r = 100;
        int textX = 10;
        int[] textY = IntStream.range(0, 10)
                .map(i -> 25 + 20 * i)
                .toArray();

        text("(<>)Iterations per frame: " + round(iterationsPerFrame), textX, textY[0]);
        text("Number of bodies: " + bodies.size(), textX, textY[1]);
        text("([]) Speed: " + round(speed * r) / r, textX, textY[2]);
        text("(-+) Scale: " + round(scale * r) / r, textX, textY[3]);
        text("FPS: " + round(frameRate), textX, textY[4]);
    }

    private void calculateVelocities() {
        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            body.setAcceleration(new PVector(0, 0, 0));

            for (int j = 0; j < bodies.size(); j++) {
                Body secBody = bodies.get(j);
                if (i != j) {
                    PVector posA = body.getPosition().copy();
                    PVector posB = secBody.getPosition().copy();

                    float softenedR = sqrt(pow(posA.dist(posB), 2) + pow(SOFTENING, 2));
                    float force = G * secBody.getMass() / (float) Math.pow(softenedR, 3);

                    PVector acceleration = (posB.sub(posA)).mult(force);
                    body.getAcceleration().add(acceleration);
                }
            }
        }
    }

    private void moveBodies() {
        for (Body body : bodies) {
            float t = speed / iterationsPerFrame;

            body.getVelocity().add(body.getAcceleration().copy().mult(t));
            PVector vt = body.getVelocity().copy().mult(t);

            body.getPosition().add(vt);
        }
    }

    private void handleInelasticCollisions() {
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                Body a = bodies.get(i);
                Body b = bodies.get(j);

                if (a.isRemoved() || b.isRemoved()) {
                    continue;
                }

                if (bodies.get(i).getMass() < bodies.get(j).getMass()) {
                    a = bodies.get(j);
                    b = bodies.get(i);
                }

                if (a.getPosition().dist(b.getPosition()) < (a.getRadius() + b.getRadius() / 4)) {
                    PVector momentumA = a.getVelocity().mult(a.getMass());
                    PVector momentumB = b.getVelocity().mult(b.getMass());

                    PVector newVelocity = (momentumA.add(momentumB)).div(a.getMass() + b.getMass());
                    a.setVelocity(newVelocity);

                    a.setMass(a.getMass() + b.getMass());
                    b.setRemoved(true);
                }
            }
        }
        bodies.removeIf(Body::isRemoved);
    }

    private void handlePressedKeys() {
        if (pressedKeys.contains('=')) scale *= 1.02;
        if (pressedKeys.contains('-')) scale /= 1.02;

        if (pressedKeys.contains(']')) speed += 0.1;
        if (pressedKeys.contains('[')) speed = max(speed - 0.1f, 0.1f);

        if (pressedKeys.contains('.')) iterationsPerFrame += 1;
        if (pressedKeys.contains(',')) iterationsPerFrame = max(iterationsPerFrame - 1, 1);

        if (pressedKeys.contains('w')) posY += 10 / scale;
        if (pressedKeys.contains('s')) posY -= 10 / scale;
        if (pressedKeys.contains('a')) posX += 10 / scale;
        if (pressedKeys.contains('d')) posX -= 10 / scale;
    }

    public void keyPressed() {
        pressedKeys.add(key);
    }

    public void keyReleased() {
        pressedKeys.remove(key);
    }
}
