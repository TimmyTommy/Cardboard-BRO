package de.tinf13aibi.cardboardbro.Engine;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.PolyLineEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.TextEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 15.04.2016.
 */
public class StateWaitForPlaceTextPoint extends StateBase implements IState {
    public StateWaitForPlaceTextPoint(DrawingContext drawingContext) {
        super(drawingContext);
    }

    @Override
    public void processOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        super.processOnDrawEye(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void processOnNewFrame(float[] headView, Vec3d armForwardVec) {
        super.processOnNewFrame(headView, armForwardVec);

        ArrayList<IEntity> entities = mDrawing.getEntityListWithFloorAndCanvas();
        entities.remove(mDrawingContext.getEditingEntity());
        mUser.calcArmPointingAt(entities);
        //mUser.calcArmPointingAt(mDrawing.getEntityListWithFloorAndCanvas());

        drawTextEntityOnPosition(mUser.getArmCrosshair().getPosition(), false);
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        drawTextEntityOnPosition(mUser.getArmCrosshair().getPosition(), true);
    }

    private void drawTextEntityOnPosition(Vec3d pos, Boolean fix) {
        if (mDrawingContext.getEditingEntity() instanceof TextEntity) {
            TextEntity textEntity = (TextEntity) mDrawingContext.getEditingEntity();
            textEntity.updatePosition(mUser.getInvHeadView(), pos);
            if (fix) {
                changeState(new StateWaitForKeyboardInput(mDrawingContext), "Keyboard Input");
            }
        }
    }
}
