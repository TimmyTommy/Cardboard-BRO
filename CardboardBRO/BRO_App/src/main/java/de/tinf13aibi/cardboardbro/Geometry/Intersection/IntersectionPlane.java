package de.tinf13aibi.cardboardbro.Geometry.Intersection;

import de.tinf13aibi.cardboardbro.Geometry.Simple.Plane;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Straight;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 07.01.2016.
 */
public class IntersectionPlane extends IntersectionTriangle {
    public Vec3d mTRS = new Vec3d();

    public IntersectionPlane(Straight straight, Plane plane){
        super(straight, plane, null);
        calcPlaneLineIntersection();
    }

    public void calcPlaneLineIntersection(){
//        triangleNormal = VecMath.calcNormalVector(triangle);
        intersectionPos = null;
        distance = -1;
        if (triangle != null) {
            triangleNormal = triangle.getN1().copy();
            float[] trs = new float[3];
            float[] pos = new float[3];
            if (VecMath.calcPlaneLineIntersection(pos, trs, triangle, straight)) {
                intersectionPos = new Vec3d(pos);
                mTRS.assignFloatArray(trs);
                distance = trs[0];
//            calcDistance();
            }
//            else {
//                intersectionPos = null;
//                distance = -1;
//            }
        }
    }

}
