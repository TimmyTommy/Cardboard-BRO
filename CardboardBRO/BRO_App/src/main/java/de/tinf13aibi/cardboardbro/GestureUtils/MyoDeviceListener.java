package de.tinf13aibi.cardboardbro.GestureUtils;

/**
 * Created by DTH on 08.01.2016.
 */

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;

import java.util.ArrayList;

public class MyoDeviceListener extends AbstractDeviceListener {
    static private MyoDeviceListener mInstance;
    private Quaternion mArmForward;

    private Pose mLastPose = Pose.UNKNOWN;

    private double PI = Math.PI;    // 180°
    private double TWOPI = PI*2;    // 360°
    private double EIGHTHPI = PI/8; // 22.5°

    private double centreYaw = 0.0;
    private double centrePitch = 0.0;

    private double YAW_DEADZONE = EIGHTHPI;
    private double PITCH_DEADZONE = EIGHTHPI;

    private Myo mMyo;
    private ArrayList<MyoListenerTarget> mTargets;

    private MyoStatus mStatus = MyoStatus.DISCONNECTED;

    public MyoStatus getStatus(){
        return mStatus;
    }

    private void notifyPoseChange(Pose previousPose, Pose newPose) {
        for(MyoListenerTarget target : mTargets) {
            target.OnPoseChange(previousPose, newPose);
        }
    }

    private void notifyArmForward(Quaternion armForward) {
        for(MyoListenerTarget target : mTargets) {
            target.OnArmForwardUpdate(armForward);
        }
    }

    private void notifyArmCenter(Quaternion armForwardCenter) {
        for(MyoListenerTarget target : mTargets) {
            target.OnArmCenterUpdate(armForwardCenter);
        }
    }

    private void notifyUpdateStatus(MyoStatus status) {
        mStatus = status;
        for(MyoListenerTarget target : mTargets) {
            target.OnUpdateStatus(status);
        }
    }


    private MyoDeviceListener(){
//        mArmPosition = new Point(2, 2);
//        mLastSetPoint = new Point(2, 2);
        mTargets = new ArrayList<>();
    }

    static public MyoDeviceListener getInstance() {
        if (mInstance == null) {
            mInstance = new MyoDeviceListener();
        }
        return mInstance;
    }

    public void addTarget(MyoListenerTarget target) {
        if (!mTargets.contains(target)){
            mTargets.add(target);
        }
    }

    public void removeTarget(MyoListenerTarget target) {
        mTargets.remove(target);
    }

    @Override
    public void onPose(Myo myo, long timestamp, Pose pose) {
        super.onPose(myo, timestamp, pose);
        if(pose == Pose.DOUBLE_TAP) {
            centre(mArmForward);
        }else if ( pose == Pose.FINGERS_SPREAD){
            myo.lock();
        }
        mMyo = myo;

        if (mLastPose != pose) {
            notifyPoseChange(mLastPose, pose);
            mLastPose = pose;
        }
    }


    @Override
    public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
        super.onArmSync(myo, timestamp, arm, xDirection);
        mMyo = myo;
        notifyUpdateStatus(MyoStatus.LOCKED);
    }

    @Override
    public void onArmUnsync(Myo myo, long timestamp) {
        super.onArmUnsync(myo, timestamp);
        mMyo = myo;
        notifyUpdateStatus(MyoStatus.UNSYNCED);
    }

    @Override
    public void onConnect(Myo myo, long timestamp) {
        super.onConnect(myo, timestamp);
        mMyo = myo;
        notifyUpdateStatus(MyoStatus.UNSYNCED);
    }

    @Override
    public void onDisconnect(Myo myo, long timestamp) {
        super.onDisconnect(myo, timestamp);
        mMyo = myo;
        notifyUpdateStatus(MyoStatus.DISCONNECTED);
    }

    @Override
    public void onUnlock(Myo myo, long timestamp) {
        super.onUnlock(myo, timestamp);
        myo.unlock(Myo.UnlockType.HOLD);
        centre(mArmForward);
        mMyo = myo;
        notifyUpdateStatus(MyoStatus.IDLE);
    }

    @Override
    public void onLock(Myo myo, long timestamp) {
        super.onLock(myo, timestamp);
        mMyo = myo;
        notifyUpdateStatus(MyoStatus.LOCKED);
    }

    @Override
    public void onDetach(Myo myo, long timestamp) {
        super.onDetach(myo, timestamp);
        mMyo = myo;
    }

    @Override
    public void onAttach(Myo myo, long timestamp) {
        super.onAttach(myo, timestamp);
        mMyo = myo;
        notifyUpdateStatus(MyoStatus.UNSYNCED);
    }

    @Override
    public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
        super.onGyroscopeData(myo, timestamp, gyro);
        mMyo = myo;
    }

    @Override
    public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
        super.onAccelerometerData(myo, timestamp, accel);
        mMyo = myo;
    }

    @Override
    public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
        super.onOrientationData(myo, timestamp, rotation);
        mArmForward = rotation;
        notifyArmForward(mArmForward);
    }

    private int getArmPositionX(double deltaYaw){
        if (deltaYaw < -YAW_DEADZONE){
            return 1;
        } else if (deltaYaw > YAW_DEADZONE){
            return -1;
        } else {
            return 0;
        }
    }

    private int getArmPositionY(double deltaPitch){
        XDirection direction = mMyo.getXDirection();
        int y = (direction == XDirection.TOWARD_WRIST) ? 1 : -1;
        if (deltaPitch < -PITCH_DEADZONE){
            return y;
        } else if (deltaPitch > PITCH_DEADZONE){
            return -y;
        } else {
            return 0;
        }
    }

    private void calculateArmPosition(Quaternion orientation){
        if (centreYaw == 0) {
            return;
        }
        double currentYaw = Quaternion.yaw(orientation);
        double currentPitch = Quaternion.pitch(orientation);
        double deltaYaw = calculateDeltaRadians(currentYaw, centreYaw);
        double deltaPitch = calculateDeltaRadians(currentPitch, centrePitch);

        int x = getArmPositionX(deltaYaw);
        int y = getArmPositionY(deltaPitch);
//        mArmPosition.set(x, y);
    }

    private double calculateDeltaRadians(double current, double centre){
        double delta = current - centre;
        if (delta > PI) {
            delta = delta - TWOPI;
        } else if (delta < -PI) {
            delta = delta + TWOPI;
        }
        return delta;
    }

    private void centre(Quaternion armForward){
        notifyArmCenter(armForward);
        if (armForward!=null) {
            centreYaw = Quaternion.yaw(armForward);
            centrePitch = Quaternion.pitch(armForward);
        }
    }

}

