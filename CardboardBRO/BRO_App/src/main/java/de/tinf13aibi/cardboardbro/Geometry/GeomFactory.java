package de.tinf13aibi.cardboardbro.Geometry;

import java.util.ArrayList;
import java.util.List;

import de.tinf13aibi.cardboardbro.Engine.Constants;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Line;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 07.01.2016.
 */
public class GeomFactory {
    private static Line calcCycleSegmentLine(float radius,float height, Vec3d cycleNormal, float[] angles, Boolean normalsInverse){
        Vec3d firstCycleDir = new Vec3d();
        Vec3d secondCycleDir = new Vec3d();
        VecMath.calcCrossedVectorsFromNormal(secondCycleDir, firstCycleDir, cycleNormal);
        Vec3d firstPointOfCycle = VecMath.calcVecTimesScalar(firstCycleDir, radius);
        Vec3d heightVec = VecMath.calcVecTimesScalar(cycleNormal, height);


        Vec3d p1 = VecMath.calcRotateVecAroundAxis(firstPointOfCycle, cycleNormal, angles[0]);
        Vec3d p2 = VecMath.calcRotateVecAroundAxis(firstPointOfCycle, cycleNormal, angles[1]);

        p1 = VecMath.calcVecPlusVec(p1, heightVec);
        p2 = VecMath.calcVecPlusVec(p2, heightVec);
        if (normalsInverse) {
            return new Line(p1, p2);
        } else {
            return new Line(p2, p1);
        }
    }

    private static ArrayList<Line> calcCycleSegmentLines(Vec3d center, float radius, float height, Vec3d cycleNormal, int segments, Boolean normalsInverse){
        ArrayList<Line> lines = new ArrayList<>();

        for (int i=0; i<segments; i++) {
            float[] angles = calcCycleSegmentAnglesDeg(segments, i);
            Line line = calcCycleSegmentLine(radius, height, cycleNormal, angles, normalsInverse);
            line.setP1(VecMath.calcVecPlusVec(line.getP1(), center));
            line.setP2(VecMath.calcVecPlusVec(line.getP2(), center));
            lines.add(line);
        }
        return lines;
    }

    private static float[] calcCycleSegmentAnglesRad(int cycleEdgesCount, int edgeIndex){
        float fromAngle = (float)(2*Math.PI*(1.0*edgeIndex/cycleEdgesCount));
        float toAngle = (float)(2*Math.PI*((1.0*edgeIndex+1)/cycleEdgesCount));
        float[] angles = new float[2];
        angles[0] = fromAngle;
        angles[1] = toAngle;
        return angles;
    }

    private static float[] calcCycleSegmentAnglesDeg(int cycleEdgesCount, int edgeIndex){
        float[] angles = calcCycleSegmentAnglesRad(cycleEdgesCount, edgeIndex);
        angles[0] = VecMath.radToDeg(angles[0]);
        angles[1] = VecMath.radToDeg(angles[1]);
        return angles;
    }

    private static ArrayList<Triangle> calcRectangularFace(Line segmentBottom, Vec3d heightVec){
        Line segmentTop = new Line(
            VecMath.calcVecPlusVec(segmentBottom.getP1(), heightVec),
            VecMath.calcVecPlusVec(segmentBottom.getP2(), heightVec)
        );
        return calcRectangularFace(segmentBottom, segmentTop);
    }

    private static ArrayList<Triangle> calcRectangularFace(Line segmentBottom, Line segmentTop){
        Triangle triangle1 = new Triangle(segmentTop.getP1(), segmentBottom.getP1(), segmentBottom.getP2());
        Triangle triangle2 = new Triangle(segmentBottom.getP2(), segmentTop.getP2(), segmentTop.getP1());

        ArrayList<Triangle> cylinderFace = new ArrayList<>();
        cylinderFace.add(triangle1);
        cylinderFace.add(triangle2);

        return cylinderFace;
    }

