package de.tinf13aibi.cardboardbro.Entities;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Geometry.Point3d;
import de.tinf13aibi.cardboardbro.Geometry.Triangle;

/**
 * Created by dth on 27.11.2015.
 */
public interface IEntity {
    void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace);
    float[] getModel();
    float[] getBaseModel();
    void resetModelToBase();
    ArrayList<Point3d> getAbsoluteCoords();
    ArrayList<Triangle> getAbsoluteTriangles();
}
