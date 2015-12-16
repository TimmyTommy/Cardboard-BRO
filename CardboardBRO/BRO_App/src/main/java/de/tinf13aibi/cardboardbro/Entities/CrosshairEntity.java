package de.tinf13aibi.cardboardbro.Entities;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Constants;

/**
 * Created by dthom on 16.12.2015.
 */
public class CrosshairEntity extends BaseEntity implements IEntity {
    private float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };
    private Point3d mPosition = new Point3d();
    private Point3d mNormal = new Point3d(0, 0, 1);
    private float mDistance = 0;

    private Point3d mVerticalVec = new Point3d();
    private Point3d mHoroizontalVec = new Point3d();

    private ArrayList<LineEntity> mLines = new ArrayList<>();

    @Override
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        for (LineEntity lineEntity : mLines) {
            lineEntity.draw(view, perspective, lightPosInEyeSpace);
        }
//        float[] modelView = new float[16];
//        float[] modelViewProjection = new float[16];
//
//        Matrix.multiplyMM(modelView, 0, view, 0, mModel, 0);
//        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
//
//        GLES20.glUseProgram(mProgram);
//        GLES20.glUniform4fv(mColorParam, 1, color, 0);
//        GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, modelViewProjection, 0);
//        GLES20.glVertexAttribPointer(mPositionParam, Constants.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mVertices);
//
//        GLES20.glDrawArrays(GLES20.GL_LINES, 0, mVerticesCount);
    }

    private void calcCrossVectors(Point3d normal){
        final float eps = 0.000001f;
        mHoroizontalVec.y = 0;
        if (Math.abs(normal.x)<eps){
            mHoroizontalVec.x = 1;
            mHoroizontalVec.z = 0;
        } else if (Math.abs(normal.z)<eps) {
            mHoroizontalVec.x = 0;
            mHoroizontalVec.z = 1;
        } else {
            mHoroizontalVec.x = 1;
            mHoroizontalVec.z = -normal.x*mHoroizontalVec.x/normal.z;
        }
        float[] crossProcuct = GeometryFactory.calcCrossProduct(normal.toFloatArray(), mHoroizontalVec.toFloatArray());
        mVerticalVec = new Point3d(crossProcuct);

        mHoroizontalVec = GeometryFactory.calcNormalizedVector(mHoroizontalVec);
        mVerticalVec = GeometryFactory.calcNormalizedVector(mVerticalVec);

//        Log.i("ergX", String.valueOf(mHoroizontalVec.x));
//        Log.i("ergY", String.valueOf(mHoroizontalVec.y));
//        Log.i("ergZ", String.valueOf(mHoroizontalVec.z));
    }

    private void calcCrossedLines(){
        mLines.get(0).setVerts(mPosition.toFloatArray(), GeometryFactory.calcVecPlusVec(mPosition.toFloatArray(), mHoroizontalVec.toFloatArray()));
        mLines.get(1).setVerts(mPosition.toFloatArray(), GeometryFactory.calcVecMinusVec(mPosition.toFloatArray(), mHoroizontalVec.toFloatArray()));
        mLines.get(2).setVerts(mPosition.toFloatArray(), GeometryFactory.calcVecPlusVec(mPosition.toFloatArray(), mVerticalVec.toFloatArray()));
        mLines.get(3).setVerts(mPosition.toFloatArray(), GeometryFactory.calcVecMinusVec(mPosition.toFloatArray(), mVerticalVec.toFloatArray()));
    }

    public void setPosition(Point3d position, Point3d normal, float distance) {
        mPosition = position;
        mNormal = normal;
        mDistance = distance;
        calcCrossVectors(normal);
        calcCrossedLines();
    }

    public CrosshairEntity(int vertexShader, int fragmentShader){
        super();
        mLines.add(new LineEntity(vertexShader, fragmentShader));
        mLines.add(new LineEntity(vertexShader, fragmentShader));
        mLines.add(new LineEntity(vertexShader, fragmentShader));
        mLines.add(new LineEntity(vertexShader, fragmentShader));
    }

    public void setColor(float red, float green, float blue, float alpha) {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        color[3] = alpha;
    }
}
