package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionTriangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dth on 25.01.2016.
 */
public class StateSelectEntityToDelete extends StateBase implements IState {
    public StateSelectEntityToDelete(DrawingContext drawingContext) {
        super(drawingContext);
    }

    @Override
    public void processOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        super.processOnDrawEye(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void processOnNewFrame(float[] headView, Vec3d armForwardVec) {
        super.processOnNewFrame(headView, armForwardVec);
        mUser.calcArmPointingAt(mDrawing.getEntityListWithFloorAndCanvas());
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction) {
            case DoStateBack: changeState(new StateSelectAction(mDrawingContext), "Go Back"); break;
            case DoEndSelect: deleteEntityPointingAt(mUser.getArmPointingAt()); break;
        }
    }

    private void deleteEntityPointingAt(IntersectionTriangle intersectionPoint){
        if (mDrawing.getEntityList().remove(intersectionPoint.entity)) {
            mOverlayView.show3DToast("Deleted Entity");
            mVibrator.vibrate(50);
        }
    }
}