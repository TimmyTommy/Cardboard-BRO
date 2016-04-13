package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Triangulated.CuboidEntity;
import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionPlane;
import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionTriangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForCuboidBasePoint2 extends StateBase implements IState {
    public StateWaitForCuboidBasePoint2(DrawingContext drawingContext){
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
        drawCuboidBeginBasePoint2(mUser.getArmPointingAt(), false);
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction){
            case DoEndSelect: drawCuboidBeginBasePoint2(mUser.getArmPointingAt(), true); break;
            case DoStateBack: drawCuboidAbort(false); break;
        }
    }

    private void drawCuboidBeginBasePoint2(IntersectionTriangle intersectionPoint, Boolean fix){
        if (mDrawingContext.getEditingEntity() instanceof CuboidEntity && intersectionPoint instanceof IntersectionPlane) {
            CuboidEntity cuboidEntity = (CuboidEntity) mDrawingContext.getEditingEntity();
            Vec3d pos = ((IntersectionPlane) intersectionPoint).mTRS.copy();
            cuboidEntity.setDepthAndWidth(pos.y, pos.z, fix);
            if (fix){
                mOverlayView.show3DToast(String.format("Depth: %f; Width: %f", pos.y, pos.z));

                //TODO das ganze auslagern und mTempWorkingPlane updaten, je nachdem in welche richtung user guckt
                Vec3d baseNormal = cuboidEntity.getBaseNormal().copy();
                Vec3d depthDir = new Vec3d();
                Vec3d widthDir = new Vec3d();
                VecMath.calcCrossedVectorsFromNormal(widthDir, depthDir, baseNormal);

//            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cuboidEntity.getBaseVert(), depthDir);
//                mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(cuboidEntity.getBaseVert(), widthDir));
                mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(intersectionPoint.intersectionPos, widthDir));

                changeState(new StateWaitForCuboidHeightPoint(mDrawingContext), "Update Cuboid Point2");
            }
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
