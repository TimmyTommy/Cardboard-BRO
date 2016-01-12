package de.tinf13aibi.cardboardbro.Entities.Triangulated;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Geometry.GeometryDatabase;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;

/**
 * Created by dth on 27.11.2015.
 */
public class FloorEntity extends BaseEntity implements ITriangulatedEntity {
    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    public FloorEntity(int program){
        super(program);
        fillBuffers(GeometryDatabase.FLOOR_COORDS, GeometryDatabase.FLOOR_NORMALS, GeometryDatabase.FLOOR_COLORS);
        calcAbsoluteTriangles();
    }
}
