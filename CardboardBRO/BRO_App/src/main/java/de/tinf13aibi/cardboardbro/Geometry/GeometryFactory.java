package de.tinf13aibi.cardboardbro.Geometry;

import android.util.Log;

import de.tinf13aibi.cardboardbro.Constants;

/**
 * Created by dth on 27.11.2015.
 */
public class GeometryFactory {
    public static float calcVectorLength(Point3d vec){
        return calcVectorLength(vec.toFloatArray());
    }

    public static float calcVectorLength(float[] vec){
        return (float) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
    }

    public static Point3d calcNormalizedVector(Point3d vec){
        float length = calcVectorLength(vec);
        return new Point3d(calcVecTimesScalar(vec.toFloatArray(), 1/length));
    }

    public static void calcCrossVectors(Point3d normal){
        final float eps = 0.000001f;
        Point3d mHoroizontalVec = new Point3d();
        mHoroizontalVec.y = 0;
        if (Math.abs(normal.x)<eps){
            mHoroizontalVec.x = 1;
            mHoroizontalVec.z = 0;
        } else if (Math.abs(normal.z)<eps) {
            mHoroizontalVec.x = 0;
            mHoroizontalVec.z = 1;
        } else {
            mHoroizontalVec.x = 1;
            mHoroizontalVec.z = -normal.x*mHoroizontalVec.x/normal.z;
        }
        Log.i("ergX", String.valueOf(mHoroizontalVec.x));
        Log.i("ergY", String.valueOf(mHoroizontalVec.y));
        Log.i("ergZ", String.valueOf(mHoroizontalVec.z));
    }


    public static float[] calcVecPlusVec(float[] v1, float[] v2){
        float[] res = new float[3];
        res[0] = v1[0] + v2[0];
        res[1] = v1[1] + v2[1];
        res[2] = v1[2] + v2[2];

        return res;
    }

    public static float[] calcVecMinusVec(float[] v1, float[] v2){
        float[] res = new float[3];
        res[0] = v1[0] - v2[0];
        res[1] = v1[1] - v2[1];
        res[2] = v1[2] - v2[2];

        return res;
    }

    public static float[] calcVecTimesScalar(float[] v, float s){
        float[] res = new float[3];
        res[0] = v[0] * s;
        res[1] = v[1] * s;
        res[2] = v[2] * s;

        return res;
    }

    public static float calcScalarProcuct(float[] v1, float[] v2){
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }

    public static boolean calcTriangleLineIntersection(float[] intersectPointOut, Triangle triangle, StraightLine line){
        //http://www2.in.tu-clausthal.de/~zach/teaching/cg2_10/folien/07_raytracing_2.pdf

//        //Gerade: X = P + t*d
//        final float[] p = {0.50f, 0, 0.50f}; //point
//        final float[] d = {1,0.0001f,0};          //direction
//
//        //triangle ABC  //Planeequation: X = A + r*(B-A) + s*(C-A)
//        final float[] a = {0,0,0};
//        final float[] b = {1,0,0};
//        final float[] c = {0,0,1};

        //Gerade: X = P + t*d
        final float[] p = line.pos.toFloatArray(); //point
        final float[] d = line.dir.toFloatArray(); //direction

        //triangle ABC  //Planeequation: X = A + r*(B-A) + s*(C-A)
        final float[] a = triangle.p1.toFloatArray();
        final float[] b = triangle.p2.toFloatArray();
        final float[] c = triangle.p3.toFloatArray();

        float[] u = calcVecMinusVec(b, a);
        float[] v = calcVecMinusVec(c, a);
        float[] w = calcVecMinusVec(p, a);

        // (t, r, s) = 1/det(-d, u, v) * (det(w, u, v), det(−d, w, v), det(−d, u, w))
        // det (a, b, c) = a*(b x c)

        // (t, r, s) = 1/(d x v)*u  * ( (w x u)*v, (d x v)*w, (w x u)*d )

        float[] crossDV = calcCrossProduct(d, v);
        float[] crossWU = calcCrossProduct(w, u);

        float factor = 1/calcScalarProcuct(crossDV, u);

        float[] vec = new float[3];
        vec[0] = calcScalarProcuct(crossWU, v);
        vec[1] = calcScalarProcuct(crossDV, w);
        vec[2] = calcScalarProcuct(crossWU, d);

        float[] trs = calcVecTimesScalar(vec, factor);

        float[] intersectPoint = calcVecPlusVec(p, calcVecTimesScalar(d, trs[0]));
        System.arraycopy(intersectPoint, 0, intersectPointOut, 0, 3);

        return trs[0] > 0 && isInRange(trs[1], 0, 1) && isInRange(trs[2], 0, 1) && isInRange(trs[1]+trs[2], 0, 1);
    }

