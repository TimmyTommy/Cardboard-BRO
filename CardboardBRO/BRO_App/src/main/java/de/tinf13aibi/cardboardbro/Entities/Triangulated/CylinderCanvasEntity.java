package de.tinf13aibi.cardboardbro.Entities.Triangulated;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Engine.Constants;
import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Geometry.GeomFactory;
import de.tinf13aibi.cardboardbro.Geometry.GeometryDatabase;
import de.tinf13aibi.cardboardbro.Geometry.GeometryStruct;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dth on 27.11.2015.
 */
public class CylinderCanvasEntity extends BaseEntity implements ITriangulatedEntity {
    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    @Override
    public void setPositionAndOrientation(Vec3d position, Vec3d baseNormal, boolean fix) {
        //TODO
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
        CylinderCanvasEntity ent = new CylinderCanvasEntity(mProgram);
        return ent;
    }

    private void recreateGeometry(boolean fix){
        GeometryStruct geometry = GeomFactory.createCylinderGeom(new Vec3d(0, 0, 0), new Vec3d(0, 1, 0), Constants.CANVAS_CYL_RADIUS, Constants.CANVAS_CYL_HEIGHT, GeometryDatabase.CANVAS_CYL_COLOR, true);
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
        if (fix) {
            calcAbsoluteTriangles();
        }
    }

    public CylinderCanvasEntity(int program){
        super(program);
        recreateGeometry(true);
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

        recreateGeometry(true);
    }
}