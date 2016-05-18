package de.tinf13aibi.cardboardbro.Entities.Triangulated;

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

    public CylinderCanvasEntity(int program){
        super(program);
        GeometryStruct geometry = GeomFactory.createCylinderGeom(new Vec3d(0, 0, 0), new Vec3d(0, 1, 0), Constants.CANVAS_CYL_RADIUS, Constants.CANVAS_CYL_HEIGHT, GeometryDatabase.CANVAS_CYL_COLOR, true);
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
        calcAbsoluteTriangles();
    }
}