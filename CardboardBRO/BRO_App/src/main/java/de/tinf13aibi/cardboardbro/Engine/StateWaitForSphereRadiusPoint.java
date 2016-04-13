package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Triangulated.SphereEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForSphereRadiusPoint extends StateBase implements IState {
    public StateWaitForSphereRadiusPoint(DrawingContext drawingContext){
        super(drawingContext);
    }

    @Override
    public void processOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        super.processOnDrawEye(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void processOnNewFrame(float[] headView, Vec3d armForwardVec) {
        super.processOnNewFrame(headView, armForwardVec);
        mUser.calcArmPointingAt(mDrawing.getTempWorkingPlane());
        drawSphereEndRadius(mUser.getArmCrosshair().getPosition(), false);
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction){
            case DoEndSelect: drawSphereEndRadius(mUser.getArmCrosshair().getPosition(), true); break;
            case DoStateBack: drawSphereAbort(false); break;
        }
    }

    private void drawSphereEndRadius(Vec3d point, Boolean fix){
        if (mDrawingContext.getEditingEntity() instanceof SphereEntity) {
            SphereEntity sphereEntity = (SphereEntity) mDrawingContext.getEditingEntity();
            sphereEntity.setRadius(VecMath.calcVectorLength(VecMath.calcVecMinusVec(point, sphereEntity.getCenter())), fix);
            if (fix){
                mDrawing.setTempWorkingPlane(null);
                changeState(new StateWaitForSphereCenterPoint(mDrawingContext), "End Sphere Radius");
            }
        }
    }

    private void drawSphereAbort(Boolean leave){
        if (leave){
            changeState(new StateSelectEntityToCreate(mDrawingContext), "Leave Sphere Mode");
        } else {
            if (mDrawingContext.getEditingEntity() instanceof SphereEntity) {
                mDrawing.getEntityList().remove(mDrawingContext.getEditingEntity());
            }
            changeState(new StateWaitForSphereCenterPoint(mDrawingContext), "Delete Drawn Sphere");
        }
        mDrawingContext.setEditingEntity(null);
        mDrawing.setTempWorkingPlane(null);
    }
}
