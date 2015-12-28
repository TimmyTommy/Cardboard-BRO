package de.tinf13aibi.cardboardbro.Entities;

import android.opengl.Matrix;

import java.util.Date;

import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 28.12.2015.
 */
public class User {
    private float[] mHeadView = new float[16];

    private Vec3d mUpVector = new Vec3d(0, 1, 0);
    private Vec3d mCenterOfView = new Vec3d(0, 0, -0.01f);

    private Vec3d mEyeForward = new Vec3d(0, 0, -0.01f);
    private Vec3d mArmForward = new Vec3d(0, 0, -0.01f);

    private Vec3d mPosition = new Vec3d();
    private Vec3d mVelocity = new Vec3d();

    private Date mLastUpdate = new Date();

    public float[] move(Vec3d acceleration){
        Date timeDelta = new Date(new Date().getTime()-mLastUpdate.getTime());
        float timeSeconds = timeDelta.getTime() * 0.001f;
        mLastUpdate = new Date();


        // Position berechnen
        mPosition.assignPoint3d(VecMath.calcVecPlusVec(mPosition, VecMath.calcVecTimesScalar(mVelocity, timeSeconds)));
        mCenterOfView.assignPoint3d(VecMath.calcVecPlusVec(mCenterOfView, VecMath.calcVecTimesScalar(mVelocity, timeSeconds)));

        //Augenhöhe auf mindestens 1,75m
        if (mPosition.y < 1.75f) {
            mPosition.y = 1.75f;
        }
        if (mCenterOfView.y < 1.75f) {
            mCenterOfView.y = 1.75f;
        }
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

        mVelocity.assignPoint3d(VecMath.calcVecTimesScalar(mVelocity, 0.90f));
        if (VecMath.calcVectorLength(mVelocity)<0.0001f){
            mVelocity.assignPoint3d(new Vec3d());
        }
//        // Alle Kräfte entfernen
//        clearForceAccum();

        return getCamera();
    }

    public float[] getCamera() {
        float[] camera = new float[16];
        Matrix.setLookAtM(camera, 0, mPosition.x, mPosition.y, mPosition.z,
                mCenterOfView.x, mCenterOfView.y, mCenterOfView.z,
                mUpVector.x, mUpVector.y, mUpVector.z);
        return camera;
    }

    public void setHeadView(float[] headView) {
        System.arraycopy(headView, 0, mHeadView, 0, 16);
    }

    public float[] getHeadView() {
        return mHeadView;
    }

    public float[] getInvHeadView() {
        float[] invertedHead = new float[16];
        Matrix.invertM(invertedHead, 0, mHeadView, 0);
        return invertedHead;
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
}
