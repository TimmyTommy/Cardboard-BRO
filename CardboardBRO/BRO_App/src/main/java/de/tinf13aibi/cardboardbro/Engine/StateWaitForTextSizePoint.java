package de.tinf13aibi.cardboardbro.Engine;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.TextEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 18.04.2016.
 */
public class StateWaitForTextSizePoint extends StateBase implements IState {
    public StateWaitForTextSizePoint(DrawingContext drawingContext) {
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
        drawTextEntitySized(mUser.getArmCrosshair().getPosition(), false);
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction) {
            case DoStateBack: sizeTextAbort();break;
            case DoEndSelect: drawTextEntitySized(mUser.getArmCrosshair().getPosition(), true);
        }
    }

    private void sizeTextAbort() {
        if (mDrawingContext.getEditingEntity() instanceof TextEntity) {
            mDrawing.getEntityList().remove(mDrawingContext.getEditingEntity());
        }
        changeState(new StateWaitForTextInput(mDrawingContext), "Abort Text Sizing");
        mDrawingContext.setEditingEntity(null);
        mDrawing.setTempWorkingPlane(null);
    }

    private void drawTextEntitySized(Vec3d point, Boolean fix) {
        if (mDrawingContext.getEditingEntity() instanceof TextEntity) {
            TextEntity textEntity = (TextEntity) mDrawingContext.getEditingEntity();
            Vec3d position = textEntity.getPosition();
            float size = VecMath.calcVectorLength(VecMath.calcVecMinusVec(position, point));
            textEntity.updateSize(size); //TODO
            //textEntity.updatePosition(mUser.getInvHeadView(), pos);
            if (fix) {
                changeState(new StateWaitForTextInput(mDrawingContext), "Keyboard Input");
                mDrawingContext.setEditingEntity(null);
                mDrawing.setTempWorkingPlane(null);
            }
        }
    }
}
