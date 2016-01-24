package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.ButtonSet;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForKeyboardInput extends StateBase implements IState {
    public StateWaitForKeyboardInput(DrawingContext drawingContext) {
        super(drawingContext);
    }

    @Override
    public void processOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        super.processOnDrawEye(view, perspective, lightPosInEyeSpace);

        mDrawing.getKeyboardButtons().draw(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void processOnNewFrame(float[] headView, Vec3d armForwardVec) {
        super.processOnNewFrame(headView, armForwardVec);

        ButtonSet buttonSet = mDrawing.getKeyboardButtons();
        buttonSet.rotateStep();
        buttonSet.setButtonsRelativeToCamera(mUser.getInvHeadView(), mUser.getPosition());
        mUser.calcArmPointingAt(buttonSet.getButtonEntities());
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction) {
            case DoStateBack: changeState(new StateSelectAction(mDrawingContext), "Go Back"); break;
//            case DoSelect: selectButton(mUser.getArmPointingAt()); break; //TODO select Key-Button
        }
    }
}