    public static boolean isInRange(float x, float begin, float end){
        return (begin <= x) && (x <= end);
    }

    public static float[] calcCrossProduct(float[] v1, float[] v2){
        float[] kreuz = new float[3];

        kreuz[0]=+((v1[1]*v2[2])-(v1[2]*v2[1]));
        kreuz[1]=-((v1[0]*v2[2])-(v1[2]*v2[0]));
        kreuz[2]=+((v1[0]*v2[1])-(v1[1]*v2[0]));

        return kreuz;
    }

    public static float[] calcNormalVector(float[] triangle){
        return calcNormalVector(triangle, 1);
    }

    public static float[] calcNormalVector(float[] triangle, int normalsDirection){
//        final float[] v1 = {0,0,0};
//        final float[] v2 = {1,0,0};
//        final float[] v3 = {0,0,1};
        float[] v1 = new float[3];
        float[] v2 = new float[3];
        float[] v3 = new float[3];
        System.arraycopy(triangle, 0, v1, 0, 3);
        System.arraycopy(triangle, 3, v2, 0, 3);
        System.arraycopy(triangle, 6, v3, 0, 3);

        float[] v1v2, v1v3, kreuz;
        //Vorbereitung
        v1v2=new float[3];
        v1v2[0]= v2[0]-v1[0];
        v1v2[1]= v2[1]-v1[1];
        v1v2[2]= v2[2]-v1[2];

        v1v3=new float[3];
        v1v3[0]= v3[0]-v1[0];
        v1v3[1]= v3[1]-v1[1];
        v1v3[2]= v3[2]-v1[2];

//        //Berechnung des Kreuz
//        double x=+((v1v2[1]*v1v3[2])-(v1v2[2]*v1v3[1]));
//        double y=-((v1v2[0]*v1v3[2])-(v1v2[2]*v1v3[0]));
//        double z=+((v1v2[0]*v1v3[1])-(v1v2[1]*v1v3[0]));
//        kreuz=new float[3];

//        kreuz[0]=(float)x*normalsDirection;
//        kreuz[1]=(float)y*normalsDirection;
//        kreuz[2]=(float)z*normalsDirection;

        kreuz = calcCrossProduct(v1v2, v1v3);
        kreuz = calcVecTimesScalar(kreuz, normalsDirection);
//        kreuz[0] *= normalsDirection;
//        kreuz[1] *= normalsDirection;
//        kreuz[2] *= normalsDirection;

        return kreuz;
    }


    public static float[] calcNormalVector2(float[] triangle, int normalsDirection){
        float[] v1 = new float[3];
        float[] v2 = new float[3];
        float[] v3 = new float[3];
        System.arraycopy(triangle, 0, v1, 0, 3);
        System.arraycopy(triangle, 3, v2, 0, 3);
        System.arraycopy(triangle, 6, v3, 0, 3);

        float[] v1v2, v1v3, kreuz;
        //Vorbereitung
        v1v2=new float[3];
        v1v2[0]= v2[0]-v1[0];
        v1v2[1]= v2[1]-v1[1];
        v1v2[2]= v2[2]-v1[2];

        v1v3=new float[3];
        v1v3[0]= v3[0]-v1[0];
        v1v3[1]= v3[1]-v1[1];
        v1v3[2]= v3[2]-v1[2];

        //Berechnung des Kreuz
        double x=+((v1v2[1]*v1v3[2])-(v1v2[2]*v1v3[1]));
        double y=-((v1v2[0]*v1v3[2])-(v1v2[2]*v1v3[0]));
        double z=+((v1v2[0]*v1v3[1])-(v1v2[1]*v1v3[0]));
        kreuz=new float[3];
        kreuz[0]=(float)x*normalsDirection;
        kreuz[1]=(float)y*normalsDirection;
        kreuz[2]=(float)z*normalsDirection;

        return kreuz;
    }

