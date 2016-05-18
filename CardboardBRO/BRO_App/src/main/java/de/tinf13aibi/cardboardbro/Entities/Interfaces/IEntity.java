package de.tinf13aibi.cardboardbro.Entities.Interfaces;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.EntityDisplayType;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

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
    JSONObject toJsonObject() throws JSONException;
    void loadFromJsonObject(JSONObject jsonEntity) throws JSONException;
}
