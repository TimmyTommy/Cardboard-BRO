package de.tinf13aibi.cardboardbro.Entities.Triangulated;

import android.opengl.GLES20;
import android.opengl.Matrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private float[] mColor = new float[]{0, 1f, 0, 1};
    private Vec3d mBaseNormal = new Vec3d(0, 1, 0);
    private Vec3d mCenter = new Vec3d();

    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        GLES20.glUseProgram(mProgram);

        fillParameters(mProgram); //muss erstmal hier sein da draw() nur in onDrawEye() aufgerufen wird und somit GLES20-Context vorhanden ist

        float[] modelView = new float[16];
        float[] modelViewProjection = new float[16];

        Matrix.multiplyMM(modelView, 0, view, 0, mModel, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

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
    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("class", this.getClass().toString());
        json.put("mModel", super.getModelToJson());
        json.put("mBaseModel", super.getBaseModelToJson());

        json.put("mCenter", mCenter.toJsonArray());
        json.put("mBaseNormal", mBaseNormal.toJsonArray());
        json.put("mColor", new JSONArray(mColor));

        return json;
    }

    @Override
    public void loadFromJsonObject(JSONObject jsonEntity) throws JSONException {
        setModelFromJson(jsonEntity.optJSONArray("mModel"));
        setBaseModelFromJson(jsonEntity.optJSONArray("mBaseModel"));

        mCenter = new Vec3d(jsonEntity.optJSONArray("mCenter"));
        mBaseNormal = new Vec3d(jsonEntity.optJSONArray("mBaseNormal"));

        JSONArray jsonColor = jsonEntity.optJSONArray("mColor");
        for (int i = 0; i < jsonColor.length(); i++) {
            mColor[i] = (float)jsonColor.optDouble(i);
        }

        recreateGeometry();
    }

    private void recreateGeometry(){
        GeometryStruct geometry = GeomFactory.createPlaneGeom(new Vec3d(0, 0, 0), mBaseNormal, mColor);
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
    }

    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    @Override
    public void setPositionAndOrientation(Vec3d position, Vec3d baseNormal, boolean fix) {
        //TODO
        setCenter(position);
        mBaseNormal = baseNormal;
        recreateGeometry();
    }

    @Override
    public Vec3d getBaseVert() {
        return mCenter;
    }

    @Override
    public Vec3d getBaseNormal() {
        return mBaseNormal;
    }

    @Override
    public ITriangulatedEntity clone() {
        PlaneEntity ent = new PlaneEntity(mProgram, new Vec3d(0, 0, 0), getBaseNormal(), mColor);
        return ent;
    }

    public PlaneEntity(int program, Vec3d center, Vec3d baseNormal, float[] color){
        super(program);

        setCenter(center);
        mBaseNormal = baseNormal;
        mColor = color;

        recreateGeometry();
    }

    public void setCenter(Vec3d center) {
        mCenter = center;
        Matrix.setIdentityM(mModel, 0);
        Matrix.translateM(mModel, 0, mCenter.x, mCenter.y, mCenter.z);
    }
}
