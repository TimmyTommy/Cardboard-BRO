package de.tinf13aibi.cardboardbro.Entities;

/**
 * Created by dth on 01.12.2015.
 */
public class ButtonEntity extends BaseEntity implements IEntity {
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        super.draw(view, perspective, lightPosInEyeSpace);
    }

    public ButtonEntity(int vertexShader, int fragmentShader){
        super(vertexShader, fragmentShader);
        fillBuffers(GeometryDatabase.CUBE_COORDS, GeometryDatabase.CUBE_NORMALS, GeometryDatabase.CUBE_COLORS);
    }
}