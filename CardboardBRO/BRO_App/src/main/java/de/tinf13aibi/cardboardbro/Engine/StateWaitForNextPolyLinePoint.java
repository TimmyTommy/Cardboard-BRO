package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Lined.PolyLineEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForNextPolyLinePoint extends StateBase implements IState {
    public StateWaitForNextPolyLinePoint(DrawingContext drawingContext){
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
        drawPolyLineNextPoint(mUser.getArmCrosshair().getPosition(), false);
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction) {
            case DoEndSelect: drawPolyLineNextPoint(mUser.getArmCrosshair().getPosition(), true); break;
            case DoStateBack: drawPolyLineEnd(); break;
        }
    }

    private void drawPolyLineNextPoint(Vec3d point, Boolean fix){
        if (mDrawingContext.getEditingEntity() instanceof PolyLineEntity) {
            PolyLineEntity polyLineEntity = (PolyLineEntity) mDrawingContext.getEditingEntity();
            polyLineEntity.changeLastVert(point);
            if (fix) {
                polyLineEntity.addVert(point);
                changeState(new StateWaitForNextPolyLinePoint(mDrawingContext), "Update PolyLine");
            }
        }
    }

    private void drawPolyLineEnd(){
        if (mDrawingContext.getEditingEntity() instanceof PolyLineEntity) {
            ((PolyLineEntity)mDrawingContext.getEditingEntity()).delLastVert();
        }
        changeState(new StateWaitForBeginPolyLinePoint(mDrawingContext), "End PolyLine");
    }
}
