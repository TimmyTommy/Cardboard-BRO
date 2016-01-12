package de.tinf13aibi.cardboardbro.Geometry;

import android.content.Entity;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.IEntity;
import de.tinf13aibi.cardboardbro.Entities.ITriangulatedEntity;

/**
 * Created by dthom on 17.12.2015.
 */
public class CollisionEntityPoints {
    public StraightLine straight;
    public ITriangulatedEntity entity;
    public CollisionTrianglePoint nearestCollision;

    public ArrayList<CollisionTrianglePoint> collisions = new ArrayList<>();

    public CollisionEntityPoints(StraightLine straight, ITriangulatedEntity entity){
        this.straight = straight;
        this.entity = entity;
        calcTriangleCollisions();
        calcNearestCollision();
    }

    private void calcNearestCollision(){
        if (collisions.size()>0){
            float minDis = collisions.get(0).distance;
            nearestCollision = collisions.get(0);
            for (CollisionTrianglePoint collision : collisions) {
                if (collision.distance<minDis){
                    minDis = collision.distance;
                    nearestCollision = collision;
                }
            }
        } else {
            nearestCollision = null;
        }
    }

    private void calcTriangleCollisions(){
        ArrayList<Triangle> triangles = entity.getAbsoluteTriangles();
        for (Triangle triangle : triangles) {
//            float angle = VecMath.calcAngleBetweenVecsDeg(triangle.n1, straight.dir);
//            Vec3d normal = VecMath.calcNormalVector(triangle);
////            float angle = VecMath.calcAngleBetweenVecsDeg(normal, straight.dir);
            float angle = VecMath.calcAngleBetweenVecsDeg(triangle.n1, VecMath.calcVecTimesScalar(straight.dir, -1));
            if (angle<90) {
                CollisionTrianglePoint collisionPoint = new CollisionTrianglePoint(straight, triangle, entity);
                collisionPoint.calcTriangleLineIntersection();
                if (collisionPoint.collisionPos != null) {
                    collisions.add(collisionPoint);
                }
            }
        }
    }
}
