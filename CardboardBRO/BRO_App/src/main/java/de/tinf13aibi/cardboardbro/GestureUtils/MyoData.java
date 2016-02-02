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

//    public Vec3d getArmForwardVec() {
//        float[] armForwardCenterQuat    = getArmForwardCenterQuat();
//        float[] armForwardQuat          = getArmForwardQuat();
//        float[] armCenterRotMat  = VecMath.calcQuaternionToMatrix(armForwardCenterQuat);
//        Matrix.invertM(armCenterRotMat, 0, armCenterRotMat, 0);
//
//        float[] armForwardRotMat = VecMath.calcQuaternionToMatrix(armForwardQuat);
//        float[] finalMat = new float[16];
//        Matrix.setIdentityM(finalMat, 0);
//        Matrix.multiplyMM(finalMat, 0, armCenterRotMat, 0, finalMat, 0);
//        Matrix.multiplyMM(finalMat, 0, armForwardRotMat, 0, finalMat, 0);
//
//        float[] initVec = new Vec3d(-1,0,0).toFloatArray4d();
//        float[] forwardVec = new float[4];
//        Matrix.multiplyMV(forwardVec, 0, finalMat, 0, initVec, 0);
//        return new Vec3d(forwardVec);
//
////        return VecMath.calcQuaternionToForwardVector(armForwardQuat);
//
////        float[] quat = new float[]{(float)mArmForward.x(), (float)mArmForward.y(), (float)mArmForward.z(), (float)mArmForward.w()};
////        float[] rotMat = VecMath.calcQuaternionToMatrix(quat);
////
////        float[] initVec = new Vec3d(0,0,-1).toFloatArray4d();
////        float[] forwardVec = new float[4];
////        Matrix.multiplyMV(forwardVec, 0, rotMat, 0, new float[]{0, 0, -1, 1}, 0);
////        Vec3d arm = new Vec3d(forwardVec);
////        return arm;
//    }

