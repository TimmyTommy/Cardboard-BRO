package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.ButtonSet;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateSelectAction extends StateBase implements IState {
    public StateSelectAction(DrawingContext drawingContext){
        super(drawingContext);
    }

    @Override
    public void processOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        super.processOnDrawEye(view, perspective, lightPosInEyeSpace);

        mDrawing.getEntityActionButtons().draw(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void processOnNewFrame(float[] headView, Vec3d armForwardVec) {
        super.processOnNewFrame(headView, armForwardVec);

        ButtonSet buttonSet = mDrawing.getEntityActionButtons();
        buttonSet.rotateStep();
        buttonSet.setButtonsRelativeToCamera(mUser.getInvHeadView(), mUser.getPosition());
        mUser.calcArmPointingAt(buttonSet.getButtonEntities());
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction){
            case DoStateBack: changeState(new StateSelectAction(mDrawingContext), "Go Back"); break;
            case DoEndSelect: selectButton(mUser.getArmPointingAt()); break;
        }
    }
}
