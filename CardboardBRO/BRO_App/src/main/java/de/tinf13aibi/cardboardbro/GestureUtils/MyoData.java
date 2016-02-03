package de.tinf13aibi.cardboardbro.GestureUtils;

import android.opengl.Matrix;

import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 08.01.2016.
 */
public class MyoData {
    private MyoStatus mMyoStatus = MyoStatus.DISCONNECTED;
    private Pose mPose = Pose.UNKNOWN;
    private Quaternion mArmForward = new Quaternion();
    private Quaternion mArmForwardCenter = new Quaternion();
    private float[] mCenterHeadViewMat = new float[16];

    public MyoData(){
        Matrix.setIdentityM(mCenterHeadViewMat, 0);
    }

    public MyoStatus getMyoStatus() {
        return mMyoStatus;
    }

    public void setMyoStatus(MyoStatus myoStatus) {
        mMyoStatus = myoStatus;
    }

    public Pose getPose() {
        return mPose;
    }

    public void setPose(Pose pose) {
        mPose = pose;
    }

    public Quaternion getArmForward() {
        return mArmForward;
    }

    public float[] getArmForwardQuat() {
        return new float[]{
            (float)mArmForward.x(),
            (float)mArmForward.y(),
            (float)mArmForward.z(),
            (float)mArmForward.w()
        };
    }

    public float[] getArmForwardCenterQuat() {
        return new float[]{
            (float)mArmForwardCenter.x(),
            (float)mArmForwardCenter.y(),
            (float)mArmForwardCenter.z(),
            (float)mArmForwardCenter.w()
        };
    }

    public float[] quatToFloatArray(Quaternion quaternion) {
        return new float[]{
                (float)quaternion.x(),
                (float)quaternion.y(),
                (float)quaternion.z(),
                (float)quaternion.w()
        };
    }

    public Vec3d getArmForwardVec() {
        double centerYaw = Quaternion.yaw(mArmForwardCenter);
        double centerPitch = Quaternion.pitch(mArmForwardCenter);
        double currentYaw = Quaternion.yaw(mArmForward);
        double currentPitch = Quaternion.pitch(mArmForward);
        double deltaYaw = calculateDeltaRadians(currentYaw, centerYaw);
        double deltaPitch = calculateDeltaRadians(currentPitch, centerPitch);
        float deltaYawDeg = VecMath.radToDeg((float) deltaYaw);
        float deltaPitchDeg = VecMath.radToDeg((float) deltaPitch);

//        Log.i("Arm", String.format("Yaw: %.2f, Pitch: %.2f", deltaYawDeg, deltaPitchDeg));

        float[] initVec = new Vec3d(0,0,1).toFloatArray4d();
        float[] forwardVec = new float[4];

        float[] anOp = new float[16];
        Matrix.setRotateM(anOp, 0, deltaPitchDeg, 1, 0, 0);

        float[] anOp2 = new float[16];
        Matrix.setRotateM(anOp2, 0, deltaYawDeg, 0, 1, 0);

        Matrix.multiplyMV(forwardVec, 0, anOp, 0, initVec, 0);
        Matrix.multiplyMV(forwardVec, 0, anOp2, 0, forwardVec, 0);
        Matrix.multiplyMV(forwardVec, 0, mCenterHeadViewMat, 0, forwardVec, 0);

//        Matrix.setRotateM(anOp, 0, deltaYawDeg, 0, 1, 0);
//        Matrix.rotateM(anOp, 0, deltaPitchDeg, 1, 0, 0);

//        Matrix.multiplyMV(forwardVec, 0, mCenterHeadViewMat, 0, initVec, 0);
//        Matrix.multiplyMV(forwardVec, 0, anOp, 0, forwardVec, 0);

//        Matrix.multiplyMV(forwardVec, 0, anOp, 0, initVec, 0);
//        Matrix.multiplyMV(forwardVec, 0, mCenterHeadViewMat, 0, forwardVec, 0);

        return VecMath.calcVecTimesScalar(new Vec3d(forwardVec), -1);
    }

    private double calculateDeltaRadians(double current, double centre){
        double delta = current - centre;
        if (delta > Math.PI) {
            delta = delta - Math.PI*2;
        } else if (delta < -Math.PI) {
            delta = delta + Math.PI*2;
        }
        return delta;
    }

    public void setArmForward(Quaternion armForward) {
        mArmForward = armForward;
    }

    public Quaternion getArmForwardCenter() {
        return mArmForwardCenter;
    }

    public void setArmForwardCenter(Quaternion armForwardCenter, float[] headViewMat) {
        mArmForwardCenter = armForwardCenter;
    }

    public void setArmForwardCenter(Quaternion armForwardCenter) {
        mArmForwardCenter = armForwardCenter;
    }

    public float[] getCenterHeadViewMat() {
        return mCenterHeadViewMat;
    }

    public void setCenterHeadViewMat(float[] centerHeadViewMat) {
        mCenterHeadViewMat = centerHeadViewMat.clone();
    }
}
