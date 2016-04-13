package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Triangulated.CuboidEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Plane;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForCuboidHeightPoint extends StateBase implements IState {
    public StateWaitForCuboidHeightPoint(DrawingContext drawingContext){
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
        drawCuboidEndHeight(mUser.getArmCrosshair().getPosition(), false);
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction){
            case DoEndSelect: drawCuboidEndHeight(mUser.getArmCrosshair().getPosition(), true); break;
            case DoStateBack: drawCuboidAbort(false); break;
        }
    }

    private void drawCuboidEndHeight(Vec3d point, Boolean fix){
        if (mDrawingContext.getEditingEntity() instanceof CuboidEntity) {
            CuboidEntity cuboidEntity = (CuboidEntity) mDrawingContext.getEditingEntity();
            Plane basePlane = VecMath.calcPlaneFromPointAndNormal(cuboidEntity.getBaseVert(), cuboidEntity.getBaseNormal());

            float distance = VecMath.calcDistancePlanePoint(basePlane, point);
            cuboidEntity.setHeight(distance, fix);
        }
        if (fix){
            changeState(new StateWaitForCuboidBasePoint1(mDrawingContext), "End Draw Cuboid");
            mDrawing.setTempWorkingPlane(null);
        }
    }

    private void drawCuboidAbort(Boolean leave){
        if (leave){
            changeState(new StateSelectEntityToCreate(mDrawingContext), "Leave Cuboid Mode");
        } else {
            if (mDrawingContext.getEditingEntity() instanceof CuboidEntity) {
                mDrawing.getEntityList().remove(mDrawingContext.getEditingEntity());
            }
            changeState(new StateWaitForCuboidBasePoint1(mDrawingContext), "Delete Drawn Cuboid");
        }
        mDrawingContext.setEditingEntity(null);
        mDrawing.setTempWorkingPlane(null);
    }
}