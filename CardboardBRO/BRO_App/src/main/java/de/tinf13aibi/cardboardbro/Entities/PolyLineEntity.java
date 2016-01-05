package de.tinf13aibi.cardboardbro.Entities;

import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Constants;
import de.tinf13aibi.cardboardbro.Geometry.Vec3d;

/**
 * Created by Tommy on 02.01.2016.
 */
public class PolyLineEntity extends BaseEntity implements IEntity {
    private float mColor[] = { 0.0f, 1.0f, 0.0f, 1.0f };
    private ArrayList<Vec3d> mPolyLinePoints = new ArrayList<>();

    public void setColor(float red, float green, float blue, float alpha) {
        mColor[0] = red;
        mColor[1] = green;
        mColor[2] = blue;
        mColor[3] = alpha;
    }

    @Override
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        fillParameters(mProgram); //muss erstmal hier sein da draw() nur in onDrawEye() aufgerufen wird und somit GLES20-Context vorhanden ist

        float[] modelView = new float[16];
        float[] modelViewProjection = new float[16];

        Matrix.multiplyMM(modelView, 0, view, 0, mModel, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        GLES20.glUseProgram(mProgram);
        GLES20.glUniform4fv(mColorParam, 1, mColor, 0);
        GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(mPositionParam, Constants.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mVertices);
        GLES20.glLineWidth(5);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, mPolyLinePoints.size());
        GLES20.glLineWidth(1);
    }

    private float[] transformPointsToCoords(ArrayList<Vec3d> points){
        float[] pointCoords = new float[points.size()*3];
        for (int i=0; i<points.size(); i++){
            Vec3d point = points.get(i);
            System.arraycopy(point.toFloatArray(), 0, pointCoords, i*3, 3);
        }
        return pointCoords;
    }

    public void addVert(Vec3d point) {
        mPolyLinePoints.add(point.copy());
        mCoords = transformPointsToCoords(mPolyLinePoints);
        fillBufferVertices(mCoords);
    }

    public void delLastVert() {
        if (mPolyLinePoints.size()>0) {
            mPolyLinePoints.remove(mPolyLinePoints.size() - 1);
        }
        mCoords = transformPointsToCoords(mPolyLinePoints);
        fillBufferVertices(mCoords);
    }

    public void setLastVert(Vec3d point) {
        if (mPolyLinePoints.size()>1) {
            mPolyLinePoints.get(mPolyLinePoints.size() - 1).assignPoint3d(point);
        }
        mCoords = transformPointsToCoords(mPolyLinePoints);
        fillBufferVertices(mCoords);
    }


    public PolyLineEntity(int program){
        super();
        mProgram = program;
        fillBufferVertices(mCoords);
    }

    private void fillParameters(int program){
        mPositionParam = GLES20.glGetAttribLocation(program, "vPosition");
        mColorParam = GLES20.glGetUniformLocation(program, "vColor");
        mModelViewProjectionParam = GLES20.glGetUniformLocation(program, "uMVPMatrix");

        GLES20.glEnableVertexAttribArray(mPositionParam);

//        mPositionParam = GLES20.glGetAttribLocation(program, "a_Position");
//        mNormalParam = GLES20.glGetAttribLocation(program, "a_Normal");
//        mColorParam = GLES20.glGetAttribLocation(program, "a_Color");
//
//        mModelParam = GLES20.glGetUniformLocation(program, "u_Model");
//        mModelViewParam = GLES20.glGetUniformLocation(program, "u_MVMatrix");
//        mModelViewProjectionParam = GLES20.glGetUniformLocation(program, "u_MVP");
//        mLightPosParam = GLES20.glGetUniformLocation(program, "u_LightPos");
//
//        GLES20.glEnableVertexAttribArray(mPositionParam);
//        GLES20.glEnableVertexAttribArray(mNormalParam);
//        GLES20.glEnableVertexAttribArray(mColorParam);
    }
}
