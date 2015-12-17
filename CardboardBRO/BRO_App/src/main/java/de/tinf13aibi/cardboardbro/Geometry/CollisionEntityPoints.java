package de.tinf13aibi.cardboardbro.Geometry;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.IEntity;

/**
 * Created by dthom on 17.12.2015.
 */
public class CollisionEntityPoints {
    public StraightLine straight;
    public IEntity entity;
    public CollisionTrianglePoint nearestCollision;

    public ArrayList<CollisionTrianglePoint> collisions = new ArrayList<>();

    public CollisionEntityPoints(StraightLine straight, IEntity entity){
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
            CollisionTrianglePoint collisionPoint = new CollisionTrianglePoint(straight, triangle, entity);
            if (collisionPoint.collisionPos!=null){
                collisions.add(collisionPoint);
            }
        }
    }
}
