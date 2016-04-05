package de.tinf13aibi.cardboardbro.Geometry.Intersection;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Straight;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 17.12.2015.
 */
public class InsersectionEntity {
    public Straight straight;
    public ITriangulatedEntity entity;
    public IntersectionTriangle nearestIntersection;

    public ArrayList<IntersectionTriangle> intersectionPoints = new ArrayList<>();

    public InsersectionEntity(Straight straight, ITriangulatedEntity entity){
        this.straight = straight;
        this.entity = entity;
        calcTriangleIntersections();
        calcNearestIntersection();
    }

    private void calcNearestIntersection(){
        if (intersectionPoints.size()>0){
            float minDis = intersectionPoints.get(0).distance;
            nearestIntersection = intersectionPoints.get(0);
            for (IntersectionTriangle intersectionPoint : intersectionPoints) {
                if (intersectionPoint.distance<minDis){
                    minDis = intersectionPoint.distance;
                    nearestIntersection = intersectionPoint;
                }
            }
        } else {
            nearestIntersection = null;
        }
    }

    private void calcTriangleIntersections(){
        ArrayList<Triangle> triangles = entity.getAbsoluteTriangles();
        for (Triangle triangle : triangles) {
            float angle = VecMath.calcAngleBetweenVecsDeg(triangle.getN1(), VecMath.calcVecTimesScalar(straight.dir, -1));
            //if (angle<90) { //TODO Nochmal drüber schauen: Manche Entities haben evtl falsche Normalen
                IntersectionTriangle intersectionPoint = new IntersectionTriangle(straight, triangle, entity);
                intersectionPoint.calcTriangleLineIntersection();
                if (intersectionPoint.intersectionPos != null) {
                    intersectionPoints.add(intersectionPoint);
                }
            //}
        }
    }
}
