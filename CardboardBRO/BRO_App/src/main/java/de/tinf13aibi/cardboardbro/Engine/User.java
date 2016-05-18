package de.tinf13aibi.cardboardbro.Engine;

import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.Date;

import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.CrosshairEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.LineEntity;
import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionEntityList;
import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionPlane;
import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionTriangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Plane;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Straight;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 28.12.2015.
 */
public class User {
    private float[] mHeadView = new float[16];
    private float[] mInvHeadView = new float[16];

    private Vec3d mUpVector = new Vec3d(0, 1, 0);
    private Vec3d mCenterOfView = new Vec3d(0, 0, -0.01f);

    private Vec3d mEyeForward = new Vec3d(0, 0, -0.01f);
    private Vec3d mArmForward = new Vec3d(0, 0, -0.01f);

    private IntersectionTriangle mEyeLookingAt;
    private IntersectionTriangle mArmPointingAt;

    private CrosshairEntity mEyeCrosshair;
    private CrosshairEntity mArmCrosshair;

    private LineEntity mArmLine;

    private Vec3d mPosition = new Vec3d();
    private Vec3d mVelocity = new Vec3d();

    private Date mLastUpdate = new Date();

    private Vec3d mAcceleration3D = new Vec3d();

    private Vec3d mAcceleration2D = new Vec3d();

    private Vec3d mAccelerationY = new Vec3d();

    public void drawCrosshairs(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        if (mEyeCrosshair != null) {
            mEyeCrosshair.draw(view, perspective, lightPosInEyeSpace);
        }
        if (mArmCrosshair != null) {
            mArmCrosshair.draw(view, perspective, lightPosInEyeSpace);
            mArmLine.draw(view, perspective, lightPosInEyeSpace);
        }
    }

    public void createCrosshairs(int program){
        mEyeCrosshair = new CrosshairEntity(program);
        mArmCrosshair = new CrosshairEntity(program);

        mArmLine = new LineEntity(program);
        mArmLine.setColor(0, 1, 1, 1);
    }


    private void calcCrosshairPos(CrosshairEntity crosshairEntity, IntersectionTriangle pointingAt, Vec3d forwardVec){
        if (pointingAt!=null) {
            crosshairEntity.setPosition(pointingAt.intersectionPos, pointingAt.triangleNormal, pointingAt.distance);
        } else {
            //calc cross some meters away from eyes
            Vec3d farPointOnEyeLine = VecMath.calcVecPlusVec(mPosition, VecMath.calcVecTimesScalar(forwardVec, 100));
            Vec3d distanceVec = VecMath.calcVecMinusVec(farPointOnEyeLine, mPosition);
            float distance = VecMath.calcVectorLength(distanceVec);
            crosshairEntity.setPosition(farPointOnEyeLine, forwardVec, distance);
        }
    }

    public IntersectionTriangle calcEyeLookingAt(ArrayList<IEntity> entityList){
        Straight line = new Straight(mPosition, mEyeForward);
        mEyeLookingAt = new IntersectionEntityList(line, entityList).nearestIntersection;
        calcCrosshairPos(mEyeCrosshair, mEyeLookingAt, mEyeForward);
        return mEyeLookingAt;
    }

    public IntersectionTriangle calcArmPointingAt(ArrayList<IEntity> entityList){
        Straight line = new Straight(mPosition, mArmForward);
        mArmPointingAt = new IntersectionEntityList(line, entityList).nearestIntersection;
        calcCrosshairPos(mArmCrosshair, mArmPointingAt, mArmForward);

        mArmLine.setVerts(mPosition, mArmCrosshair.getPosition());
        return mArmPointingAt;
    }

    public void calcArmPointingAt(Plane plane){
        if (plane!=null) {
            Straight line = new Straight(mPosition, mArmForward);
            mArmPointingAt = new IntersectionPlane(line, plane);
            calcCrosshairPos(mArmCrosshair, mArmPointingAt, mArmForward);

            mArmLine.setVerts(mPosition, mArmCrosshair.getPosition());
//            return mArmPointingAt;
        }
    }

    public float[] step(){
        Vec3d acceleration = VecMath.calcVecTimesScalar(getEyeForward(), 2);//0.3f); //

        mVelocity.assignPoint3d(VecMath.calcVecPlusVec(mVelocity, VecMath.calcVecTimesScalar(acceleration, 1)));

        mVelocity.assignPoint3d(VecMath.calcVecTimesScalar(mVelocity, 0.90f));
        if (VecMath.calcVectorLength(mVelocity)<0.0001f){
            mVelocity.assignPoint3d(new Vec3d());
        }

        return getCamera();
    }

    private Vec3d calcMovementByControllerInput(){
        //2D Movement über 2D-Blickrichtung + Analogstick
        float[] forwardVec = new float[]{0,0,0,0};
        if (mAcceleration2D.getLength()>0) {
            Matrix.multiplyMV(forwardVec, 0, mInvHeadView, 0, mAcceleration2D.toFloatArray4d(), 0);
        }
        Vec3d acceleration = new Vec3d(forwardVec);
        acceleration.y = 0;
        if (acceleration.getLength()>0) {
            Vec3d normalizedAcceleration = VecMath.calcNormalizedVector(acceleration);
            acceleration = VecMath.calcVecTimesScalar(normalizedAcceleration, 5);
        }

        //Y-Movement über Controller-Buttons
        Vec3d accelerationY = VecMath.calcVecTimesScalar(mAccelerationY, 5);
        acceleration.y = accelerationY.y;

        return acceleration;
    }

