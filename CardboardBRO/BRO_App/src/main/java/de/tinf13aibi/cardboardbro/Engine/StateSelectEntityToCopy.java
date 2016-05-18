package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionTriangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dth on 18.05.2016.
 */
public class StateSelectEntityToCopy  extends StateBase implements IState {
    public StateSelectEntityToCopy(DrawingContext drawingContext) {
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
                //TODO Clone
                entity = BaseEntity.clone(entity);
                mDrawingContext.setEditingEntity(entity);
                changeState(new StateWaitForEntityPlacePoint(mDrawingContext), "Grabbed Entity");
            }
        }
    }
}
