package de.tinf13aibi.cardboardbro.Entities;

/**
 * Created by dth on 27.11.2015.
 */
public interface IEntity {
    void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace);
    float[] getModel();
    float[] getBaseModel();
    void resetModelToBase();
}
