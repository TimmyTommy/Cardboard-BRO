package de.tinf13aibi.cardboardbro.Geometry;

import de.tinf13aibi.cardboardbro.Entities.IEntity;

/**
 * Created by dthom on 07.01.2016.
 */
public class CollisionPlanePoint extends CollisionTrianglePoint {
    public CollisionPlanePoint(StraightLine straight, Plane plane){
        super(straight, plane, null);
        calcPlaneLineIntersection();
    }

    public void calcPlaneLineIntersection(){
        triangleNormal = VecMath.calcNormalVector(triangle);
        float[] trsDummy = new float[3];
        float[] pos = new float[3];
        if (VecMath.calcPlaneLineIntersection(pos, trsDummy, triangle, straight)){
            collisionPos = new Vec3d(pos);
            calcDistance();
        } else {
            collisionPos = null;
            distance = -1;
        }
    }

}