    private static ArrayList<Triangle> calcCylinderSegmentBottomAndTop(Vec3d center, Line cycleSegment, Vec3d heightVec){
        Triangle triangle1 = new Triangle(center, cycleSegment.getP2(), cycleSegment.getP1());
        Triangle triangle2 = new Triangle(
            VecMath.calcVecPlusVec(center, heightVec),
            VecMath.calcVecPlusVec(cycleSegment.getP1(), heightVec),
            VecMath.calcVecPlusVec(cycleSegment.getP2(), heightVec)
        );

        ArrayList<Triangle> cylinderBottomAndTop = new ArrayList<>();
        cylinderBottomAndTop.add(triangle1);
        cylinderBottomAndTop.add(triangle2);

        return cylinderBottomAndTop;
    }

    private static ArrayList<Triangle> calcCylinderSegment(Vec3d center, Vec3d heightVec, Line cycleSegment){
        ArrayList<Triangle> cylinderSegment = new ArrayList<>();

        cycleSegment.setP1(VecMath.calcVecPlusVec(cycleSegment.getP1(), center));
        cycleSegment.setP2(VecMath.calcVecPlusVec(cycleSegment.getP2(), center));

        cylinderSegment.addAll(calcRectangularFace(cycleSegment, heightVec));
        cylinderSegment.addAll(calcCylinderSegmentBottomAndTop(center, cycleSegment, heightVec));

        return  cylinderSegment;
    }

    private static ArrayList<Vec3d> calcNormalsOfTriangles(ArrayList<Triangle> triangleArray){
        ArrayList<Vec3d> normals = new ArrayList<>();
        for (int i = 0; i < triangleArray.size(); i++) {
//            Vec3d normal = VecMath.calcNormalVector(triangleArray.get(i));
            Vec3d normal = triangleArray.get(i).getN1();
            normals.add(normal.copy());
            normals.add(normal.copy());
            normals.add(normal.copy());
        }
        return normals;
    }

    private static float[] transformTrianglesToFloatArray(ArrayList<Triangle> triangleArray){
        float[] array = new float[triangleArray.size()*9];
        for (int i = 0; i < triangleArray.size(); i++) {
            System.arraycopy(triangleArray.get(i).toFloatArray(), 0, array, i * 9, 9);
        }
        return array;
    }

    private static ArrayList<Vec3d> transformTrianglesToVec3dList(ArrayList<Triangle> triangleArray){
        ArrayList<Vec3d> list = new ArrayList<>();
        for (int i = 0; i < triangleArray.size(); i++) {
            Triangle triangle = triangleArray.get(i);
            list.add(triangle.getP1().copy());
            list.add(triangle.getP2().copy());
            list.add(triangle.getP3().copy());
        }
        return list;
    }

    private static float[] transformVec3dListToFloatArray(ArrayList<Vec3d> list){
        float[] array = new float[list.size()*3];
        for (int i = 0; i < list.size(); i++) {
            System.arraycopy(list.get(i).toFloatArray(), 0, array, i * 3, 3);
        }
        return array;
    }

    public static GeometryStruct createCylinderGeom(Vec3d center, Vec3d baseNormal, float radius, float height, float[] color, Boolean normalsInverse){
        normalsInverse = normalsInverse ^ height<0;

        Vec3d heightVec = VecMath.calcVecTimesScalar(baseNormal, height);
        ArrayList<Triangle> cylinderTriangles = new ArrayList<>();
        for (int i=0; i<Constants.CYCLE_SEGMENTS; i++){
            float[] angles = calcCycleSegmentAnglesDeg(Constants.CYCLE_SEGMENTS, i);
            Line cycleSegment = calcCycleSegmentLine(radius, 0, baseNormal, angles, normalsInverse);
            ArrayList<Triangle> cylinderSegment = calcCylinderSegment(center, heightVec, cycleSegment);
            cylinderTriangles.addAll(cylinderSegment);
        }

        float[] vertices = transformTrianglesToFloatArray(cylinderTriangles);
        float[] normals = transformVec3dListToFloatArray(calcNormalsOfTriangles(cylinderTriangles));

        int verticesCount = vertices.length/3;
        float[] colors = new float[4*verticesCount];
        for (int i = 0; i < verticesCount; i++) {
            System.arraycopy(color, 0, colors, i*4, 4);
        }

        GeometryStruct result = new GeometryStruct();
        result.vertices = vertices;
        result.normals = normals;
        result.colors = colors;
        return result;
    }


