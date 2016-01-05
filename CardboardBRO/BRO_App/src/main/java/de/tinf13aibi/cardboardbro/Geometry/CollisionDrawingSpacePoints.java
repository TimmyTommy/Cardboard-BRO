package de.tinf13aibi.cardboardbro.Geometry;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.CuboidEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderCanvasEntity;
import de.tinf13aibi.cardboardbro.Entities.FloorEntity;
import de.tinf13aibi.cardboardbro.Entities.IEntity;

/**
 * Created by dthom on 17.12.2015.
 */
public class CollisionDrawingSpacePoints {
    public StraightLine straight;
    public ArrayList<IEntity> entityList;
    public CollisionTrianglePoint nearestCollision;
    public ArrayList<CollisionEntityPoints> entityCollisions = new ArrayList<>();

    private void calcNearestCollision(){
        if (entityCollisions.size()>0){
            float minDis = entityCollisions.get(0).nearestCollision.distance;
            nearestCollision = entityCollisions.get(0).nearestCollision;
            for (CollisionEntityPoints collision : entityCollisions) {
                CollisionTrianglePoint nearestEntityCollision = collision.nearestCollision;
                if (nearestEntityCollision.distance<minDis){
                    minDis = nearestEntityCollision.distance;
                    nearestCollision = nearestEntityCollision;
                }
            }
        } else {
            nearestCollision = null;
        }
    }

    private void calcEntityCollisions(){
        for (int i=0; i<entityList.size(); i++) {
            IEntity entity = entityList.get(i);
            if (isCollideEntity(entity)) {
                CollisionEntityPoints collisionPoints = new CollisionEntityPoints(straight, entity);
                if (collisionPoints.collisions.size() > 0) {
                    entityCollisions.add(collisionPoints);
                }
            }
        }
    }

    private boolean isCollideEntity(IEntity entity){
        return  entity instanceof FloorEntity ||
                entity instanceof CylinderCanvasEntity ||
                entity instanceof ButtonEntity || //Aktivieren, wenn Buttons "anzielbar" sein sollen
                entity instanceof CuboidEntity;
    }

    public CollisionDrawingSpacePoints(StraightLine straight, ArrayList<IEntity> entityList){
        this.straight = straight;
        this.entityList = entityList;
        calcEntityCollisions();
        calcNearestCollision();
    }
}
