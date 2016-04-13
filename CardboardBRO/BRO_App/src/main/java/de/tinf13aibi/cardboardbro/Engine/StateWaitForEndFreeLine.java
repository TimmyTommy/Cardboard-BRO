package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Lined.PolyLineEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForEndFreeLine extends StateBase implements IState{
    public StateWaitForEndFreeLine(DrawingContext drawingContext){
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
        drawFreeLinePoint(mUser.getArmCrosshair().getPosition());
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction){
            case DoEndSelect: drawFreeLineEnd(mUser.getArmCrosshair().getPosition()); break;
            case DoStateBack: drawFreeLineAbort(false); break;
        }
    }

    private void drawFreeLinePoint(Vec3d point){
        if (mDrawingContext.getEditingEntity() instanceof PolyLineEntity) {
            ((PolyLineEntity)mDrawingContext.getEditingEntity()).addVert(point);
        }
    }

    private void drawFreeLineEnd(Vec3d point){
        drawFreeLinePoint(point);
        changeState(new StateWaitForBeginFreeLine(mDrawingContext), "End FreeDraw");
    }

    private void drawFreeLineAbort(Boolean leave){
        if (leave){
            changeState(new StateSelectEntityToCreate(mDrawingContext), "Leave FreeLine Mode");
        } else {
            if (mDrawingContext.getEditingEntity() instanceof PolyLineEntity) {
                mDrawing.getEntityList().remove(mDrawingContext.getEditingEntity());
            }
            changeState(new StateWaitForBeginFreeLine(mDrawingContext), "Delete Drawn FreeLine");
        }
        mDrawingContext.setEditingEntity(null);
    }
}
