package de.tinf13aibi.cardboardbro.Geometry;

import de.tinf13aibi.cardboardbro.Entities.CylinderCanvasEntity;
import de.tinf13aibi.cardboardbro.Entities.IEntity;

/**
 * Created by dthom on 17.12.2015.
 */
public class CollisionTrianglePoint {
    public Triangle triangle;
    public Point3d triangleNormal;
    public StraightLine straight;
    public Point3d collisionPos;
    public IEntity entity;
    public float distance = -1;
    public int normalDir = 1;
    public CollisionTrianglePoint(StraightLine straight, Triangle triangle , IEntity entity){
        this.straight = straight;
        this.triangle = triangle;
        this.entity = entity;
        calcTriangleLineIntersection();
    }

    private void calcDistance(){
        float[] vec = GeometryFactory.calcVecMinusVec(collisionPos.toFloatArray(), straight.pos.toFloatArray());
        distance = GeometryFactory.calcVectorLength(vec);
    }

    private void calcTriangleLineIntersection(){
        normalDir = entity instanceof CylinderCanvasEntity ? -1 : 1;
        triangleNormal = new Point3d(GeometryFactory.calcNormalVector(triangle.toFloatArray(), normalDir));
        float[] pos = new float[3];
        if (GeometryFactory.calcTriangleLineIntersection(pos, triangle, straight)){
            collisionPos = new Point3d(pos);
            calcDistance();
        } else {
            collisionPos = null;
        }
    }
}