    private static ArrayList<Line> calcBaseRectLines(Vec3d baseVert, Vec3d baseNormal, float depth, float width, Boolean normalsInverse){
        ArrayList<Line> lines = new ArrayList<>();

        Vec3d depthVec = new Vec3d();
        Vec3d widthVec = new Vec3d();
        VecMath.calcCrossedVectorsFromNormal(widthVec, depthVec, baseNormal);
        depthVec.assignPoint3d(VecMath.calcVecTimesScalar(depthVec, depth));
        widthVec.assignPoint3d(VecMath.calcVecTimesScalar(widthVec, width));

        Vec3d A = baseVert.copy();                          // ^       D --- c --- C
        Vec3d B = VecMath.calcVecPlusVec(A, widthVec);      // |     /           /
        Vec3d C = VecMath.calcVecPlusVec(B, depthVec);      // n   d   BASE    b
        Vec3d D = VecMath.calcVecPlusVec(A, depthVec);      // | /           /
                                                            // A --- a --- B
        if (!normalsInverse){
            lines.add(new Line(A, B));
            lines.add(new Line(B, C));
            lines.add(new Line(C, D));
            lines.add(new Line(D, A));
        } else {
            lines.add(new Line(A, D));
            lines.add(new Line(D, C));
            lines.add(new Line(C, B));
            lines.add(new Line(B, A));
        }
        return lines;
    }

    // ^       D --- c --- C
    // |     /           /
    // h   d   BASE    b
    // | /           /
    // A --- a --- B
    public static ArrayList<Triangle> calcCuboidBottomAndTop(ArrayList<Line> lines, Vec3d heightVec){
        ArrayList<Triangle> triangles = new ArrayList<>();
        Vec3d A = lines.get(0).getP1();
        Vec3d B = lines.get(0).getP2();
        Vec3d C = lines.get(1).getP2();
        Vec3d D = lines.get(2).getP2();

        Triangle bottom1 = new Triangle(B, A, C);
        Triangle bottom2 = new Triangle(D, C, A);

        Vec3d AT = VecMath.calcVecPlusVec(A, heightVec);
        Vec3d BT = VecMath.calcVecPlusVec(B, heightVec);
        Vec3d CT = VecMath.calcVecPlusVec(C, heightVec);
        Vec3d DT = VecMath.calcVecPlusVec(D, heightVec);

        Triangle top1 = new Triangle(AT, BT, CT);
        Triangle top2 = new Triangle(CT, DT, AT);

        triangles.add(bottom1);
        triangles.add(bottom2);
        triangles.add(top1);
        triangles.add(top2);

        return triangles;
    }

    public static GeometryStruct createCuboidGeom(Vec3d baseVert, Vec3d baseNormal, float depth, float width, float height, float[] color, Boolean normalsInverse){
        normalsInverse = depth<0 ^ width<0 ^ height<0 ^ normalsInverse;

        Vec3d heightVec = new Vec3d();
        heightVec.assignPoint3d(VecMath.calcVecTimesScalar(baseNormal, height));

        ArrayList<Line> baseRectLines = calcBaseRectLines(baseVert, baseNormal, depth, width, normalsInverse);
        ArrayList<Triangle> cuboidTriangles = new ArrayList<>();

        for (int i = 0; i < baseRectLines.size(); i++) {
            cuboidTriangles.addAll(calcRectangularFace(baseRectLines.get(i), heightVec));
        }
        cuboidTriangles.addAll(calcCuboidBottomAndTop(baseRectLines, heightVec));

        float[] vertices = transformTrianglesToFloatArray(cuboidTriangles);
        float[] normals = transformVec3dListToFloatArray(calcNormalsOfTriangles(cuboidTriangles));

        int verticesCount = vertices.length/3;

        float[] colors = new float[4*verticesCount];
        for (int i = 0; i < verticesCount; i++) {
            System.arraycopy(color, 0, colors, i*4, 4);
        }

        GeometryStruct result = new GeometryStruct();
        result.vertices = vertices;
        result.normals = normals;
        result.colors = colors;
        return result;
    }

