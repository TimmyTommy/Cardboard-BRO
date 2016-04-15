package de.tinf13aibi.cardboardbro.Entities;

import android.opengl.Matrix;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Engine.Constants;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 05.01.2016.
 */
public class ButtonSet {
    private int mRotationPos = 0;
    private Boolean mRotationDir = true;

    private ArrayList<IEntity> mButtonEntities = new ArrayList<>();

    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        for (int i = 0; i < mButtonEntities.size(); i++) {
            mButtonEntities.get(i).draw(view, perspective, lightPosInEyeSpace);
        }
    }

    public void addButton(ButtonEntity buttonEntity){
        mButtonEntities.add(buttonEntity);
    }

    public void rotateStep(){
        //Test: rotiere Buttons
        if (mRotationPos < -3){
            mRotationDir = true;
        } else if (mRotationPos > 3) {
            mRotationDir = false;
        }
        int direction = mRotationDir ? 1 : -1;
        mRotationPos += direction;
        for (int i = 0; i < mButtonEntities.size(); i++) {
            IEntity entity = mButtonEntities.get(i);
            if (entity.getDisplayType() == EntityDisplayType.RelativeToCamera) {
                Matrix.rotateM(entity.getBaseModel(), 0, Constants.TIME_DELTA * 4, 0f, 0f, direction);
            }
        }
    }

    public void setButtonsRelativeToCamera(float[] userHeadView, Vec3d userPosition){
        float[] finalOp = new float[16];
        float[] anOp = new float[16];
        Matrix.setIdentityM(finalOp, 0);

//                Matrix.setIdentityM(anOp, 0);
//                Matrix.scaleM(anOp, 0, 0.01f, 0.01f, 0.01f);
//                Matrix.multiplyMM(finalOp, 0, anOp, 0, finalOp, 0);

//                Matrix.setIdentityM(anOp, 0);
//                Matrix.translateM(anOp, 0, 0, 0, -0.50f);
//                Matrix.multiplyMM(finalOp, 0, anOp, 0, finalOp, 0);

//                Matrix.setIdentityM(anOp, 0);
//                Matrix.multiplyMM(anOp, 0, mUser.getInvHeadView(), 0, anOp, 0);
//                Matrix.multiplyMM(finalOp, 0, anOp, 0, finalOp, 0);

//                float[] quat = new float[4];
//                headTransform.getQuaternion(quat, 0);
//                for (int k=0; k<3; k++){
//                    quat[k] *= -1;
//                }
//                float[] rotMat = VecMath.calcQuaternionToMatrix(quat);
//                Matrix.multiplyMM(finalOp, 0, rotMat, 0, finalOp, 0);

        Matrix.multiplyMM(finalOp, 0, userHeadView, 0, finalOp, 0);

        Matrix.setIdentityM(anOp, 0);
        Matrix.translateM(anOp, 0, userPosition.x, userPosition.y, userPosition.z);
        Matrix.multiplyMM(finalOp, 0, anOp, 0, finalOp, 0);
        for (int i = 0; i < mButtonEntities.size(); i++) {
            IEntity entity = mButtonEntities.get(i);
            //if (entity.getDisplayType() == EntityDisplayType.RelativeToCamera) {
                entity.resetModelToBase();
                Matrix.multiplyMM(entity.getModel(), 0, finalOp, 0, entity.getModel(), 0);
                entity.changedModel();
            //}
        }
    }

    public ArrayList<IEntity> getButtonEntities() {
        return mButtonEntities;
    }
}
