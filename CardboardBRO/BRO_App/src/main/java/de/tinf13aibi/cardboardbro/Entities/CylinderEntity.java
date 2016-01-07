package de.tinf13aibi.cardboardbro.Entities;

import de.tinf13aibi.cardboardbro.Constants;
import de.tinf13aibi.cardboardbro.Geometry.GeomFactory;
import de.tinf13aibi.cardboardbro.Geometry.GeometryDatabase;
import de.tinf13aibi.cardboardbro.Geometry.GeometryStruct;
import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 07.01.2016.
 */
public class CylinderEntity extends BaseEntity implements IEntity  {
    private float[] mColor = new float[]{0, 0.7f, 0, 1};
    private float mRadius = 1;
    private float mHeight = 1;

    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        super.draw(view, perspective, lightPosInEyeSpace);
    }

    private void recreateGeometry(){
        GeometryStruct geometry = GeomFactory.createCylinderGeom(new Vec3d(0, 0, 0), mRadius, mHeight, mColor, false);
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
    }

    public CylinderEntity(int program){
        super(program);
        recreateGeometry();
    }

    public float[] getColor() {
        return mColor;
    }

    public void setColor(float[] color) {
        mColor = color;
        recreateGeometry();
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        mRadius = radius;
        recreateGeometry();
    }

    public float getHeight() {
        return mHeight;
    }

    public void setHeight(float height) {
        mHeight = height;
        recreateGeometry();
    }
}
