package de.tinf13aibi.cardboardbro.Entities;

import de.tinf13aibi.cardboardbro.Geometry.GeometryDatabase;

/**
 * Created by dth on 27.11.2015.
 */
public class CubeEntity extends BaseEntity implements IEntity {
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        super.draw(view, perspective, lightPosInEyeSpace);
    }

    public CubeEntity(int program){
        super(program);
        fillBuffers(GeometryDatabase.CUBE_COORDS, GeometryDatabase.CUBE_NORMALS, GeometryDatabase.CUBE_COLORS);
    }
}