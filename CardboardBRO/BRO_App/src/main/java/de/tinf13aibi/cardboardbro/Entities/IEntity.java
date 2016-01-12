package de.tinf13aibi.cardboardbro.Entities;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Enums.EntityDisplayType;
import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.Triangle;

/**
 * Created by dth on 27.11.2015.
 */
public interface IEntity {
    void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace);
    float[] getModel();
    void changedModel();
    float[] getBaseModel();
    EntityDisplayType getDisplayType();
    void resetModelToBase();
    ArrayList<Vec3d> getAbsoluteCoords();
}
