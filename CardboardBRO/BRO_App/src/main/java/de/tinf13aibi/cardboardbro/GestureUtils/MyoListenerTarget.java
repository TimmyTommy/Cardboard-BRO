package de.tinf13aibi.cardboardbro.GestureUtils;

import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

/**
 * Created by DTH on 08.01.2016.
 */
public interface MyoListenerTarget {
    void OnPoseChange(Pose previousPose, Pose newPose);
    void OnArmForwardUpdate(Quaternion armForward);
    void OnArmCenterUpdate(Quaternion armForwardCenter); //TODO verwenden
    void OnUpdateStatus(MyoStatus status);
}
