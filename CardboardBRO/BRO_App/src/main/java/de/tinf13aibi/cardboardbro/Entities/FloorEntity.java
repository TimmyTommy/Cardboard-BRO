package de.tinf13aibi.cardboardbro.Entities;

/**
 * Created by dth on 27.11.2015.
 */
public class FloorEntity extends BaseEntity implements IEntity {
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        super.draw(view, perspective, lightPosInEyeSpace);
    }

    public FloorEntity(int vertexShader, int fragmentShader){
        super(vertexShader, fragmentShader);
        fillBuffers(GeometryDatabase.FLOOR_COORDS, GeometryDatabase.FLOOR_NORMALS, GeometryDatabase.FLOOR_COLORS);
    }
}