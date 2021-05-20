import processing.core.PApplet;
import processing.core.PVector;

import java.awt.*;

public class Body {

    private PVector position;
    private PVector velocity;
    private PVector acceleration;
    private float mass;

    private boolean removed = false;
    private Trajectory trajectory = new Trajectory();

    public Body(PVector position, PVector velocity, float mass) {
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
        acceleration = new PVector();
    }

    public void draw(Sketch sketch) {
        sketch.fill(221, 224, 18);
        sketch.stroke(221, 224, 18);
        sketch.circle(position.x, position.y, getRadius() * 2);
    }

    public void updateTrajectory() {
        trajectory.update(position);
    }

    public Trajectory getTrajectory() {
        return trajectory;
    }

    public void drawVelocity(PApplet sketch) {
        drawVector(velocity, sketch, Color.BLUE, 200);
    }

    public void drawAcceleration(PApplet sketch) {
        drawVector(acceleration, sketch, Color.RED, 3000);
    }

    private void drawVector(PVector vector, PApplet sketch, Color color, int multiplier) {
        float limit = PApplet.sqrt(vector.mag());
        vector = vector.copy()
                .limit(limit)
                .mult(multiplier);

        float x2 = (position.x + vector.x);
        float y2 = (position.y + vector.y);

        sketch.stroke(color.getRed(), color.getGreen(), color.getBlue());
        sketch.line(position.x, position.y, x2, y2);

        PVector arrowRight = new PVector(x2 - position.x, y2 - position.y);
        arrowRight.normalize().mult(8).rotate(2.5f);
        PVector arrowLeft = arrowRight.copy().rotate(-5f);

        sketch.line(x2, y2, x2 + arrowRight.x, y2 + arrowRight.y);
        sketch.line(x2, y2, x2 + arrowLeft.x, y2 + arrowLeft.y);
    }

    public PVector getPosition() {
        return position;
    }

    public void setPosition(PVector position) {
        this.position = position;
    }

    public PVector getVelocity() {
        return velocity;
    }

    public void setVelocity(PVector velocity) {
        this.velocity = velocity;
    }

    public PVector getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(PVector acceleration) {
        this.acceleration = acceleration;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public float getRadius() {
        return 4 * PApplet.sqrt(mass / 10 / PApplet.PI);
    }
}
