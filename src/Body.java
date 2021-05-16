import processing.core.PApplet;
import processing.core.PVector;

public class Body {
    private PVector position;
    private PVector velocity;
    private PVector acceleration;
    private float mass;

    private boolean removed = false;

    public Body(PVector position, PVector velocity, float mass) {
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
        acceleration = new PVector();
    }

    public void draw(Sketch sketch) {
        sketch.fill(180);
        sketch.stroke(180);
        sketch.circle(position.x, position.y, getRadius()*2);
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
        return 4 * PApplet.sqrt(mass / PApplet.PI);
    }
}
