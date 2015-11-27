package de.tinf13aibi.cardboardbro.Entities;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import de.tinf13aibi.cardboardbro.Constants;

/**
 * Created by dth on 27.11.2015.
 */
public class LineEntity extends BaseEntity implements IEntity {
    float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };
    static float[] LineCoords = {
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f
    };
    private final int mVerticesCount = LineCoords.length / Constants.COORDS_PER_VERTEX;

    public void setColor(float red, float green, float blue, float alpha) {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        color[3] = alpha;
    }

    @Override
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        float[] modelView = new float[16];
        float[] modelViewProjection = new float[16];

        Matrix.multiplyMM(modelView, 0, view, 0, mModel, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        GLES20.glUseProgram(mProgram);
        GLES20.glUniform4fv(mColorParam, 1, color, 0);
        GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(mPositionParam, Constants.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mVertices);

        GLES20.glDrawArrays(GLES20.GL_LINES, 0, mVerticesCount);
    }

    public LineEntity(int vertexShader, int fragmentShader){
        super();
        mProgram = createProgram(vertexShader, fragmentShader);
        fillParameters(mProgram);
        fillBufferVertices(LineCoords);
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
