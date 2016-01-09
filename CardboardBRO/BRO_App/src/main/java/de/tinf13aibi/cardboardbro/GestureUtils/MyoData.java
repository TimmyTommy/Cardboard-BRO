package de.tinf13aibi.cardboardbro.GestureUtils;

import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 08.01.2016.
 */
public class MyoData {
    private MyoStatus mMyoStatus = MyoStatus.DISCONNECTED;
    private Pose mPose = Pose.UNKNOWN;
    private Quaternion mArmForward = new Quaternion();
    private Quaternion mArmForwardCenter = new Quaternion();

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

    public Vec3d getArmForwardVec() {
        float[] quaternion = new float[]{
            (float)mArmForward.x(),
            (float)mArmForward.y(),
            (float)mArmForward.z(),
            (float)(float)mArmForward.w()
        };
        return VecMath.calcQuaternionToForwardVector(quaternion);
    }

    public void setArmForward(Quaternion armForward) {
        mArmForward = armForward;
    }

    public Quaternion getArmForwardCenter() {
        return mArmForwardCenter;
    }

    public void setArmForwardCenter(Quaternion armForwardCenter) {
        mArmForwardCenter = armForwardCenter;
    }
}
