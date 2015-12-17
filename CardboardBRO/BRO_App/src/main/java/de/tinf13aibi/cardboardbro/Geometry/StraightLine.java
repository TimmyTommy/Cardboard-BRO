package de.tinf13aibi.cardboardbro.Geometry;

/**
 * Created by dthom on 17.12.2015.
 */
public class StraightLine {
    public Point3d pos;
    public Point3d dir;
    public StraightLine(Point3d position, Point3d direction){
        this.pos = position;
        this.dir = direction;
    }
}
