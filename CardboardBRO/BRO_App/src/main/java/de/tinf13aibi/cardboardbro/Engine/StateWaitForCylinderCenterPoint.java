package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Triangulated.CylinderEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;
import de.tinf13aibi.cardboardbro.Shader.Programs;
import de.tinf13aibi.cardboardbro.Shader.ShaderCollection;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForCylinderCenterPoint extends StateBase implements IState {
    public StateWaitForCylinderCenterPoint(DrawingContext drawingContext){
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
            case DoEndSelect: drawCylinderBeginCenter(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal()); break;
            case DoStateBack: drawCylinderAbort(true); break;
        }
    }

    private void drawCylinderBeginCenter(Vec3d point, Vec3d baseNormal){
        mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(point, baseNormal));
        mDrawingContext.setEditingEntity(new CylinderEntity(ShaderCollection.getProgram(Programs.BodyProgram)));

        float[] color = new float[]{0.2f, 1f, 0.7f, 1};
        ((CylinderEntity)mDrawingContext.getEditingEntity()).setAttributes(point, baseNormal, 0.01f, 0.01f, color);
        mDrawing.getEntityList().add(mDrawingContext.getEditingEntity());

        changeState(new StateWaitForCylinderRadiusPoint(mDrawingContext), "Begin Draw Cylinder");
    }

    private void drawCylinderAbort(Boolean leave){
        if (leave){
            changeState(new StateSelectEntityToCreate(mDrawingContext), "Leave Cylinder Mode");
        } else {
            if (mDrawingContext.getEditingEntity() instanceof CylinderEntity) {
                mDrawing.getEntityList().remove(mDrawingContext.getEditingEntity());
            }
            changeState(new StateWaitForCylinderCenterPoint(mDrawingContext), "Delete Drawn Cylinder");
        }
        mDrawingContext.setEditingEntity(null);
        mDrawing.setTempWorkingPlane(null);
    }

}
