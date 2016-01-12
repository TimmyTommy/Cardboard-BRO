package de.tinf13aibi.cardboardbro.Entities;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Geometry.GeometryDatabase;
import de.tinf13aibi.cardboardbro.Geometry.Triangle;

/**
 * Created by dth on 27.11.2015.
 */
public class CubeEntity extends BaseEntity implements ITriangulatedEntity {
    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    public CubeEntity(int program){
        super(program);
        fillBuffers(GeometryDatabase.CUBE_COORDS, GeometryDatabase.CUBE_NORMALS, GeometryDatabase.CUBE_COLORS);
        calcAbsoluteTriangles();
    }
}
