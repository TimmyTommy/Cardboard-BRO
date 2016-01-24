package de.tinf13aibi.cardboardbro.Geometry.Intersection;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IManySidedEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Entities.Triangulated.CuboidEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Straight;

/**
 * Created by dthom on 17.12.2015.
 */
public class IntersectionEntityList {
    public Straight straight;
    public ArrayList<IEntity> entityList;
    public IntersectionTriangle nearestIntersection;
    public ArrayList<InsersectionEntity> entityIntersections = new ArrayList<>();

    private void calcNearestIntersection(){
        if (entityIntersections.size()>0){
            float minDis = entityIntersections.get(0).nearestIntersection.distance;
            nearestIntersection = entityIntersections.get(0).nearestIntersection;
            for (InsersectionEntity intersectionEntity : entityIntersections) {
                IntersectionTriangle nearestEntityIntersection = intersectionEntity.nearestIntersection;
                if (nearestEntityIntersection.distance<minDis){
                    minDis = nearestEntityIntersection.distance;
                    nearestIntersection = nearestEntityIntersection;
                }
            }
        } else {
            nearestIntersection = null;
        }
    }

    private Boolean isHitboxHit(Straight straight, CuboidEntity hitbox){
        InsersectionEntity intersectionHitbox = new InsersectionEntity(straight, hitbox);
        return intersectionHitbox.intersectionPoints.size() > 0;
    }

    private void doCalcEntityIntersections(Straight straight, ITriangulatedEntity entity){
        InsersectionEntity intersectionEntity = new InsersectionEntity(straight, entity);
        if (intersectionEntity.intersectionPoints.size() > 0) {
            entityIntersections.add(intersectionEntity);
        }
    }

    private void calcEntityIntersections(){
        for (int i=0; i<entityList.size(); i++) {
            IEntity ent = entityList.get(i);
            if (isTriangulatedEntity(ent)) {
                ITriangulatedEntity entity = (ITriangulatedEntity)ent;
                if (isManySidedEntity(entity)){
                    CuboidEntity hitbox = ((IManySidedEntity)entity).getHitBox();
                    if (isHitboxHit(straight, hitbox)){
                        doCalcEntityIntersections(straight, entity);
                    }
                } else {
                    doCalcEntityIntersections(straight, entity);
                }
            }
        }
    }

    private boolean isManySidedEntity(IEntity entity){
        return entity instanceof IManySidedEntity;
    }

    private boolean isTriangulatedEntity(IEntity entity){
        return entity instanceof ITriangulatedEntity;
    }

    public IntersectionEntityList(Straight straight, ArrayList<IEntity> entityList){
        this.straight = straight;
        this.entityList = entityList;
        calcEntityIntersections();
        calcNearestIntersection();
    }
}
