package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Triangulated.CuboidEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;
import de.tinf13aibi.cardboardbro.Shader.Programs;
import de.tinf13aibi.cardboardbro.Shader.ShaderCollection;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForCuboidBasePoint1 extends StateBase implements IState {
    public StateWaitForCuboidBasePoint1(DrawingContext drawingContext){
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
            case DoEndSelect: drawCuboidBeginBasePoint1(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal()); break;
            case DoStateBack: drawCuboidAbort(true); break;
        }
    }

    private void drawCuboidBeginBasePoint1(Vec3d point, Vec3d baseNormal){
        mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(point, baseNormal));
        mDrawingContext.setEditingEntity(new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram)));

        float[] color = new float[]{0.8f, 0.8f, 0.8f, 1};
        ((CuboidEntity) mDrawingContext.getEditingEntity()).setAttributes(point, baseNormal, 0.01f, 0.01f, 0.01f, color);
        mDrawing.getEntityList().add(mDrawingContext.getEditingEntity());

        changeState(new StateWaitForCuboidBasePoint2(mDrawingContext), "Begin Draw Cuboid");
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
