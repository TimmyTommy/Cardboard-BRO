package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.Lined.PolyLineEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Shader.Programs;
import de.tinf13aibi.cardboardbro.Shader.ShaderCollection;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForBeginPolyLinePoint extends StateBase implements IState {
    public StateWaitForBeginPolyLinePoint(DrawingContext drawingContext){
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
            case DoEndSelect: drawPolyLineBegin(mUser.getArmCrosshair().getPosition()); break;
            case DoStateBack: drawPolyLineLeave(); break;
        }
    }

    //Draw PolyLine
    private void drawPolyLineBegin(Vec3d point){
        mDrawingContext.setEditingEntity(new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram)));
        ((PolyLineEntity)mDrawingContext.getEditingEntity()).addVert(point);
        ((PolyLineEntity)mDrawingContext.getEditingEntity()).addVert(point);
        mDrawing.getEntityList().add(0, mDrawingContext.getEditingEntity()); //Linien zuerst zeichnen sonst unsichtbar
        changeState(new StateWaitForNextPolyLinePoint(mDrawingContext), "Begin PolyLine");
    }

    private void drawPolyLineLeave(){
        mDrawingContext.setEditingEntity(null);
        changeState(new StateSelectEntityToCreate(mDrawingContext), "Leave PolyLine Mode");
    }
}
