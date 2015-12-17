package de.tinf13aibi.cardboardbro.Geometry;

/**
 * Created by dthom on 15.12.2015.
 */
public class Point3d {
    public float x;
    public float y;
    public float z;

    public Point3d(){
        x = 0;
        y = 0;
        z = 0;
    }

    public Point3d(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3d(float[] array){
        this.x = array[0];
        this.y = array[1];
        this.z = array[2];
    }

    public float[] toFloatArray(){
        float[] res = new float[3];
        res[0] = x;
        res[1] = y;
        res[2] = z;
        return res;
    }
}
