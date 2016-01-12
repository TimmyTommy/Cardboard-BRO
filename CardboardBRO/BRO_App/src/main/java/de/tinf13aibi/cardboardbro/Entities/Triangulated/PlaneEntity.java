package de.tinf13aibi.cardboardbro.Entities.Triangulated;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Engine.Constants;
import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Geometry.GeomFactory;
import de.tinf13aibi.cardboardbro.Geometry.GeometryStruct;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 10.01.2016.
 */
public class PlaneEntity extends BaseEntity implements ITriangulatedEntity {
    private float[] mColor = new float[]{0, 0.7f, 0, 1};
    private Vec3d mBaseNormal = new Vec3d(0, 1, 0);
    private Vec3d mCenter = new Vec3d();

    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        fillParameters(mProgram); //muss erstmal hier sein da draw() nur in onDrawEye() aufgerufen wird und somit GLES20-Context vorhanden ist

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

//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVerticesCount);

        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVerticesCount);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    public PlaneEntity(int program, Vec3d center, Vec3d baseNormal, float[] color){
        super(program);

        setCenter(center);
        mBaseNormal = baseNormal;
        mColor = color;

        GeometryStruct geometry = GeomFactory.createPlaneGeom(new Vec3d(0, 0, 0), mBaseNormal, mColor);
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
    }

    public void setCenter(Vec3d center) {
        mCenter = center;
        Matrix.setIdentityM(mModel, 0);
        Matrix.translateM(mModel, 0, mCenter.x, mCenter.y, mCenter.z);
    }
}
