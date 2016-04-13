package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Lined.PolyLineEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Shader.Programs;
import de.tinf13aibi.cardboardbro.Shader.ShaderCollection;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForBeginFreeLine extends StateBase implements IState {
    public StateWaitForBeginFreeLine(DrawingContext drawingContext){
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
            case DoBeginSelect: drawFreeLineBegin(mUser.getArmCrosshair().getPosition()); break;
            case DoStateBack: drawFreeLineAbort(true); break;
        }
    }

    private void drawFreeLineBegin(Vec3d point){
        mDrawingContext.setEditingEntity(new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram)));
        ((PolyLineEntity)mDrawingContext.getEditingEntity()).setColor(1,0,0,1);
        ((PolyLineEntity)mDrawingContext.getEditingEntity()).addVert(point);
        mDrawing.getEntityList().add(0, mDrawingContext.getEditingEntity()); //Linien zuerst zeichnen sonst unsichtbar
        changeState(new StateWaitForEndFreeLine(mDrawingContext), "Begin FreeDraw");
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
