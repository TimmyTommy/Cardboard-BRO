package de.tinf13aibi.cardboardbro.Entities.Triangulated;

import android.opengl.Matrix;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Geometry.GeomFactory;
import de.tinf13aibi.cardboardbro.Geometry.GeometryStruct;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 09.01.2016.
 */
public class CuboidEntity extends BaseEntity implements ITriangulatedEntity {
    private float[] mColor = new float[]{0, 0.7f, 0, 1};
    private Vec3d mBaseNormal = new Vec3d(0, 1, 0);
    private Vec3d mBaseVert = new Vec3d();

    private float mWidth = 1;
    private float mDepth = 1;
    private float mHeight = 1;

    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    private void recreateGeometry(boolean fix){
        GeometryStruct geometry = GeomFactory.createCuboidGeom(new Vec3d(0, 0, 0), mBaseNormal, mDepth, mWidth, mHeight, mColor, false);
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
        if (fix) {
            calcAbsoluteTriangles();
        }
    }
    public CuboidEntity(int program){
        super(program);
        recreateGeometry(true);
    }

    public float[] getColor() {
        return mColor;
    }

    public void setColor(float[] color) {
        mColor = color;
        recreateGeometry(true);
    }

    public float getDepth() {
        return mDepth;
    }

    public void setDepth(float depth) {
        mDepth = depth;
        recreateGeometry(true);
    }

    public float getWidth() {
        return mWidth;
    }

    public void setDepthAndWidth(float depth, float width, boolean fix) {
        mDepth = depth;
        mWidth = width;
        recreateGeometry(fix);
    }



    public void setWidth(float width) {
        mWidth = width;
        recreateGeometry(true);
    }

    public float getHeight() {
        return mHeight;
    }

    public void setHeight(float height, boolean fix) {
        mHeight = height;
        recreateGeometry(fix);
    }

    public Vec3d getBaseNormal() {
        return mBaseNormal;
    }

    public void setBaseNormal(Vec3d baseNormal) {
        mBaseNormal = baseNormal;
        recreateGeometry(true);
    }

    public Vec3d getBaseVert() {
        return mBaseVert;
    }

    public void setBaseVert(Vec3d baseVert) {
        mBaseVert = baseVert;
        Matrix.setIdentityM(mModel, 0);
        Matrix.translateM(mModel, 0, mBaseVert.x, mBaseVert.y, mBaseVert.z);
    }

    public void setAttributes(Vec3d center, Vec3d baseNormal, float depth, float width, float height, float[] color){
        setBaseVert(center);
        mBaseNormal = baseNormal;
        mDepth = depth;
        mWidth = width;
        mHeight = height;
        mColor = color;
        recreateGeometry(true);
    }
}