//    public Vec3d getArmForwardVec() {
//        float[] initVec = new Vec3d(0,0,1).toFloatArray4d();
//        float[] forwardVec = new float[4];
//
//        Quaternion armForward = mArmForward.normalized();
//        Quaternion armCenter = mArmForwardCenter.normalized();
//        Quaternion armDelta = armForward.normalized();
//        armDelta.multiply(armCenter);
//
//        Vec3d init = new Vec3d(0,0,-1);
//        Vec3d armDeltaXYZ = new Vec3d((float)armDelta.x(),(float)armDelta.y(),(float)armDelta.z());
//        Vec3d cross1 = VecMath.calcCrossProduct(armDeltaXYZ, init);
//        Vec3d v2 = VecMath.calcVecTimesScalar(init, (float) armDelta.w());
//        Vec3d v3 = VecMath.calcVecPlusVec(cross1, v2);
//        Vec3d cross2 = VecMath.calcCrossProduct(armDeltaXYZ, v3);
//        Vec3d cross2times2 = VecMath.calcVecTimesScalar(cross2, 2);
//        Vec3d dir = VecMath.calcVecPlusVec(init, cross2times2);
//        return dir;
////        return v + 2.0 * cross2(q.xyz, cross1(q.xyz, v) + q.w * v);
//
//
////        Quaternion armForward = mArmForward.normalized();
////        Quaternion armCenter = mArmForwardCenter.normalized();
////        Quaternion armDelta = armForward.normalized();
////        armDelta.multiply(armCenter);
////
//////        Quaternion armDelta = mArmForwardCenter.normalized();
//////        armDelta.inverse();
//////        armDelta.multiply(mArmForward);
////
////        float[] rotMatArm = VecMath.calcQuaternionToMatrix(quatToFloatArray(armDelta));
////        Matrix.multiplyMV(forwardVec, 0, rotMatArm, 0, initVec, 0);
////
//////        float[] rotMatCenter = VecMath.calcQuaternionToMatrix(getArmForwardCenterQuat());
//////        Matrix.invertM(rotMatCenter, 0, rotMatCenter, 0);
//////        float[] rotMatArm = VecMath.calcQuaternionToMatrix(getArmForwardQuat());
////
//////        Matrix.multiplyMV(forwardVec, 0, rotMatCenter, 0, initVec, 0);
//////        Matrix.multiplyMV(forwardVec, 0, rotMatArm, 0, forwardVec, 0);
////
//////        float[] anOp = new float[16];
//////        Matrix.setRotateM(anOp, 0, deltaPitchDeg, 1, 0, 0);
//////        Matrix.rotateM(anOp, 0, deltaYawDeg, 0, 1, 0);
//////
//////        Matrix.multiplyMV(forwardVec, 0, anOp, 0, initVec, 0);
////
////        Matrix.multiplyMV(forwardVec, 0, mCenterHeadViewMat, 0, forwardVec, 0);
////
////        return new Vec3d(forwardVec);
//////        return VecMath.calcVecTimesScalar(new Vec3d(forwardVec), -1);
//    }

    public Vec3d getArmForwardVec() {
        double centerYaw = Quaternion.yaw(mArmForwardCenter);
        double centerPitch = Quaternion.pitch(mArmForwardCenter);
        double currentYaw = Quaternion.yaw(mArmForward);
        double currentPitch = Quaternion.pitch(mArmForward);
        double deltaYaw = calculateDeltaRadians(currentYaw, centerYaw);
        double deltaPitch = calculateDeltaRadians(currentPitch, centerPitch);
        float deltaYawDeg = VecMath.radToDeg((float) deltaYaw);
        float deltaPitchDeg = VecMath.radToDeg((float) deltaPitch);

        float[] initVec = new Vec3d(0,0,1).toFloatArray4d();
        float[] forwardVec = new float[4];

        float[] anOp = new float[16];
        Matrix.setRotateM(anOp, 0, deltaPitchDeg, 1, 0, 0);
        Matrix.rotateM(anOp, 0, deltaYawDeg, 0, 1, 0);

        Matrix.multiplyMV(forwardVec, 0, anOp, 0, initVec, 0);

        Matrix.multiplyMV(forwardVec, 0, mCenterHeadViewMat, 0, forwardVec, 0);

        return VecMath.calcVecTimesScalar(new Vec3d(forwardVec), -1);
    }

//    //TODO funktioniert nur so halb
//    public Vec3d getArmForwardVec() {
//        double centerYaw = Quaternion.yaw(mArmForwardCenter);
//        double centerPitch = Quaternion.pitch(mArmForwardCenter);
//        double currentYaw = Quaternion.yaw(mArmForward);
//        double currentPitch = Quaternion.pitch(mArmForward);
//        double deltaYaw = calculateDeltaRadians(currentYaw, centerYaw);
//        double deltaPitch = calculateDeltaRadians(currentPitch, centerPitch);
//        float deltaYawDeg = VecMath.radToDeg((float)deltaYaw);
//        float deltaPitchDeg = VecMath.radToDeg((float)deltaPitch);
//
//
//        float[] finalRotMat = new float[16];
//        Matrix.setIdentityM(finalRotMat, 0);
//
//        Matrix.multiplyMM(finalRotMat, 0, mCenterHeadViewMat, 0, finalRotMat, 0); //Rotieren um Centrierten Kopf
//
//        float[] anOp = new float[16];
//        Matrix.setRotateM(anOp, 0, deltaPitchDeg, 1, 0, 0);
//        Matrix.rotateM(anOp, 0, deltaYawDeg, 0, 1, 0);
//
////        Matrix.setRotateEulerM(anOp, 0, deltaPitchDeg, deltaYawDeg, 0);
//
//        Matrix.multiplyMM(finalRotMat, 0, anOp, 0, finalRotMat, 0); //Rotieren um MYO rotation
//
//        float[] initVec = new Vec3d(0,0,1).toFloatArray4d();
//        float[] forwardVec = new float[4];
//        Matrix.multiplyMV(forwardVec, 0, finalRotMat, 0, initVec, 0);
//
//        return new Vec3d(forwardVec);
//    }

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
