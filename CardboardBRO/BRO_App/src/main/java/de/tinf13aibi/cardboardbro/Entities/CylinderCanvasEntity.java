package de.tinf13aibi.cardboardbro.Entities;

import de.tinf13aibi.cardboardbro.Geometry.VecMath;
import de.tinf13aibi.cardboardbro.Geometry.GeometryStruct;

/**
 * Created by dth on 27.11.2015.
 */
public class CylinderCanvasEntity extends BaseEntity implements IEntity {
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        super.draw(view, perspective, lightPosInEyeSpace);
    }

    public CylinderCanvasEntity(int program){
        super(program);
        GeometryStruct geometry = VecMath.createCylinderGeom(true); //TODO Normalenberechnung nochmal angucken
        fillBuffers(geometry.vertices, geometry.normals, geometry.colors);
    }
}