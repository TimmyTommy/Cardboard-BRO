package de.tinf13aibi.cardboardbro.Entities;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Constants;
import de.tinf13aibi.cardboardbro.Geometry.GeomFactory;
import de.tinf13aibi.cardboardbro.Geometry.GeometryDatabase;
import de.tinf13aibi.cardboardbro.Geometry.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;
import de.tinf13aibi.cardboardbro.Geometry.GeometryStruct;

/**
 * Created by dth on 27.11.2015.
 */
public class CylinderCanvasEntity extends BaseEntity implements ITriangulatedEntity {
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        super.draw(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    public CylinderCanvasEntity(int program){
        super(program);
        GeometryStruct geometry = GeomFactory.createCylinderGeom(new Vec3d(0, 0, 0), new Vec3d(0, 1, 0), Constants.CANVAS_CYL_RADIUS, Constants.CANVAS_CYL_HEIGHT, GeometryDatabase.CANVAS_CYL_COLOR, true);
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
        calcAbsoluteTriangles();
    }
}