package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonSet;
import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionTriangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForKeyboardInput extends StateBase implements IState {
    private String mText = "";

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
            case DoSelect: selectKeyButton(mUser.getArmPointingAt()); break; //TODO select Key-Button
        }
    }

    protected void selectKeyButton(IntersectionTriangle intersectionPoint){
        if (intersectionPoint!=null){
            if (intersectionPoint.entity != null){
                if (intersectionPoint.entity instanceof ButtonEntity) {
                    ButtonEntity buttonEntity = (ButtonEntity)intersectionPoint.entity;
                    char key = buttonEntity.getKey();
                    if (key!='\b') {
                        mText += buttonEntity.getKey();
                    } else {
                        if(mText.length()>0) {
                            mText = mText.substring(0, mText.length() - 1);
                        }
                    }
                    mDrawingContext.getMainActivity().getOverlayView().show3DToast(mText);
//                    changeState(buttonEntity.getNextState(mDrawingContext), "Change state"); //TODO button States ändern
                }
            }
        }
    }
}
