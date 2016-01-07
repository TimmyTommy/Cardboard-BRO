package de.tinf13aibi.cardboardbro.Geometry;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Constants;

/**
 * Created by dthom on 07.01.2016.
 */
public class GeomFactory {
    private static float[] calcCycleSegment(float radius, float fromAngle, float toAngle){
        float[] vertices = new float[6];
        vertices[0] = (float)(radius * Math.sin(fromAngle));
        vertices[1] = 0;
        vertices[2] = (float)(radius * Math.cos(fromAngle));

        vertices[3] = (float)(radius * Math.sin(toAngle));
        vertices[4] = 0;
        vertices[5] = (float)(radius * Math.cos(toAngle));
        return vertices;
    }

    private static float[] calcCycleSegmentAngles(int cycleEdgesCount, int edgeIndex){
        float fromAngle = (float)(2*Math.PI*(1.0*edgeIndex/cycleEdgesCount));
        float toAngle = (float)(2*Math.PI*((1.0*edgeIndex+1)/cycleEdgesCount));
        float[] angles = new float[2];
        angles[0] = fromAngle;
        angles[1] = toAngle;
        return angles;
    }

    private static ArrayList<Triangle> calcCylinderSegmentFace(Line cycleSegment, float height){
        Triangle triangle1 = new Triangle();
        Triangle triangle2 = new Triangle();
        ArrayList<Triangle> cylinderFace = new ArrayList<>();
        cylinderFace.add(triangle1);
        cylinderFace.add(triangle2);

        triangle1.setP1(VecMath.calcVecPlusVec(cycleSegment.getP1(), new Vec3d(0, height, 0)));
        triangle1.setP2(cycleSegment.getP1().copy());
        triangle1.setP3(cycleSegment.getP2().copy());

        triangle2.setP1(cycleSegment.getP2().copy());
        triangle2.setP2(VecMath.calcVecPlusVec(cycleSegment.getP2(), new Vec3d(0, height, 0)));
        triangle2.setP3(VecMath.calcVecPlusVec(cycleSegment.getP1(), new Vec3d(0, height, 0)));

        return cylinderFace;
    }

    private static ArrayList<Triangle> calcCylinderSegmentBottomAndTop(Vec3d center, Line cycleSegment, float height){
        Triangle triangle1 = new Triangle();
        Triangle triangle2 = new Triangle();
        ArrayList<Triangle> cylinderBottomAndTop = new ArrayList<>();
        cylinderBottomAndTop.add(triangle1);
        cylinderBottomAndTop.add(triangle2);

        triangle1.setP1(center.copy());
        triangle1.setP2(cycleSegment.getP2().copy());
        triangle1.setP3(cycleSegment.getP1().copy());

        triangle2.setP1(VecMath.calcVecPlusVec(center, new Vec3d(0, height, 0)));
        triangle2.setP2(VecMath.calcVecPlusVec(cycleSegment.getP1(), new Vec3d(0, height, 0)));
        triangle2.setP3(VecMath.calcVecPlusVec(cycleSegment.getP2(), new Vec3d(0, height, 0)));

        return cylinderBottomAndTop;
    }

    private static Line calcCycleSegmentLine(float radius, float fromAngle, float toAngle){
        return new Line(calcCycleSegment(radius, fromAngle, toAngle));
    }

    private static ArrayList<Triangle> calcCylinderSegment(Vec3d center, float radius, float fromAngle, float toAngle, float height, Boolean normalsInverse){
        ArrayList<Triangle> cylinderSegment = new ArrayList<>();
        Line cycleSegment;
        if (normalsInverse) {
            cycleSegment = calcCycleSegmentLine(radius, fromAngle, toAngle);
        } else {
            cycleSegment = calcCycleSegmentLine(radius, toAngle, fromAngle);
        }

        cycleSegment.setP1(VecMath.calcVecPlusVec(cycleSegment.getP1(), center));
        cycleSegment.setP2(VecMath.calcVecPlusVec(cycleSegment.getP2(), center));

        cylinderSegment.addAll(calcCylinderSegmentFace(cycleSegment, height));
        cylinderSegment.addAll(calcCylinderSegmentBottomAndTop(center, cycleSegment, height));

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
            System.arraycopy(triangleArray.get(i).toFloatArray(), 0, array, i*9, 9);
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
            System.arraycopy(list.get(i).toFloatArray(), 0, array, i*3, 3);
        }
        return array;
    }

    public static GeometryStruct createCylinderGeom(Vec3d center, float radius, float height, float[] color, Boolean normalsInverse){
        int SEGMENT_VERTICES_COUNT = 12;
        int SEGMENT_COORDS_COUNT = SEGMENT_VERTICES_COUNT * Constants.COORDS_PER_VERTEX;
        int SEGMENT_COLOR_ARRAY_LENGTH = SEGMENT_VERTICES_COUNT * 4;

        int COORDS_COUNT = Constants.CYLINDER_SEGMENTS * SEGMENT_COORDS_COUNT;


        float[] vertices = new float[COORDS_COUNT];
        float[] normals = new float[COORDS_COUNT];
        float[] colors = new float[4*12*Constants.CYLINDER_SEGMENTS];

        for (int i=0; i<Constants.CYLINDER_SEGMENTS; i++){
            float[] angles = calcCycleSegmentAngles(Constants.CYLINDER_SEGMENTS, i);
            ArrayList<Triangle> cylinderSegment = calcCylinderSegment(center, radius, angles[0], angles[1], height, normalsInverse);
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
}