    public static GeometryStruct createPlaneGeom(Vec3d point, Vec3d normal, float[] color){
        Vec3d depthDir = new Vec3d();
        Vec3d widthDir = new Vec3d();
        Vec3d heightDir = normal.copy();
        VecMath.calcCrossedVectorsFromNormal(widthDir, depthDir, heightDir);

        Vec3d diagonalVec = VecMath.calcVecPlusVec(widthDir, depthDir);
        Vec3d diagonalVecTimesRadius = VecMath.calcVecTimesScalar(diagonalVec, 500);
        Vec3d basePoint = VecMath.calcVecMinusVec(point, diagonalVecTimesRadius);

        ArrayList<Line> rectLines = calcBaseRectLines(basePoint, normal, 1000, 1000, false);

        Vec3d A = rectLines.get(0).getP1();
        Vec3d B = rectLines.get(0).getP2();
        Vec3d C = rectLines.get(1).getP2();
        Vec3d D = rectLines.get(2).getP2();

        Triangle top1 = new Triangle(A, B, C);
        Triangle top2 = new Triangle(C, D, A);

        ArrayList<Triangle> triangles = new ArrayList<>();
        triangles.add(top1);
        triangles.add(top2);

        float[] vertices = transformTrianglesToFloatArray(triangles);
        float[] normals = transformVec3dListToFloatArray(calcNormalsOfTriangles(triangles));

        int verticesCount = vertices.length/3;

        float[] colors = new float[4*verticesCount];
        for (int j = 0; j < verticesCount; j++) {
            System.arraycopy(color, 0, colors, j * 4, 4);
        }

        GeometryStruct result = new GeometryStruct();
        result.vertices = vertices;
        result.normals = normals;
        result.colors = colors;
        return result;
    }

    public static GeometryStruct createSphereGeom(Vec3d center, Vec3d baseNormal, float radius, float[] color, Boolean normalsInverse){
        ArrayList<Triangle> sphereTriangles = new ArrayList<>();

        Vec3d firstCycleDir = new Vec3d();
        Vec3d secondCycleDir = new Vec3d();
        VecMath.calcCrossedVectorsFromNormal(secondCycleDir, firstCycleDir, baseNormal);

        List<Line> linesTopDownHalfSphere = calcCycleSegmentLines(new Vec3d(), radius, 0, new Vec3d(1,0,0), Constants.HALFCYCLE_SEGMENTS*2, false).subList(0, Constants.HALFCYCLE_SEGMENTS);

        for (int i = 0; i < linesTopDownHalfSphere.size(); i++) {
//        for (int i = 1; i < linesTopDownHalfSphere.size()-1; i++) {
            Vec3d bottomFirstCyclePoint = linesTopDownHalfSphere.get(i).getP1();
            ArrayList<Line> bottomCycle = calcCycleSegmentLines(center, bottomFirstCyclePoint.z, bottomFirstCyclePoint.y, baseNormal, Constants.CYCLE_SEGMENTS, normalsInverse);

            Vec3d topFirstCyclePoint = linesTopDownHalfSphere.get(i).getP2();
            ArrayList<Line> topCycle = calcCycleSegmentLines(center, topFirstCyclePoint.z, topFirstCyclePoint.y, baseNormal, Constants.CYCLE_SEGMENTS, normalsInverse);

            for (int j = 0; j < bottomCycle.size(); j++) {
                Line bottomSegment = bottomCycle.get(j);
                Line topSegment = topCycle.get(j);
                if (i==0 || i==linesTopDownHalfSphere.size()){
                    sphereTriangles.add(calcRectangularFace(bottomSegment, topSegment).get(0));
                } else {
                    sphereTriangles.addAll(calcRectangularFace(bottomSegment, topSegment));
                }
            }
        }

        float[] vertices = transformTrianglesToFloatArray(sphereTriangles);
        float[] normals = transformVec3dListToFloatArray(calcNormalsOfTriangles(sphereTriangles));
        int verticesCount = vertices.length/3;

        float[] colors = new float[4*verticesCount];
        for (int j = 0; j < verticesCount; j++) {
            System.arraycopy(color, 0, colors, j * 4, 4);
        }

        GeometryStruct result = new GeometryStruct();
        result.vertices = vertices;
        result.normals = normals;
        result.colors = colors;
        return result;
    }
}
