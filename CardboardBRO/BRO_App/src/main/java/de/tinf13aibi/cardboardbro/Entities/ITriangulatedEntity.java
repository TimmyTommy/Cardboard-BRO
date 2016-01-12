package de.tinf13aibi.cardboardbro.Entities;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Geometry.Triangle;

/**
 * Created by dthom on 11.01.2016.
 */
public interface ITriangulatedEntity extends IEntity{
    ArrayList<Triangle> getAbsoluteTriangles();
//    Boolean hasFaces();
}
