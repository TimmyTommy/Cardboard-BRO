package de.tinf13aibi.cardboardbro.Geometry.Simple;

/**
 * Created by dthom on 06.01.2016.
 */
public class Line {
    private Vec3d p1, p2;

    public Line(Vec3d p1, Vec3d p2){
        this.p1 = p1.copy();
        this.p2 = p2.copy();
    }

    public Line(float[] line){
        this.p1 = new Vec3d(line[0], line[1], line[2]);
        this.p2 = new Vec3d(line[3], line[4], line[5]);
    }

    public Vec3d getP1() {
        return p1;
    }

    public void setP1(Vec3d p1) {
        this.p1 = p1;
    }

    public Vec3d getP2() {
        return p2;
    }

    public void setP2(Vec3d p2) {
        this.p2 = p2;
    }
}
