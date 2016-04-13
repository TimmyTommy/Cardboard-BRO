package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Triangulated.SphereEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;
import de.tinf13aibi.cardboardbro.Shader.Programs;
import de.tinf13aibi.cardboardbro.Shader.ShaderCollection;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForSphereCenterPoint extends StateBase implements IState {
    public StateWaitForSphereCenterPoint(DrawingContext drawingContext){
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
        switch (inputAction){
            case DoEndSelect: drawSphereBeginCenter(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal()); break;
            case DoStateBack: drawSphereAbort(true); break;
        }
    }

    private void drawSphereBeginCenter(Vec3d point, Vec3d baseNormal){
        mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(point, baseNormal));
        mDrawingContext.setEditingEntity(new SphereEntity(ShaderCollection.getProgram(Programs.BodyProgram)));

        float[] color = new float[]{0.3f, 0.7f, 0.5f, 1};
        ((SphereEntity)mDrawingContext.getEditingEntity()).setAttributes(point, baseNormal, 0.01f, color);
        mDrawing.getEntityList().add(mDrawingContext.getEditingEntity());

        changeState(new StateWaitForSphereRadiusPoint(mDrawingContext), "Begin Draw Sphere");
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
