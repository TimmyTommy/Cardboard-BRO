package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionTriangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dth on 25.01.2016.
 */
public class StateSelectEntityToMove extends StateBase implements IState {
    public StateSelectEntityToMove(DrawingContext drawingContext) {
        super(drawingContext);
    }

    @Override
    public void processOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        super.processOnDrawEye(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void processOnNewFrame(float[] headView, Vec3d armForwardVec) {
        super.processOnNewFrame(headView, armForwardVec);
        mUser.calcArmPointingAt(mDrawing.getEntityList());
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction) {
            case DoStateBack: changeState(new StateSelectAction(mDrawingContext), "Go Back"); break;
            case DoEndSelect: grabEntityPointingAt(mUser.getArmPointingAt()); break;
        }
    }

    private void grabEntityPointingAt(IntersectionTriangle intersectionPoint){
        if (intersectionPoint != null) {
            IEntity entity = intersectionPoint.entity;
            if (entity != null) {
                mDrawingContext.setEditingEntity(entity);
                changeState(new StateWaitForEntityPlacePoint(mDrawingContext), "Grabbed Entity");
            }
        }
    }
}
