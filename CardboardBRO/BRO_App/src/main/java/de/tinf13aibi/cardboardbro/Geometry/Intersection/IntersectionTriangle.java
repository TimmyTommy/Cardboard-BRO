package de.tinf13aibi.cardboardbro.Geometry.Intersection;

import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Straight;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 17.12.2015.
 */
public class IntersectionTriangle {
    public Triangle triangle;
    public Vec3d triangleNormal;
    public Straight straight;
    public Vec3d intersectionPos;
    public IEntity entity;
    public float distance = -1;
    public IntersectionTriangle(Straight straight, Triangle triangle, IEntity entity){
        this.straight = straight;
        this.triangle = triangle;
        this.entity = entity;
    }

    protected void calcDistance(){
        Vec3d vec = VecMath.calcVecMinusVec(intersectionPos, straight.pos);
        distance = VecMath.calcVectorLength(vec);
    }

    public Boolean calcTriangleLineIntersection(){
//        triangleNormal = VecMath.calcNormalVector(triangle);
        triangleNormal = triangle.getN1().copy();
        float[] pos = new float[3];
        if (VecMath.calcTriangleLineIntersection(pos, triangle, straight)){
            intersectionPos = new Vec3d(pos);
            calcDistance();
            return true;
        } else {
            intersectionPos = null;
            distance = -1;
            return false;
        }
    }
}
