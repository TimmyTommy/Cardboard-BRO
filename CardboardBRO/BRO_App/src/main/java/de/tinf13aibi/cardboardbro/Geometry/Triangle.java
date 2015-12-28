package de.tinf13aibi.cardboardbro.Geometry;

/**
 * Created by dthom on 17.12.2015.
 */
public class Triangle {
    public Vec3d p1, p2, p3;
    public Triangle(Vec3d p1, Vec3d p2, Vec3d p3){
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }
    public Triangle(float[] triangle){
        this.p1 = new Vec3d(triangle[0], triangle[1], triangle[2]);
        this.p2 = new Vec3d(triangle[3], triangle[4], triangle[5]);
        this.p3 = new Vec3d(triangle[6], triangle[7], triangle[8]);
    }

    public float[] toFloatArray(){
        float[] triangle = new float[9];
        System.arraycopy(p1.toFloatArray(), 0, triangle, 0, 3);
        System.arraycopy(p2.toFloatArray(), 0, triangle, 3, 3);
        System.arraycopy(p3.toFloatArray(), 0, triangle, 6, 3);
        return triangle;
    }
}
