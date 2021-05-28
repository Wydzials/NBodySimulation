package nbodysimulation;

import nbodysimulation.threads.CollisionThread;
import nbodysimulation.threads.MoveThread;
import nbodysimulation.threads.VelocityThread;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class Sketch extends PApplet {
    //Liczba wątków z liczbą ciał się nie zgadza
    private final float SOFTENING = 10;
    private final float G = 6.67f * pow(10, -11) * 1_000_000_000;

    private List<Body> bodies = new ArrayList<>();
    private Set<Character> pressedKeys = new HashSet<>();

    private float scale = 1;
    private float speed = 1;
    private float iterationsPerFrame = 1;
    private float posX = 0;
    private float posY = 0;
    private boolean pause = false;
    private final int numOfThread = 4;

    private boolean drawVelocities = false;
    private boolean drawAccelerations = false;
    private boolean drawTrajectories = false;

    public void settings() {
        size(1500, 1000);
    }

    public void setup() {
        frameRate(60);

        textSize(20);
        textFont(createFont("Ubuntu Mono", 20, true));

        BodyCreator creator = new BodyCreator();
        //creator.readFromFile("data/3-bodies.txt");
        creator.generateRandom(200);

        bodies.addAll(creator.getBodies());
    }


    public void draw() {
        handlePressedKeys();

        background(0);
        fill(220);

        drawText();

        translate(width / 2f, height / 2f);
        scale(scale);
        translate(posX, posY);

        strokeWeight(2 / scale);

        if (drawTrajectories) {
            bodies.forEach(body -> body.getTrajectory().draw(this));
            if (!pause && frameCount % 2 == 0) {
                bodies.forEach(Body::updateTrajectory);
            }
        }

        bodies.forEach(body -> body.draw(this));

        if (drawVelocities) {
            bodies.forEach(body -> body.drawVelocity(this));
        }

        if (drawAccelerations) {
            bodies.forEach(body -> body.drawAcceleration(this));
        }

        if (!pause) {
            for (int i = 0; i < iterationsPerFrame; i++) {
                int[] indexArr = createBodiesIndexArr(bodies.size()); // gdy nie ma kolizji można wyrzucić wyżej
                calculateVelocitiesThread(indexArr);
                moveBodies(indexArr);//Brak różnicy
                handleInelasticCollisionsThread(indexArr);
            }
        }
    }

    private void drawText() {
        float r = 100;
        int textX = 10;
        int[] textY = IntStream.range(0, 10)
                .map(i -> 25 + 20 * i)
                .toArray();

        text("Number of bodies: " + bodies.size(), width - 220, textY[0]);
        text("FPS: " + round(frameRate), width - 90, textY[1]);

        text("(<>) iterations per frame: " + round(iterationsPerFrame), textX, textY[0]);
        text("(1) show velocities: " + drawVelocities, textX, textY[1]);
        text("(2) show accelerations: " + drawAccelerations, textX, textY[2]);
        text("(3) show trajectories: " + drawTrajectories, textX, textY[3]);
        text("(space) paused: " + pause, textX, textY[4]);
        text("([]) speed: " + round(speed * r) / r, textX, textY[5]);
        text("(-+) scale: " + round(scale * r) / r, textX, textY[6]);
    }

    private int[] createBodiesIndexArr(int numOfBodies) {
        int[] indexArr = new int[numOfThread + 1];
        int div = numOfBodies / numOfThread;
        int rest = numOfBodies % numOfThread;
        for (int i = 1; i <= numOfThread; i++) {
            indexArr[i] = indexArr[i - 1] + div;
            if (i <= rest) {
                indexArr[i]++;
            }
        }
        return indexArr;
    }

    private void calculateVelocities(int[] indexArr) {
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

    private void calculateVelocitiesThread(int[] indexArr) {

        ArrayList<Thread> threadArr = new ArrayList<>();
        for (int i = 1; i <= numOfThread; i++) {
            Thread thread = new VelocityThread(bodies, G, SOFTENING, indexArr[i - 1], indexArr[i]);
            threadArr.add(thread);
            thread.start();
        }
        calculateVelocity(indexArr[numOfThread - 1], indexArr[numOfThread]);
        try {
            for (int i = 1; i <= numOfThread; i++) {
                threadArr.get(i - 1).join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calculateVelocity(int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            Body body = bodies.get(i);
            body.setAcceleration(new PVector(0, 0, 0));

            for (int j = 0; j < bodies.size(); j++) {
                Body secBody = bodies.get(j);
                if (i != j) {
                    PVector posA = body.getPosition().copy();
                    PVector posB = secBody.getPosition().copy();

                    float softenedR = sqrt(pow(posA.dist(posB), 2) + pow(SOFTENING, 2));
                    float force = G * secBody.getMass() / pow(softenedR, 3);

                    PVector acceleration = (posB.sub(posA)).mult(force);
                    body.getAcceleration().add(acceleration);
                }
            }
        }
    }

    private void moveBodies(int[] indexArr) {
        for (Body body : bodies) {
            float t = speed / iterationsPerFrame;

            body.getVelocity().add(body.getAcceleration().copy().mult(t));
            PVector vt = body.getVelocity().copy().mult(t);

            body.getPosition().add(vt);
        }
    }

    private void moveBodiesThread(int[] indexArr) {
        ArrayList<Thread> threadArr = new ArrayList<>();
        for (int i = 1; i < numOfThread; i++) {
            Thread thread = new MoveThread(bodies, speed, iterationsPerFrame, indexArr[i - 1], indexArr[i]);
            threadArr.add(thread);
            thread.start();
        }
        moveBody(indexArr[numOfThread - 1], indexArr[numOfThread]);
        try {
            for (int i = 1; i < numOfThread; i++) {
                threadArr.get(i - 1).join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void moveBody(int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            float t = speed / iterationsPerFrame;

            bodies.get(i).getVelocity().add(bodies.get(i).getAcceleration().copy().mult(t));
            PVector vt = bodies.get(i).getVelocity().copy().mult(t);

            bodies.get(i).getPosition().add(vt);

        }
    }


    private void handleInelasticCollisions(int[] indexArr) {
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

    private void handleInelasticCollisionsThread(int[] indexArr) { //Wyrzuca dziwny błąd
        ArrayList<Thread> threadArr = new ArrayList<>();
        for (int i = 1; i < numOfThread; i++) {
            Thread thread = new CollisionThread(bodies, indexArr[i - 1], indexArr[i]);
            threadArr.add(thread);
            thread.start();
        }
        handleColision(indexArr[numOfThread - 1], indexArr[numOfThread]);
        try {
            for (int i = 1; i < numOfThread; i++) {
                threadArr.get(i - 1).join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bodies.removeIf(Body::isRemoved);
    }


    private void handleColision(int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
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
    }

    private void handlePressedKeys() {
        if (pressedKeys.contains('=')) scale *= 1.02;
        if (pressedKeys.contains('-')) scale /= 1.02;

        if (pressedKeys.contains(']')) speed *= 1.01;
        if (pressedKeys.contains('[')) speed = max(speed /= 1.01f, 0.1f);

        if (pressedKeys.contains('.')) iterationsPerFrame += 1;
        if (pressedKeys.contains(',')) iterationsPerFrame = max(iterationsPerFrame - 1, 1);

        if (pressedKeys.contains('w')) posY += 10 / scale;
        if (pressedKeys.contains('s')) posY -= 10 / scale;
        if (pressedKeys.contains('a')) posX += 10 / scale;
        if (pressedKeys.contains('d')) posX -= 10 / scale;
    }

    public void keyPressed() {
        pressedKeys.add(key);
        if (key == ' ') pause = !pause;
        if (key == '1') drawVelocities = !drawVelocities;
        if (key == '2') drawAccelerations = !drawAccelerations;
        if (key == '3') {
            drawTrajectories = !drawTrajectories;
            bodies.forEach(body -> body.getTrajectory().clear());
        }
    }

    public void keyReleased() {
        pressedKeys.remove(key);
    }
}