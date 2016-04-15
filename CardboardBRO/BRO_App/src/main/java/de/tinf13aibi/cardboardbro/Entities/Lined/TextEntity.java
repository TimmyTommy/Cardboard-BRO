package de.tinf13aibi.cardboardbro.Entities.Lined;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Engine.Constants;
import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonSet;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Shader.Programs;
import de.tinf13aibi.cardboardbro.Shader.ShaderCollection;
import de.tinf13aibi.cardboardbro.Shader.Textures;

/**
 * Created by dthom on 15.04.2016.
 */
public class TextEntity extends BaseEntity implements IEntity {
    private String mText = "";
    private Vec3d mPosition = new Vec3d();
    private float[] mFacing = new float[16];

    private ArrayList<TextCharEntity> mTextCharArray = new ArrayList<>();

    private ButtonSet mCharSet = new ButtonSet();

    public void setColor(float red, float green, float blue, float alpha) {
        for (TextCharEntity entity : mTextCharArray) {
            entity.setColor(red, green, blue, alpha);
        }
    }

    @Override
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
//        for (TextCharEntity entity : mTextCharArray) {
//            entity.draw(view, perspective, lightPosInEyeSpace);
//        }
        mCharSet.draw(view, perspective, lightPosInEyeSpace);
    }

    public TextEntity(String text, float[] facing, Vec3d pos){
        super();
        mText = text;
        mFacing = facing;
        mPosition = pos;
        transformTextToTextCharEntities(text);
    }

    private void transformTextToTextCharEntities(String text){
//        mTextCharArray.clear();
//        for (int i=0; i<text.length(); i++) {
//            char c = text.charAt(i);
//            TextCharEntity entity;
//            entity = new TextCharEntity(mProgram, c);
//            mTextCharArray.add(entity);
//        }

        mCharSet = new ButtonSet();
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            Textures tex = Textures.parseValue(c);

            ButtonEntity entity = new ButtonEntity(ShaderCollection.getProgram(Programs.BodyTexturedProgram))
                    .setTextureHandle(ShaderCollection.getTexture(tex));

            float x = -2f*text.length()/2 + 2f * i;

            Matrix.translateM(entity.getBaseModel(), 0, x, 0, 0);

            if (tex != Textures.TextureNone) {
                mCharSet.addButton(entity);
            }
        }

        updatePosition(mFacing, mPosition);
    }

    public void updatePosition(float[] facing, Vec3d position){
        mCharSet.setButtonsRelativeToCamera(facing, position);
    }

    public Vec3d getPosition() {
        return mPosition;
    }

    public void setPosition(Vec3d position) {
        mPosition = position;
        updatePosition(mFacing, mPosition);
    }

    public float[] getFacing() {
        return mFacing;
    }

    public void setFacing(float[] facing) {
        mFacing = facing;
    }
}
