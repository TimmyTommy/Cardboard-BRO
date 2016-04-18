package de.tinf13aibi.cardboardbro.Engine;

import android.opengl.Matrix;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.TextEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 15.04.2016.
 */
public class StateWaitForTextPlacePoint extends StateBase implements IState {
    public StateWaitForTextPlacePoint(DrawingContext drawingContext) {
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

        drawTextEntityOnPosition(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal(), false);
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction) {
            case DoStateBack: placeTextAbort(); break;
            case DoEndSelect: drawTextEntityOnPosition(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal(), true);
        }
    }

    private void placeTextAbort() {
        if (mDrawingContext.getEditingEntity() instanceof TextEntity) {
            mDrawing.getEntityList().remove(mDrawingContext.getEditingEntity());
        }
        changeState(new StateWaitForTextInput(mDrawingContext), "Abort Text");
        mDrawingContext.setEditingEntity(null);
        mDrawing.setTempWorkingPlane(null);
    }

    private void drawTextEntityOnPosition(Vec3d pos, Vec3d baseNormal, Boolean fix) {
        if (mDrawingContext.getEditingEntity() instanceof TextEntity) {
            TextEntity textEntity = (TextEntity) mDrawingContext.getEditingEntity();
//            Vec3d firstDir = new Vec3d();
//            Vec3d secondDir = new Vec3d();
//            VecMath.calcCrossedVectorsFromNormal(secondDir, firstDir, baseNormal.copy());

            //float[] mat = new float[16];
            ////Matrix.setLookAtM(mat, 0, pos.x, pos.y, pos.z, pos.x+baseNormal.x, pos.y+baseNormal.y, pos.z+baseNormal.z, firstDir.x, firstDir.y, firstDir.z);
            //Matrix.setLookAtM(mat, 0, 0, 0, 0, baseNormal.x, baseNormal.y, -baseNormal.z, -firstDir.x, -firstDir.y, -firstDir.z);
            //textEntity.updatePosition(mat, pos);
            //textEntity.updatePosition(mat, new Vec3d());

            textEntity.updatePosition(mUser.getInvHeadView(), pos);
            if (fix) {
                mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(pos, baseNormal));
                changeState(new StateWaitForTextSizePoint(mDrawingContext), "Keyboard Input");
            }
        }
    }
}
