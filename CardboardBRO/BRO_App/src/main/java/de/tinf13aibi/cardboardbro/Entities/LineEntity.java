package de.tinf13aibi.cardboardbro.Entities;

import android.opengl.GLES20;
import android.opengl.Matrix;

import de.tinf13aibi.cardboardbro.Constants;
import de.tinf13aibi.cardboardbro.Geometry.Vec3d;

/**
 * Created by dth on 27.11.2015.
 */
public class LineEntity extends BaseEntity implements IEntity {
    private float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };

    public void setColor(float red, float green, float blue, float alpha) {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        color[3] = alpha;
    }

    @Override
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        fillParameters(mProgram); //muss erstmal hier sein da draw() nur in onDrawEye() aufgerufen wird und somit GLES20-Context vorhanden ist

        float[] modelView = new float[16];
        float[] modelViewProjection = new float[16];

        Matrix.multiplyMM(modelView, 0, view, 0, mModel, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        GLES20.glUseProgram(mProgram);
        GLES20.glUniform4fv(mColorParam, 1, color, 0);
        GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(mPositionParam, Constants.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mVertices);
        GLES20.glLineWidth(3);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, mVerticesCount);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glLineWidth(1);
    }

    public void setVerts(Vec3d from, Vec3d to) {
        setVerts(from.toFloatArray(), to.toFloatArray());
    }

    public void setVerts(float[] from, float[] to) {
        System.arraycopy(from, 0, mCoords, 0, 3);
        System.arraycopy(to, 0, mCoords, 3, 3);
        fillBufferVertices(mCoords);
    }

    public LineEntity(int program){
        super();
        mCoords = new float[]{0.0f, 0.0f, 0.0f,
                              1.0f, 0.0f, 0.0f};
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
