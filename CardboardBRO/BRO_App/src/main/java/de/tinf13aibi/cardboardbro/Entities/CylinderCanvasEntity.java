package de.tinf13aibi.cardboardbro.Entities;

/**
 * Created by dth on 27.11.2015.
 */
public class CylinderCanvasEntity extends BaseEntity implements IEntity {
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        super.draw(view, perspective, lightPosInEyeSpace);
    }

    public CylinderCanvasEntity(int vertexShader, int fragmentShader){
        super(vertexShader, fragmentShader);
        GeometryStruct geometry = GeometryFactory.createCylinderGeom(false);
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
    }
}