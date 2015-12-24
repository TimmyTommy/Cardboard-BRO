package de.tinf13aibi.cardboardbro.Entities;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Geometry.GeometryFactory;
import de.tinf13aibi.cardboardbro.Geometry.Point3d;

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

    public Point3d getPosition(){
        return mPosition;
    }

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
        mHoroizontalVec = new Point3d(GeometryFactory.calcVecTimesScalar(mHoroizontalVec.toFloatArray(), mDistance/10));
        mVerticalVec = new Point3d(GeometryFactory.calcVecTimesScalar(mVerticalVec.toFloatArray(), mDistance/10));
        mNormal = new Point3d(GeometryFactory.calcVecTimesScalar(mNormal.toFloatArray(), mDistance/10));
//        Log.i("ergX", String.valueOf(mHoroizontalVec.x));
//        Log.i("ergY", String.valueOf(mHoroizontalVec.y));
//        Log.i("ergZ", String.valueOf(mHoroizontalVec.z));
    }

    private void calcCrossedLines(){
        LineEntity line;
        line = mLines.get(0);
        line.setVerts(mPosition.toFloatArray(), GeometryFactory.calcVecPlusVec(mPosition.toFloatArray(), mHoroizontalVec.toFloatArray()));
        line.setColor(1, 0, 0, 1);
        line = mLines.get(1);
        line.setVerts(mPosition.toFloatArray(), GeometryFactory.calcVecMinusVec(mPosition.toFloatArray(), mHoroizontalVec.toFloatArray()));
        line.setColor(1, 0, 0, 1);

        line = mLines.get(2);
        line.setVerts(mPosition.toFloatArray(), GeometryFactory.calcVecPlusVec(mPosition.toFloatArray(), mVerticalVec.toFloatArray()));
        line.setColor(0, 1, 0, 1);
        line = mLines.get(3);
        line.setVerts(mPosition.toFloatArray(), GeometryFactory.calcVecMinusVec(mPosition.toFloatArray(), mVerticalVec.toFloatArray()));
        line.setColor(0, 1, 0, 1);

        //Normale
        line = mLines.get(4);
        line.setVerts(mPosition.toFloatArray(), GeometryFactory.calcVecPlusVec(mPosition.toFloatArray(), mNormal.toFloatArray()));
        line.setColor(0, 0, 1, 1);
        line = mLines.get(5);
        line.setVerts(mPosition.toFloatArray(), GeometryFactory.calcVecMinusVec(mPosition.toFloatArray(), mNormal.toFloatArray()));
        line.setColor(0, 0, 1, 1);
    }

    public void setPosition(Point3d position, Point3d normal, float distance) {
//        mNormal = normal;
//        mPosition = position;
        mNormal = GeometryFactory.calcNormalizedVector(normal);
        Point3d translation = new Point3d(GeometryFactory.calcVecTimesScalar(mNormal.toFloatArray(), 0.0001f));
        mPosition = new Point3d(GeometryFactory.calcVecPlusVec(position.toFloatArray(), translation.toFloatArray()));
        mDistance = distance;
        calcCrossVectors(mNormal);
        calcCrossedLines();
    }

    public CrosshairEntity(int vertexShader, int fragmentShader){
        super();
        mLines.add(new LineEntity(vertexShader, fragmentShader));
        mLines.add(new LineEntity(vertexShader, fragmentShader));
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
