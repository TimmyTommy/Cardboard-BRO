package de.tinf13aibi.cardboardbro.Geometry.Simple;

import org.json.JSONArray;
import org.json.JSONException;

import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 15.12.2015.
 */
public class Vec3d {
    public float x;
    public float y;
    public float z;

    public Vec3d copy(){
        return new Vec3d(toFloatArray());
    }

    public Vec3d(){
        x = 0;
        y = 0;
        z = 0;
    }

    public Vec3d(JSONArray json){
        x = (float)json.optDouble(0);
        y = (float)json.optDouble(1);
        z = (float)json.optDouble(2);
    }

    public Vec3d(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3d(float[] array){
        this.x = array[0];
        this.y = array[1];
        this.z = array[2];
    }

    public void assignFloatArray(float[] array){
        if (array!=null) {
            x = array[0];
            y = array[1];
            z = array[2];
        }
    }

    public float[] toFloatArray(){
        float[] res = new float[3];
        res[0] = x;
        res[1] = y;
        res[2] = z;
        return res;
    }

    public float[] toFloatArray4d(){
        float[] res = new float[4];
        res[0] = x;
        res[1] = y;
        res[2] = z;
        res[3] = 1;
        return res;
    }

    public void assignPoint3d(Vec3d vec3D){
        x = vec3D.x;
        y = vec3D.y;
        z = vec3D.z;
    }

    public JSONArray toJsonArray() throws JSONException {
        return new JSONArray(toFloatArray());
    }

    public float getLength(){
        return VecMath.calcVectorLength(this);
    }

    public String toString(){
        return "X: " + x + " Y: " + y + " Z: " + z;
    }
}