    public static float[] getFirstTriangle(float[] vertices){
        float[] triangle = new float[9];
        System.arraycopy(vertices, 0, triangle, 0, 9);
        return triangle;
    }

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

    private static float[] calcAngles(int cycleEdgesCount, int edgeIndex){
        float fromAngle = (float)(2*Math.PI*(1.0*edgeIndex/cycleEdgesCount));
        float toAngle = (float)(2*Math.PI*((1.0*edgeIndex+1)/cycleEdgesCount));
        float[] angles = new float[2];
        angles[0] = fromAngle;
        angles[1] = toAngle;
        return angles;
    }

    private static float[] calcCylinderFace(float radius, float fromAngle, float toAngle, float height){
        float[] basePoints = calcCycleSegment(radius, fromAngle, toAngle);
        float[] cylinderFace = new float[6*3];
        //Triangle 1 //P1
        cylinderFace[0] = basePoints[0];
        cylinderFace[1] = basePoints[1] + height;
        cylinderFace[2] = basePoints[2];
        //P2
        cylinderFace[3] = basePoints[0];
        cylinderFace[4] = basePoints[1];
        cylinderFace[5] = basePoints[2];
        //P3
        cylinderFace[6] = basePoints[3];
        cylinderFace[7] = basePoints[4];
        cylinderFace[8] = basePoints[5];
        //Triangle 2 //P1
        cylinderFace[9] = basePoints[3];
        cylinderFace[10]= basePoints[4];
        cylinderFace[11]= basePoints[5];
        //P2
        cylinderFace[12] = basePoints[3];
        cylinderFace[13] = basePoints[4] + height;
        cylinderFace[14] = basePoints[5];
        //P3
        cylinderFace[15] = basePoints[0];
        cylinderFace[16] = basePoints[1] + height;
        cylinderFace[17] = basePoints[2];
        return cylinderFace;
    }

    public static GeometryStruct createCylinderGeom(Boolean normalsInverse){
        int SEGMENT_VERTICES_COUNT = 6;
        int SEGMENT_COORDS_COUNT = SEGMENT_VERTICES_COUNT * Constants.COORDS_PER_VERTEX;
        int COORDS_COUNT = Constants.CYLINDER_SEGMENTS * SEGMENT_COORDS_COUNT;

        int normalsDirection = normalsInverse ? -1 : 1;

        float[] vertices = new float[COORDS_COUNT];
        float[] normals = new float[COORDS_COUNT];
        float[] colors = new float[4*6*Constants.CYLINDER_SEGMENTS];

        for (int i=0; i<Constants.CYLINDER_SEGMENTS; i++){
            float[] angles = calcAngles(Constants.CYLINDER_SEGMENTS, i);
            float[] cylinderFace = calcCylinderFace(Constants.CYL_RADIUS/*radius*/, angles[0], angles[1], 100/*height*/);
            float[] triangle = getFirstTriangle(cylinderFace);
            float[] normal = calcNormalVector(triangle, normalsDirection);

            System.arraycopy(cylinderFace, 0, vertices, i*SEGMENT_COORDS_COUNT, SEGMENT_COORDS_COUNT);

            for (int j=0; j<SEGMENT_VERTICES_COUNT; j++){
                int dstPos = i*SEGMENT_COORDS_COUNT+j*Constants.COORDS_PER_VERTEX;
                System.arraycopy(normal, 0, normals, dstPos, Constants.COORDS_PER_VERTEX);
            }

            for (int j=0; j<6; j++){
                System.arraycopy(GeometryDatabase.CYLINDER_COLOR, 0, colors, i*24+j*4,4);
            }
        }
        GeometryStruct result = new GeometryStruct();
        result.vertices = vertices;
        result.normals = normals;
        result.colors = colors;
        return result;
    }
}
