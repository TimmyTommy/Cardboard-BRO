package de.tinf13aibi.cardboardbro.Geometry;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.CubeEntity;
import de.tinf13aibi.cardboardbro.Entities.CuboidEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderCanvasEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderEntity;
import de.tinf13aibi.cardboardbro.Entities.FloorEntity;
import de.tinf13aibi.cardboardbro.Entities.IEntity;
import de.tinf13aibi.cardboardbro.Entities.IManySidedEntity;

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

    private Boolean isHitboxHit(StraightLine straight, CuboidEntity hitbox){
        CollisionEntityPoints collisionPoints = new CollisionEntityPoints(straight, hitbox);
        return collisionPoints.collisions.size() > 0;
    }

    private void doCalcEntityCollisions(StraightLine straight, IEntity entity){
        CollisionEntityPoints collisionPoints = new CollisionEntityPoints(straight, entity);
        if (collisionPoints.collisions.size() > 0) {
            entityCollisions.add(collisionPoints);
        }
    }

    private void calcEntityCollisions(){
        for (int i=0; i<entityList.size(); i++) {
            IEntity entity = entityList.get(i);
            if (isCollideEntity(entity)) {
                if (isManySidedEntity(entity)){
                    CuboidEntity hitbox = ((IManySidedEntity)entity).getHitBox();
                    if (isHitboxHit(straight, hitbox)){
                        doCalcEntityCollisions(straight, entity);
                    }
                } else {
                    doCalcEntityCollisions(straight, entity);
                }
            }
        }
    }

    private boolean isManySidedEntity(IEntity entity){
        if (entity instanceof IManySidedEntity){
            return true;
        } else {
            return false;
        }
//        return entity instanceof IManySidedEntity;
    }

    private boolean isCollideEntity(IEntity entity){ //TODO überdenken: vllt den Körper-Entities ein Attribut geben "mHasFaces" um nich immer ergänzen zu müssen
        return  entity instanceof FloorEntity ||
                entity instanceof CylinderCanvasEntity ||
                entity instanceof CylinderEntity ||
                entity instanceof CuboidEntity ||
                entity instanceof ButtonEntity || //Aktivieren, wenn Buttons "anzielbar" sein sollen
                entity instanceof CubeEntity;
    }

    public CollisionDrawingSpacePoints(StraightLine straight, ArrayList<IEntity> entityList){
        this.straight = straight;
        this.entityList = entityList;
        calcEntityCollisions();
        calcNearestCollision();
    }
}