    private Vec3d calcMovementByCardboardButton(){
        //3D Movement über 3D-Blickrichtung + Button am Cardboard
        float[] forwardVec2 = new float[]{0,0,0,0};
        if (mAcceleration3D.getLength()>0) {
            Matrix.multiplyMV(forwardVec2, 0, mInvHeadView, 0, mAcceleration3D.toFloatArray4d(), 0);
        }
        return VecMath.calcVecTimesScalar(new Vec3d(forwardVec2), 5);
    }

    public float[] move(){
        Vec3d acceleration = calcMovementByCardboardButton();
        Vec3d acceleration3D = calcMovementByControllerInput();

        acceleration = VecMath.calcVecPlusVec(acceleration, acceleration3D);

        return move(acceleration);
    }

    private float[] move(Vec3d acceleration){
        Date timeDelta = new Date(new Date().getTime()-mLastUpdate.getTime());
        float timeSeconds = timeDelta.getTime() * 0.001f;
        mLastUpdate = new Date();

        // Position berechnen
        mPosition.assignPoint3d(VecMath.calcVecPlusVec(mPosition, VecMath.calcVecTimesScalar(mVelocity, timeSeconds)));
        mCenterOfView.assignPoint3d(VecMath.calcVecPlusVec(mCenterOfView, VecMath.calcVecTimesScalar(mVelocity, timeSeconds)));

        //Augenhöhe auf mindestens 0m
//        if (mPosition.y < 0) {
//            mPosition.y = 0;
//        }
//        if (mCenterOfView.y < 0) {
//            mCenterOfView.y =0;
//        }
        //Augenhöhe auf mindestens 1,75m
        if (mPosition.y < 1.75f) {
            mPosition.y = 1.75f;
        }
        if (mCenterOfView.y < 1.75f) {
            mCenterOfView.y = 1.75f;
        }

        //Augenhöhe immer auf 1,75m
//        if (mPosition.y != 1.75f) {
//            mPosition.y = 1.75f;
//        }
//        if (mCenterOfView.y != 1.75f) {
//            mCenterOfView.y = 1.75f;
//        }

        // Beschleunigung berechnen
//        Vector3 acc = m_ForceAccum * m_InverseMass;

        // Neue Geschwindigkeit berechnen
        mVelocity.assignPoint3d(VecMath.calcVecPlusVec(mVelocity, VecMath.calcVecTimesScalar(acceleration, timeSeconds)));
        mVelocity.assignPoint3d(VecMath.calcVecTimesScalar(mVelocity, 0.85f));
        if (VecMath.calcVectorLength(mVelocity)<0.0001f){
            mVelocity.assignPoint3d(new Vec3d());
        }

        return getCamera();
    }

    public float[] getCamera() {
        float[] camera = new float[16];
        Matrix.setLookAtM(camera, 0, mPosition.x, mPosition.y, mPosition.z,
                mCenterOfView.x, mCenterOfView.y, mCenterOfView.z,
                mUpVector.x, mUpVector.y, mUpVector.z);
        return camera;
    }

    public float[] getInvCamera() {
        float[] invCamera = new float[16];
        Matrix.invertM(invCamera, 0, getCamera(), 0);
        return invCamera;
    }

    public void setHeadView(float[] headView) {
        System.arraycopy(headView, 0, mHeadView, 0, 16);
        Matrix.invertM(mInvHeadView, 0, mHeadView, 0);

        Vec3d forwardIdent = new Vec3d(0, 0, -1);
        float[] forwardVec = new float[4];

        Matrix.multiplyMV(forwardVec, 0, mInvHeadView, 0, forwardIdent.toFloatArray4d(), 0);
        mEyeForward.assignFloatArray(VecMath.calcNormalizedVector(forwardVec));
    }

    public float[] getHeadView() {
        return mHeadView;
    }

    public float[] getInvHeadView() {
        return mInvHeadView;
    }

    public Vec3d getUpVector() {
        return mUpVector;
    }

    public Vec3d getCenterOfView() {
        return mCenterOfView;
    }

    public Vec3d getEyeForward() {
        return mEyeForward;
    }

    public Vec3d getArmForward() {
        return mArmForward;
    }

    public Vec3d getPosition() {
        return mPosition;
    }

    public Vec3d getVelocity() {
        return mVelocity;
    }

    public Date getLastUpdate() {
        return mLastUpdate;
    }

    public IntersectionTriangle getEyeLookingAt() {
        return mEyeLookingAt;
    }

    public IntersectionTriangle getArmPointingAt() {
        return mArmPointingAt;
    }

    public CrosshairEntity getEyeCrosshair() {
        return mEyeCrosshair;
    }

    public CrosshairEntity getArmCrosshair() {
        return mArmCrosshair;
    }

    public Vec3d getAcceleration3D() {
        return mAcceleration3D;
    }

    public void setAcceleration3D(Vec3d mAcceleration3D) {
        this.mAcceleration3D = mAcceleration3D;
    }

    public Vec3d getAcceleration2D() {
        return mAcceleration2D;
    }

    public void setAcceleration2D(Vec3d acceleration) {
        mAcceleration2D = acceleration;
    }

    public Vec3d getAccelerationY() {
        return mAccelerationY;
    }

    public void setAccelerationY(Vec3d mAccelerationY) {
        this.mAccelerationY = mAccelerationY;
    }

    public void setArmForward(Vec3d armForward) {
        mArmForward.assignPoint3d(armForward);
    }
}
