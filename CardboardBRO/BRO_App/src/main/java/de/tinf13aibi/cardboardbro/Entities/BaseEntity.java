package de.tinf13aibi.cardboardbro.Entities;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import de.tinf13aibi.cardboardbro.Constants;

/**
 * Created by dth on 27.11.2015.
 */
public abstract class BaseEntity implements IEntity {
    protected int mVerticesCount;
    protected FloatBuffer mVertices;
    protected FloatBuffer mColors;
    protected FloatBuffer mNormals;
    protected FloatBuffer mFoundColors;

    protected int mProgram;

    protected int mPositionParam;
    protected int mNormalParam;
    protected int mColorParam;
    protected int mModelParam;
    protected int mModelViewParam;
    protected int mModelViewProjectionParam;
    protected int mLightPosParam;

    protected float[] mModel;

    public BaseEntity(){
        mModel = new float[16];
        Matrix.setIdentityM(mModel, 0);
    }

    public BaseEntity(int vertexShader, int fragmentShader){
        this();
        mProgram = createProgram(vertexShader, fragmentShader);
        fillParameters(mProgram);
    }

    public int createProgram(int vertexShader, int fragmentShader){
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
        return program;
    }

    private void fillParameters(int program){
        mPositionParam = GLES20.glGetAttribLocation(program, "a_Position");
        mNormalParam = GLES20.glGetAttribLocation(program, "a_Normal");
        mColorParam = GLES20.glGetAttribLocation(program, "a_Color");

        mModelParam = GLES20.glGetUniformLocation(program, "u_Model");
        mModelViewParam = GLES20.glGetUniformLocation(program, "u_MVMatrix");
        mModelViewProjectionParam = GLES20.glGetUniformLocation(program, "u_MVP");
        mLightPosParam = GLES20.glGetUniformLocation(program, "u_LightPos");

        GLES20.glEnableVertexAttribArray(mPositionParam);
        GLES20.glEnableVertexAttribArray(mNormalParam);
        GLES20.glEnableVertexAttribArray(mColorParam);
    }

    protected void fillBufferVertices(float[] coords){
        mVerticesCount = coords.length;
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(mVerticesCount * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        mVertices = bbVertices.asFloatBuffer();
        mVertices.put(coords);
        mVertices.position(0);
    }

    protected void fillBuffers(float[] coords, float[] normals, float[] colors){
        fillBufferVertices(coords);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(normals.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        mNormals = bbNormals.asFloatBuffer();
        mNormals.put(normals);
        mNormals.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(colors.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        mColors = bbColors.asFloatBuffer();
        mColors.put(colors);
        mColors.position(0);
    }

    public float[] getModel(){
        return mModel;
    }

    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        float[] modelView = new float[16];
        float[] modelViewProjection = new float[16];

        Matrix.multiplyMM(modelView, 0, view, 0, mModel, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        GLES20.glUseProgram(mProgram);
        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(mLightPosParam, 1, lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(mModelParam, 1, false, mModel, 0);
        GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(mPositionParam, Constants.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mVertices);
        GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT, false, 0, mNormals);
        GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false, 0, mColors);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVerticesCount/3);
    }
}
