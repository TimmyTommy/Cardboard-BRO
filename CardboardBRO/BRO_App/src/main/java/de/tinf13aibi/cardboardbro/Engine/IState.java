package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 21.01.2016.
 */
public interface IState {
    void processOnNewFrame(float[] headView, Vec3d armForwardVec);
    void processOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace);
    void processInputAction(InputAction inputAction);
    void processUserMoving(Vec3d acceleration, InputAction inputAction);
}
