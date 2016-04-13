package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Triangulated.CylinderEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForCylinderRadiusPoint extends StateBase implements IState {
    public StateWaitForCylinderRadiusPoint(DrawingContext drawingContext){
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
        drawCylinderRadius(mUser.getArmCrosshair().getPosition(), false);
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction){
            case DoEndSelect: drawCylinderRadius(mUser.getArmCrosshair().getPosition(), true); break;
            case DoStateBack: drawCylinderAbort(false); break;
        }
    }

    private void drawCylinderRadius(Vec3d point, Boolean fix){
        if (mDrawingContext.getEditingEntity() instanceof CylinderEntity) {
            CylinderEntity cylinderEntity = (CylinderEntity) mDrawingContext.getEditingEntity();
            cylinderEntity.setRadius(VecMath.calcVectorLength(VecMath.calcVecMinusVec(point, cylinderEntity.getCenter())), fix);
            if (fix){

                //TODO das ganze auslagern und mTempWorkingPlane updaten, je nachdem in welche richtung user guckt
                Vec3d baseNormal = cylinderEntity.getBaseNormal().copy();
                Vec3d firstCycleDir = new Vec3d();
                Vec3d secondCycleDir = new Vec3d();
                VecMath.calcCrossedVectorsFromNormal(secondCycleDir, firstCycleDir, baseNormal);

//            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cylinderEntity.getCenter(), firstCycleDir);
                mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(cylinderEntity.getCenter(), secondCycleDir));

                changeState(new StateWaitForCylinderHeightPoint(mDrawingContext), "Update Cylinder Radius");
            }
        }
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
