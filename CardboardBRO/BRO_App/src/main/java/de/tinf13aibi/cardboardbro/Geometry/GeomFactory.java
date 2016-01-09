package de.tinf13aibi.cardboardbro.Geometry;

import android.opengl.Matrix;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Constants;

/**
 * Created by dthom on 07.01.2016.
 */
public class GeomFactory {

    private static Line calcCycleSegmentLine(Vec3d firstPointOfCycle, Vec3d cycleNormal, float[] angles, Boolean normalsInverse){
        Vec3d p1 = VecMath.calcRotateVecAroundAxis(firstPointOfCycle, cycleNormal, angles[0]);
        Vec3d p2 = VecMath.calcRotateVecAroundAxis(firstPointOfCycle, cycleNormal, angles[1]);
        if (normalsInverse) {
            return new Line(p1, p2);
        } else {
            return new Line(p2, p1);
        }
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

    private static ArrayList<Triangle> calcRectangularFace(Line cycleSegment, Vec3d heightVec){
        Triangle triangle1 = new Triangle();
        Triangle triangle2 = new Triangle();
        ArrayList<Triangle> cylinderFace = new ArrayList<>();
        cylinderFace.add(triangle1);
        cylinderFace.add(triangle2);

        triangle1.setP1(VecMath.calcVecPlusVec(cycleSegment.getP1(), heightVec));
        triangle1.setP2(cycleSegment.getP1().copy());
        triangle1.setP3(cycleSegment.getP2().copy());

        triangle2.setP1(cycleSegment.getP2().copy());
        triangle2.setP2(VecMath.calcVecPlusVec(cycleSegment.getP2(), heightVec));
        triangle2.setP3(VecMath.calcVecPlusVec(cycleSegment.getP1(), heightVec));

        return cylinderFace;
    }

    private static ArrayList<Triangle> calcCylinderSegmentBottomAndTop(Vec3d center, Line cycleSegment, Vec3d heightVec){
        Triangle triangle1 = new Triangle();
        Triangle triangle2 = new Triangle();
        ArrayList<Triangle> cylinderBottomAndTop = new ArrayList<>();
        cylinderBottomAndTop.add(triangle1);
        cylinderBottomAndTop.add(triangle2);

        triangle1.setP1(center.copy());
        triangle1.setP2(cycleSegment.getP2().copy());
        triangle1.setP3(cycleSegment.getP1().copy());

        triangle2.setP1(VecMath.calcVecPlusVec(center, heightVec));
        triangle2.setP2(VecMath.calcVecPlusVec(cycleSegment.getP1(), heightVec));
        triangle2.setP3(VecMath.calcVecPlusVec(cycleSegment.getP2(), heightVec));

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
            Vec3d normal = VecMath.calcNormalVector(triangleArray.get(i));
            normals.add(normal);
            normals.add(normal);
            normals.add(normal);
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
        final int SEGMENT_VERTICES_COUNT = 12;
        final int SEGMENT_COORDS_COUNT = SEGMENT_VERTICES_COUNT * Constants.COORDS_PER_VERTEX;
        final int COORDS_COUNT = Constants.CYLINDER_SEGMENTS * SEGMENT_COORDS_COUNT;
        final int SEGMENT_COLOR_ARRAY_LENGTH = SEGMENT_VERTICES_COUNT * 4;

        normalsInverse = normalsInverse ^ height<0;

        float[] vertices = new float[COORDS_COUNT];
        float[] normals = new float[COORDS_COUNT];
        float[] colors = new float[4*COORDS_COUNT];

        Vec3d firstCycleDir = new Vec3d();
        Vec3d secondCycleDir = new Vec3d();
        VecMath.calcCrossedVectorsFromNormal(secondCycleDir, firstCycleDir, baseNormal);
        Vec3d firstPointOfBaseCycle = VecMath.calcVecTimesScalar(firstCycleDir, radius);
        Vec3d heightVec = VecMath.calcVecTimesScalar(baseNormal, height);

        for (int i=0; i<Constants.CYLINDER_SEGMENTS; i++){
            float[] angles = calcCycleSegmentAnglesDeg(Constants.CYLINDER_SEGMENTS, i);
            Line cycleSegment = calcCycleSegmentLine(firstPointOfBaseCycle, baseNormal, angles, normalsInverse);
            ArrayList<Triangle> cylinderSegment = calcCylinderSegment(center, heightVec, cycleSegment);
            ArrayList<Vec3d> segmentNormals = calcNormalsOfTriangles(cylinderSegment);

            float[] segmentVerticesArray = transformTrianglesToFloatArray(cylinderSegment);
            float[] segmentNormalsArray = transformVec3dListToFloatArray(segmentNormals);
            float[] segmentColorsArray = new float[SEGMENT_COLOR_ARRAY_LENGTH];
            for (int j = 0; j < SEGMENT_VERTICES_COUNT; j++) {
                System.arraycopy(color, 0, segmentColorsArray, j*4, 4);
            }

            System.arraycopy(segmentVerticesArray, 0, vertices, i*SEGMENT_COORDS_COUNT, SEGMENT_COORDS_COUNT);
            System.arraycopy(segmentNormalsArray, 0, normals, i*SEGMENT_COORDS_COUNT, SEGMENT_COORDS_COUNT);
            System.arraycopy(segmentColorsArray, 0, colors, i*SEGMENT_COLOR_ARRAY_LENGTH, SEGMENT_COLOR_ARRAY_LENGTH);
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
        final int CUBOID_TRIANGLE_COUNT = 12;
        final int TRIANGLE_VERTICES_COUNT = 3;
        final int TRIANGLE_COORDS_COUNT = TRIANGLE_VERTICES_COUNT * Constants.COORDS_PER_VERTEX;
        final int COORDS_COUNT = CUBOID_TRIANGLE_COUNT * TRIANGLE_COORDS_COUNT;

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
        float[] colors = new float[4*COORDS_COUNT];
        for (int i = 0; i < COORDS_COUNT; i++) {
            System.arraycopy(color, 0, colors, i*4, 4);
        }

        GeometryStruct result = new GeometryStruct();
        result.vertices = vertices;
        result.normals = normals;
        result.colors = colors;
        return result;
    }



    //TODO wenn nicht mehr für Kugel gebraucht, dann entfernen

//    private static float[] calcCycleSegment(float radius, float fromAngle, float toAngle){
//        float[] vertices = new float[6];
//        vertices[0] = (float)(radius * Math.sin(fromAngle));
//        vertices[1] = 0;
//        vertices[2] = (float)(radius * Math.cos(fromAngle));
//
//        vertices[3] = (float)(radius * Math.sin(toAngle));
//        vertices[4] = 0;
//        vertices[5] = (float)(radius * Math.cos(toAngle));
//        return vertices;
//    }

//    private static ArrayList<Triangle> calcCylinderSegmentFace(Line cycleSegment, float height){
//        Triangle triangle1 = new Triangle();
//        Triangle triangle2 = new Triangle();
//        ArrayList<Triangle> cylinderFace = new ArrayList<>();
//        cylinderFace.add(triangle1);
//        cylinderFace.add(triangle2);
//
//        triangle1.setP1(VecMath.calcVecPlusVec(cycleSegment.getP1(), new Vec3d(0, height, 0)));
//        triangle1.setP2(cycleSegment.getP1().copy());
//        triangle1.setP3(cycleSegment.getP2().copy());
//
//        triangle2.setP1(cycleSegment.getP2().copy());
//        triangle2.setP2(VecMath.calcVecPlusVec(cycleSegment.getP2(), new Vec3d(0, height, 0)));
//        triangle2.setP3(VecMath.calcVecPlusVec(cycleSegment.getP1(), new Vec3d(0, height, 0)));
//
//        return cylinderFace;
//    }

//    private static ArrayList<Triangle> calcCylinderSegmentBottomAndTop(Vec3d center, Line cycleSegment, float height){
//        Triangle triangle1 = new Triangle();
//        Triangle triangle2 = new Triangle();
//        ArrayList<Triangle> cylinderBottomAndTop = new ArrayList<>();
//        cylinderBottomAndTop.add(triangle1);
//        cylinderBottomAndTop.add(triangle2);
//
//        triangle1.setP1(center.copy());
//        triangle1.setP2(cycleSegment.getP2().copy());
//        triangle1.setP3(cycleSegment.getP1().copy());
//
//        triangle2.setP1(VecMath.calcVecPlusVec(center, new Vec3d(0, height, 0)));
//        triangle2.setP2(VecMath.calcVecPlusVec(cycleSegment.getP1(), new Vec3d(0, height, 0)));
//        triangle2.setP3(VecMath.calcVecPlusVec(cycleSegment.getP2(), new Vec3d(0, height, 0)));
//
//        return cylinderBottomAndTop;
//    }

//    private static Line calcCycleSegmentLine(float radius, float fromAngle, float toAngle){
//        return new Line(calcCycleSegment(radius, fromAngle, toAngle));
//    }

//    private static ArrayList<Triangle> calcCylinderSegment(Vec3d center, float radius, float fromAngle, float toAngle, float height, Boolean normalsInverse){
//        ArrayList<Triangle> cylinderSegment = new ArrayList<>();
//        Line cycleSegment;
//        if (normalsInverse) {
//            cycleSegment = calcCycleSegmentLine(radius, fromAngle, toAngle);
//        } else {
//            cycleSegment = calcCycleSegmentLine(radius, toAngle, fromAngle);
//        }
//
//        cycleSegment.setP1(VecMath.calcVecPlusVec(cycleSegment.getP1(), center));
//        cycleSegment.setP2(VecMath.calcVecPlusVec(cycleSegment.getP2(), center));
//
//        cylinderSegment.addAll(calcCylinderSegmentFace(cycleSegment, height));
//        cylinderSegment.addAll(calcCylinderSegmentBottomAndTop(center, cycleSegment, height));
//
//        return  cylinderSegment;
//    }

//    public static GeometryStruct createCylinderGeom(Vec3d center, float radius, float height, float[] color, Boolean normalsInverse){
//        int SEGMENT_VERTICES_COUNT = 12;
//        int SEGMENT_COORDS_COUNT = SEGMENT_VERTICES_COUNT * Constants.COORDS_PER_VERTEX;
//        int SEGMENT_COLOR_ARRAY_LENGTH = SEGMENT_VERTICES_COUNT * 4;
//
//        int COORDS_COUNT = Constants.CYLINDER_SEGMENTS * SEGMENT_COORDS_COUNT;
//
//
//        float[] vertices = new float[COORDS_COUNT];
//        float[] normals = new float[COORDS_COUNT];
//        float[] colors = new float[4*12*Constants.CYLINDER_SEGMENTS];
//
//        for (int i=0; i<Constants.CYLINDER_SEGMENTS; i++){
//            float[] angles = calcCycleSegmentAnglesRad(Constants.CYLINDER_SEGMENTS, i);
//            ArrayList<Triangle> cylinderSegment = calcCylinderSegment(center, radius, angles[0], angles[1], height, normalsInverse);
//            ArrayList<Vec3d> segmentNormals = calcNormalsOfTriangles(cylinderSegment);
//            float[] segmentVerticesArray = transformTrianglesToFloatArray(cylinderSegment);
//            float[] segmentNormalsArray = transformVec3dListToFloatArray(segmentNormals);
//
//            float[] segmentColorsArray = new float[SEGMENT_COLOR_ARRAY_LENGTH];
//            for (int j = 0; j < SEGMENT_VERTICES_COUNT; j++) {
//                System.arraycopy(color, 0, segmentColorsArray, j*4, 4);
//            }
//
//            System.arraycopy(segmentVerticesArray, 0, vertices, i*SEGMENT_COORDS_COUNT, SEGMENT_COORDS_COUNT);
//            System.arraycopy(segmentNormalsArray, 0, normals, i*SEGMENT_COORDS_COUNT, SEGMENT_COORDS_COUNT);
//            System.arraycopy(segmentColorsArray, 0, colors, i*SEGMENT_COLOR_ARRAY_LENGTH, SEGMENT_COLOR_ARRAY_LENGTH);
//        }
//
//        GeometryStruct result = new GeometryStruct();
//        result.vertices = vertices;
//        result.normals = normals;
//        result.colors = colors;
//        return result;
//    }
}
