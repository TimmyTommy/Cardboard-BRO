package de.tinf13aibi.cardboardbro.Engine;

import android.os.Vibrator;

import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Geometry.Intersection.IntersectionTriangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.UiMain.CardboardOverlayView;

/**
 * Created by dthom on 24.01.2016.
 */
public abstract class StateBase implements IState {
    protected Vibrator mVibrator;
    protected CardboardOverlayView mOverlayView;
    protected User mUser;
    protected Drawing mDrawing;
    protected DrawingContext mDrawingContext;

    public StateBase(DrawingContext drawingContext){
        mDrawingContext = drawingContext;
        mUser = mDrawingContext.getUser();
        mDrawing = mDrawingContext.getDrawing();

        mVibrator = mDrawingContext.getMainActivity().getVibrator();
        mOverlayView = mDrawingContext.getMainActivity().getOverlayView();
    }

    //Change States
    protected void selectButton(IntersectionTriangle intersectionPoint){
        if (intersectionPoint!=null){
            if (intersectionPoint.entity != null){
                if (intersectionPoint.entity instanceof ButtonEntity) {
                    ButtonEntity buttonEntity = (ButtonEntity)intersectionPoint.entity;
                    changeState(buttonEntity.getNextState(mDrawingContext), "Change state"); //TODO button States ändern
                }
            }
        }
    }

    protected void changeState(IState state, String message){ //TODO funktion abändern
        mDrawingContext.setState(state);
        mVibrator.vibrate(50);
//        mOverlayView.show3DToast(message + " -> " + state.getClass());
//        mAppState = appState;
    }

    @Override
    public void processUserMoving(Vec3d acceleration) {
        mUser.setAcceleration(acceleration);
        if (acceleration.getLength()>0) {
            mOverlayView.show3DToast("Accelerating");
        } else {
            mOverlayView.show3DToast("Slowing down");
        }
    }

    @Override
    public void processOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        //Linien zuerst zeichnen sonst teilweise unsichtbar
        mUser.drawCrosshairs(view, perspective, lightPosInEyeSpace);
        mDrawing.drawEntityList(view, perspective, lightPosInEyeSpace);
        mDrawing.drawTempWorkingPlane(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public abstract void processInputAction(InputAction inputAction);

    @Override
    public void processOnNewFrame(float[] headView, Vec3d armForwardVec){
        mUser.setHeadView(headView);
        mUser.setArmForward(armForwardVec);

        mUser.move();
        mUser.calcEyeLookingAt(mDrawing.getEntityListWithFloorAndCanvas());
    }
}
