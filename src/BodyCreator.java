import processing.core.PApplet;
import processing.core.PVector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BodyCreator {

    private List<Body> bodies = new ArrayList<>();
    private PApplet sketch = new PApplet();

    public List<Body> getBodies() {
        return bodies;
    }

    public void readFromFile(String path) {
        try {
            List<String[]> bodyLines = Files.lines(Path.of(path))
                    .filter(s -> !s.startsWith("#"))
                    .map(s -> s.split(" "))
                    .filter(strings -> strings.length == 7 || strings.length == 4)
                    .collect(Collectors.toList());

            for (String[] bodyLine : bodyLines) {
                List<Float> parsed = Arrays.stream(bodyLine)
                        .map(Float::parseFloat)
                        .collect(Collectors.toList());

                PVector position = new PVector(parsed.get(0), parsed.get(1), parsed.get(2));
                PVector velocity;
                float mass;

                if (parsed.size() == 7) {
                    velocity = new PVector(parsed.get(3), parsed.get(4), parsed.get(5));
                    mass = parsed.get(6);
                } else {
                    velocity = new PVector();
                    mass = parsed.get(3);
                }

                bodies.add(new Body(position, velocity, mass));
            }
        } catch (IOException e) {
            System.out.println("Error while reading file: " + e.getMessage());
        }
    }

    public void generateRandom(int n) {
        for (int i = 0; i < n; i++) {
            PVector position = PVector.random2D().mult(sketch.random(10, 1500));
            PVector velocity = PVector.random2D().mult(sketch.random(5));
            System.out.println(position + " " + velocity);
            bodies.add(new Body(position, velocity, sketch.random(50, 1000)));
        }
    }
}
