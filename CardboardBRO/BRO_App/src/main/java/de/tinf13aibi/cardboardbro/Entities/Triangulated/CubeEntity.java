package de.tinf13aibi.cardboardbro.Entities.Triangulated;

import android.opengl.Matrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Geometry.GeometryDatabase;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dth on 27.11.2015.
 */
public class CubeEntity extends BaseEntity implements ITriangulatedEntity {
    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    @Override
    public void setPositionAndOrientation(Vec3d position, Vec3d baseNormal, boolean fix) {
        //TODO
        Matrix.setIdentityM(mModel, 0);
        Matrix.translateM(mModel, 0, position.x, position.y, position.z);
        if (fix) {
            calcAbsoluteTriangles();
        }
    }

    @Override
    public Vec3d getBaseVert() {
        return new Vec3d();
    }

    @Override
    public Vec3d getBaseNormal() {
        return new Vec3d();
    }

    @Override
    public ITriangulatedEntity clone() {
        CubeEntity ent = new CubeEntity(mProgram);
        ent.setPositionAndOrientation(getBaseVert(), getBaseNormal(), true);
        return ent;
    }

    public CubeEntity(int program){
        super(program);
        fillBuffers(GeometryDatabase.CUBE_COORDS, GeometryDatabase.CUBE_NORMALS, GeometryDatabase.CUBE_COLORS);
        calcAbsoluteTriangles();
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("class", this.getClass().toString());
        json.put("mModel", super.getModelToJson());
        json.put("mBaseModel", super.getBaseModelToJson());

        return json;
    }

    @Override
    public void loadFromJsonObject(JSONObject jsonEntity) throws JSONException {
        setModelFromJson(jsonEntity.optJSONArray("mModel"));
        setBaseModelFromJson(jsonEntity.optJSONArray("mBaseModel"));

        calcAbsoluteTriangles();
    }
}
