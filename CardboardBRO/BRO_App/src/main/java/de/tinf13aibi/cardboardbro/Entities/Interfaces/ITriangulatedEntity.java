package de.tinf13aibi.cardboardbro.Entities.Interfaces;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 11.01.2016.
 */
public interface ITriangulatedEntity extends IEntity{
    ArrayList<Triangle> getAbsoluteTriangles();
    void setPositionAndOrientation(Vec3d position, Vec3d baseNormal, boolean fix);
    Vec3d getBaseVert();
    Vec3d getBaseNormal();
    ITriangulatedEntity clone();
}
