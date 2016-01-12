package de.tinf13aibi.cardboardbro.Entities.Triangulated;

import android.opengl.Matrix;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IManySidedEntity;
import de.tinf13aibi.cardboardbro.Geometry.GeomFactory;
import de.tinf13aibi.cardboardbro.Geometry.GeometryStruct;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 07.01.2016.
 */
public class CylinderEntity extends BaseEntity implements IManySidedEntity {
    private float[] mColor = new float[]{0, 0.7f, 0, 1};
    private float mRadius = 1;
    private float mHeight = 1;
    private Vec3d mBaseNormal = new Vec3d(0, 1, 0);
    private Vec3d mCenter = new Vec3d();
    private CuboidEntity mHitBox;

    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        super.draw(view, perspective, lightPosInEyeSpace);

//        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        mHitBox.draw(view, perspective, lightPosInEyeSpace);
//        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    @Override
    public CuboidEntity getHitBox() {
        return mHitBox;
    }

    private void calcHitbox(){
        Vec3d depthDir = new Vec3d();
        Vec3d widthDir = new Vec3d();
        Vec3d heightDir = mBaseNormal.copy();
        VecMath.calcCrossedVectorsFromNormal(widthDir, depthDir, heightDir);

        Vec3d diagonalVec = VecMath.calcVecPlusVec(widthDir, depthDir);
        Vec3d diagonalVecTimesRadius = VecMath.calcVecTimesScalar(diagonalVec, mRadius);
        Vec3d basePoint = VecMath.calcVecMinusVec(mCenter, diagonalVecTimesRadius);

        mHitBox.setAttributes(basePoint, mBaseNormal, mRadius * 2, mRadius * 2, mHeight, new float[]{0.5f, 0.5f, 1, 0.5f});
    }

    private void recreateGeometry(boolean fix){
        GeometryStruct geometry = GeomFactory.createCylinderGeom(new Vec3d(0, 0, 0), mBaseNormal, mRadius, mHeight, mColor, false);
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
        if (fix) {
            calcAbsoluteTriangles();
            calcHitbox();
        }
    }

    public CylinderEntity(int program){
        super(program);
        mHitBox = new CuboidEntity(program);
        recreateGeometry(true);
    }

    public float[] getColor() {
        return mColor;
    }

    public void setColor(float[] color) {
        mColor = color;
        recreateGeometry(true);
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius, boolean fix) {
        mRadius = radius;
        recreateGeometry(fix);
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

    public Vec3d getCenter() {
        return mCenter;
    }

    public void setCenter(Vec3d center) {
        mCenter = center;
        Matrix.setIdentityM(mModel, 0);
        Matrix.translateM(mModel, 0, mCenter.x, mCenter.y, mCenter.z);
    }

    public void setAttributes(Vec3d center, Vec3d baseNormal, float radius, float height, float[] color){
        setCenter(center);
        mBaseNormal = baseNormal;
        mRadius = radius;
        mHeight = height;
        mColor = color;
        recreateGeometry(true);
    }
}
