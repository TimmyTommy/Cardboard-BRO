package de.tinf13aibi.cardboardbro.GestureUtils;

import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

import de.tinf13aibi.cardboardbro.Geometry.Vec3d;

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

    public Pose setPose(Pose pose) {
        Pose previousPose = mPose;
        mPose = pose;
        return previousPose;
        //TODO evtl wenn neue pose == REST --> auswerten was davor war: z.B. Wechsel von FINGERSPREAD auf REST bereutet
    }

    public Quaternion getArmForward() {
        return mArmForward;
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
