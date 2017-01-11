package smartMath;

/**
 * Created by shininisan on 11/01/17.
 */
public class PartialCircle extends Circle {
    private double angleDebut; // ENTRE 0 ET 2pi
    private double angleFin;
    public PartialCircle(Vec2 center, double radius, double angleDebut, double angleFin) {
        super(center,radius);
        this.angleDebut=angleDebut;
        this.angleFin=angleFin;

    }

    public double getAngleDebut() {
        return angleDebut;
    }

    public double getAngleFin() {
        return angleFin;
    }
}

