package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Triangulated.CylinderEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Plane;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForCylinderHeightPoint extends StateBase implements IState {
    public StateWaitForCylinderHeightPoint(DrawingContext drawingContext){
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
        drawCylinderEndHeight(mUser.getArmCrosshair().getPosition(), false);

//        CylinderEntity cylEnt = (CylinderEntity) mEntityList.get(mEntityList.size() - 1);
//        Vec3d cylCenter = cylEnt.getCenter();
//            Vec3d dir = VecMath.calcCrossProduct(new Vec3d(0, 1, 0), mUser.getArmForward());
//            Vec3d pnt = VecMath.calcVecPlusVec(new Vec3d(cylCenter.x, 0, cylCenter.z), dir);
//            Vec3d baseNormal = cylEnt.getBaseNormal().copy();
//            Vec3d firstCycleDir = new Vec3d();
//            Vec3d secondCycleDir = new Vec3d();
//            VecMath.calcCrossedVectorsFromNormal(secondCycleDir, firstCycleDir, baseNormal);
//            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cylCenter, firstCycleDir);
//            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cylCenter, mUser.getArmForward());
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction){
            case DoEndSelect: drawCylinderEndHeight(mUser.getArmCrosshair().getPosition(), true); break;
            case DoStateBack: drawCylinderAbort(false); break;
        }
    }

    private void drawCylinderEndHeight(Vec3d point, Boolean fix){
        if (mDrawingContext.getEditingEntity() instanceof CylinderEntity) {
            CylinderEntity cylinderEntity = (CylinderEntity) mDrawingContext.getEditingEntity();
            Plane basePlane = VecMath.calcPlaneFromPointAndNormal(cylinderEntity.getCenter(), cylinderEntity.getBaseNormal());

            float distance = VecMath.calcDistancePlanePoint(basePlane, point);
            cylinderEntity.setHeight(distance, fix);
        }
        if (fix){
            changeState(new StateWaitForCylinderCenterPoint(mDrawingContext), "End Draw Cylinder");
            mDrawing.setTempWorkingPlane(null);
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
