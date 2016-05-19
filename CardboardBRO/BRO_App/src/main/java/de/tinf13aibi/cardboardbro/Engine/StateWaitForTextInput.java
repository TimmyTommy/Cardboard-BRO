package de.tinf13aibi.cardboardbro.Engine;

import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonSet;
import de.tinf13aibi.cardboardbro.Entities.Lined.TextEntity;
import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionTriangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 24.01.2016.
 */
public class StateWaitForTextInput extends StateBase implements IState {
    private String mText = "";

    public StateWaitForTextInput(DrawingContext drawingContext) {
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
            case DoStateBack: endTextInput(mText); break;//changeState(new StateSelectAction(mDrawingContext), "Go Back"); break;
            case DoEndSelect: selectKeyButton(mUser.getArmPointingAt()); break; //TODO select Key-Button
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

    private void endTextInput(String text){
        String trimmedText = text.trim();
        if (trimmedText.length()>0){
            createTextEntity(trimmedText);
            changeState(new StateWaitForTextPlacePoint(mDrawingContext), "Write Text");
        } else {
            changeState(new StateSelectEntityToCreate(mDrawingContext), "Leave Text Input Mode");
        }
    }

    private void createTextEntity(String text){
        mDrawingContext.setEditingEntity(new TextEntity(text, mDrawingContext.getUser().getInvHeadView().clone(), new Vec3d()));
        mDrawing.getEntityList().add(mDrawingContext.getEditingEntity());
    }
}
