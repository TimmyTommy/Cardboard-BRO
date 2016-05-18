package de.tinf13aibi.cardboardbro.Entities.Triangulated;

import android.opengl.Matrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Geometry.GeomFactory;
import de.tinf13aibi.cardboardbro.Geometry.GeometryStruct;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 09.01.2016.
 */
public class CuboidEntity extends BaseEntity implements ITriangulatedEntity {
    private float[] mColor = new float[]{0, 0.7f, 0, 1};
    private Vec3d mBaseNormal = new Vec3d(0, 1, 0);
    private Vec3d mBaseVert = new Vec3d();

    private float mWidth = 1;
    private float mDepth = 1;
    private float mHeight = 1;

    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    @Override
    public void setPositionAndOrientation(Vec3d position, Vec3d baseNormal, boolean fix) {
        setBaseVert(position);
        mBaseNormal = baseNormal;
        recreateGeometry(fix);
    }

    private void recreateGeometry(boolean fix){
        GeometryStruct geometry = GeomFactory.createCuboidGeom(new Vec3d(0, 0, 0), mBaseNormal, mDepth, mWidth, mHeight, mColor, false);
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
        if (fix) {
            calcAbsoluteTriangles();
        }
    }

    public CuboidEntity(int program){
        super(program);
        recreateGeometry(true);
    }

    public float[] getColor() {
        return mColor;
    }

    public void setColor(float[] color) {
        mColor = color;
        recreateGeometry(true);
    }

    public float getDepth() {
        return mDepth;
    }

    public void setDepth(float depth) {
        mDepth = depth;
        recreateGeometry(true);
    }

    public float getWidth() {
        return mWidth;
    }

    public void setDepthAndWidth(float depth, float width, boolean fix) {
        mDepth = depth;
        mWidth = width;
        recreateGeometry(fix);
    }



    public void setWidth(float width) {
        mWidth = width;
        recreateGeometry(true);
    }

    public float getHeight() {
        return mHeight;
    }

    public void setHeight(float height, boolean fix) {
        mHeight = height;
        recreateGeometry(fix);
    }

    public Vec3d getBaseNormal() {
        return mBaseNormal;
    }

    @Override
    public ITriangulatedEntity clone() {
        CuboidEntity ent = new CuboidEntity(mProgram);
        ent.setAttributes(getBaseVert(), getBaseNormal(), getDepth(), getWidth(), getHeight(), getColor());
        return ent;
    }

    public void setBaseNormal(Vec3d baseNormal) {
        mBaseNormal = baseNormal;
        recreateGeometry(true);
    }

    public Vec3d getBaseVert() {
        return mBaseVert;
    }

    public void setBaseVert(Vec3d baseVert) {
        mBaseVert = baseVert;
        Matrix.setIdentityM(mModel, 0);
        Matrix.translateM(mModel, 0, mBaseVert.x, mBaseVert.y, mBaseVert.z);
    }

    public void setAttributes(Vec3d baseVert, Vec3d baseNormal, float depth, float width, float height, float[] color){
        setBaseVert(baseVert);
        mBaseNormal = baseNormal;
        mDepth = depth;
        mWidth = width;
        mHeight = height;
        mColor = color;
        recreateGeometry(true);
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("class", this.getClass().toString());
        json.put("mModel", super.getModelToJson());
        json.put("mBaseModel", super.getBaseModelToJson());

        json.put("mWidth", mWidth);
        json.put("mDepth", mDepth);
        json.put("mHeight", mHeight);
        json.put("mBaseVert", mBaseVert.toJsonArray());
        json.put("mBaseNormal", mBaseNormal.toJsonArray());
        json.put("mColor", new JSONArray(mColor));

        return json;
    }

    @Override
    public void loadFromJsonObject(JSONObject jsonEntity) throws JSONException {
        setModelFromJson(jsonEntity.optJSONArray("mModel"));
        setBaseModelFromJson(jsonEntity.optJSONArray("mBaseModel"));

        mWidth = (float)jsonEntity.optDouble("mWidth");
        mDepth = (float)jsonEntity.optDouble("mDepth");
        mHeight = (float)jsonEntity.optDouble("mHeight");

        mBaseVert = new Vec3d(jsonEntity.optJSONArray("mBaseVert"));
        mBaseNormal = new Vec3d(jsonEntity.optJSONArray("mBaseNormal"));

        JSONArray jsonColor = jsonEntity.optJSONArray("mColor");
        for (int i = 0; i < jsonColor.length(); i++) {
            mColor[i] = (float)jsonColor.optDouble(i);
        }

        recreateGeometry(true);
    }
}
