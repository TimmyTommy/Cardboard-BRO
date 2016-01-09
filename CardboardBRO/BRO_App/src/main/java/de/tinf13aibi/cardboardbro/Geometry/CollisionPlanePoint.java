package de.tinf13aibi.cardboardbro.Geometry;

import de.tinf13aibi.cardboardbro.Entities.IEntity;

/**
 * Created by dthom on 07.01.2016.
 */
public class CollisionPlanePoint extends CollisionTrianglePoint {
    public Vec3d mTRS = new Vec3d();

    public CollisionPlanePoint(StraightLine straight, Plane plane){
        super(straight, plane, null);
        calcPlaneLineIntersection();
    }

    public void calcPlaneLineIntersection(){
        triangleNormal = VecMath.calcNormalVector(triangle);
        float[] trs = new float[3];
        float[] pos = new float[3];
        if (VecMath.calcPlaneLineIntersection(pos, trs, triangle, straight)){
            collisionPos = new Vec3d(pos);
            mTRS.assignFloatArray(trs);
            distance = trs[0];
//            calcDistance();
        } else {
            collisionPos = null;
            distance = -1;
        }
    }

}
