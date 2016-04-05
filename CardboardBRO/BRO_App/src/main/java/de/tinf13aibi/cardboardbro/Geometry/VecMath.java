package de.tinf13aibi.cardboardbro.Geometry;

import android.opengl.Matrix;

import de.tinf13aibi.cardboardbro.Geometry.Simple.Plane;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Straight;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dth on 27.11.2015.
 */
public class VecMath {
    public static Vec3d calcQuaternionToForwardVector(float[] quaternion){
        float[] mat = calcQuaternionToMatrix(quaternion);
        float[] initVec = new Vec3d(0,0,-1).toFloatArray4d();
        float[] forwardVec = new float[4];
        Matrix.multiplyMV(forwardVec, 0, mat, 0, initVec, 0);
        return new Vec3d(forwardVec);
    }

    public static float[] calcQuaternionToMatrix(float[] quaternion){
        float x = quaternion[0];
        float y = quaternion[1];
        float z = quaternion[2];
        float w = quaternion[3];

        float x2 = x * x;
        float y2 = y * y;
        float z2 = z * z;
        float xy = x * y;
        float xz = x * z;
        float yz = y * z;
        float wx = w * x;
        float wy = w * y;
        float wz = w * z;

        return new float[]{1.0f - 2.0f * (y2 + z2), 2.0f * (xy - wz), 2.0f * (xz + wy), 0.0f,
                            2.0f * (xy + wz), 1.0f - 2.0f * (x2 + z2), 2.0f * (yz - wx), 0.0f,
                            2.0f * (xz - wy), 2.0f * (yz + wx), 1.0f - 2.0f * (x2 + y2), 0.0f,
                            0.0f, 0.0f, 0.0f, 1.0f};
    }

    public static float calcVectorLength(Vec3d vec){
        return calcVectorLength(vec.toFloatArray());
    }

    public static float calcVectorLength(float[] vec){
        return (float) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
    }

    public static Vec3d calcNormalizedVector(Vec3d vec){
//        float length = calcVectorLength(vec);
//        return new Vec3d(calcVecTimesScalar(vec.toFloatArray(), 1/length));
        return new Vec3d(calcNormalizedVector(vec.toFloatArray()));
    }

    public static float[] calcNormalizedVector(float[] vec){
        float length = calcVectorLength(vec);
        return calcVecTimesScalar(vec, 1/length);
    }

//
//    public static void calcCrossVectors(Vec3d normal){
//        final float eps = 0.000001f;
//        Vec3d mHoroizontalVec = new Vec3d();
//        mHoroizontalVec.y = 0;
//        if (Math.abs(normal.x)<eps){
//            mHoroizontalVec.x = 1;
//            mHoroizontalVec.z = 0;
//        } else if (Math.abs(normal.z)<eps) {
//            mHoroizontalVec.x = 0;
//            mHoroizontalVec.z = 1;
//        } else {
//            mHoroizontalVec.x = 1;
//            mHoroizontalVec.z = -normal.x*mHoroizontalVec.x/normal.z;
//        }
//        Log.i("ergX", String.valueOf(mHoroizontalVec.x));
//        Log.i("ergY", String.valueOf(mHoroizontalVec.y));
//        Log.i("ergZ", String.valueOf(mHoroizontalVec.z));
//    }

    public static float calcAngleBetweenVecsRad(Vec3d v1, Vec3d v2){
        return (float)Math.acos(calcScalarProcuct(v1, v2)/calcVectorLength(v1)*calcVectorLength(v2));
    }

    public static float calcAngleBetweenVecsDeg(Vec3d v1, Vec3d v2){
        return radToDeg(calcAngleBetweenVecsRad(v1, v2));
    }

    public static float[] calcVecPlusVec(float[] v1, float[] v2){
        float[] res = new float[3];
        res[0] = v1[0] + v2[0];
        res[1] = v1[1] + v2[1];
        res[2] = v1[2] + v2[2];
        return res;
    }

    public static Vec3d calcVecPlusVec(Vec3d v1, Vec3d v2){
        return new Vec3d(calcVecPlusVec(v1.toFloatArray(), v2.toFloatArray()));
    }

    public static float[] calcVecMinusVec(float[] v1, float[] v2){
        float[] res = new float[3];
        res[0] = v1[0] - v2[0];
        res[1] = v1[1] - v2[1];
        res[2] = v1[2] - v2[2];
        return res;
    }

    public static Vec3d calcVecMinusVec(Vec3d v1, Vec3d v2){
        return new Vec3d(calcVecMinusVec(v1.toFloatArray(), v2.toFloatArray()));
    }

    public static float[] calcVecTimesScalar(float[] v, float s){
        float[] res = new float[3];
        res[0] = v[0] * s;
        res[1] = v[1] * s;
        res[2] = v[2] * s;

        return res;
    }

    public static Vec3d calcVecTimesScalar(Vec3d v, float s){
        return new Vec3d(calcVecTimesScalar(v.toFloatArray(), s));
    }

    public static float calcScalarProcuct(float[] v1, float[] v2){
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }

    public static float calcScalarProcuct(Vec3d v1, Vec3d v2){
        return calcScalarProcuct(v1.toFloatArray(), v2.toFloatArray());
    }

    public static boolean calcPlaneLineIntersection(float[] intersectPointOut, float[] trsOut, Triangle triangle, Straight line){
//        Vec3d triangleNormal = calcNormalVector(triangle);
        Vec3d triangleNormal = triangle.getN1();
        float scalar = calcScalarProcuct(triangleNormal, line.dir);
        final float eps = 0.000001f;
        if (Math.abs(scalar)<eps){
            return false;
        }

        //Gerade: X = P + t*d
        final float[] p = line.pos.toFloatArray(); //point
        final float[] d = line.dir.toFloatArray(); //direction

        //triangle ABC  //Planeequation: X = A + r*(B-A) + s*(C-A)
        final float[] a = triangle.getP1().toFloatArray();
        final float[] b = triangle.getP2().toFloatArray();
        final float[] c = triangle.getP3().toFloatArray();

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

        System.arraycopy(trs, 0, trsOut, 0, 3);
        return true;
    }

    public static boolean calcTriangleLineIntersection(float[] intersectPointOut, Triangle triangle, Straight line){
        //http://www2.in.tu-clausthal.de/~zach/teaching/cg2_10/folien/07_raytracing_2.pdf

        float[] trs = new float[3];
        calcPlaneLineIntersection(intersectPointOut, trs, triangle, line);
        //System.out.println("t: "+ trs[0]);
        return trs[0] > 0 && isInRange(trs[1], 0, 1) && isInRange(trs[2], 0, 1) && isInRange(trs[1]+trs[2], 0, 1);
    }

//    public static boolean calcTriangleLineIntersection(float[] intersectPointOut, Triangle triangle, Straight line){
//        //http://www2.in.tu-clausthal.de/~zach/teaching/cg2_10/folien/07_raytracing_2.pdf
//
//        //Gerade: X = P + t*d
//        final float[] p = line.pos.toFloatArray(); //point
//        final float[] d = line.dir.toFloatArray(); //direction
//
//        //triangle ABC  //Planeequation: X = A + r*(B-A) + s*(C-A)
//        final float[] a = triangle.getP1().toFloatArray();
//        final float[] b = triangle.getP2().toFloatArray();
//        final float[] c = triangle.getP3().toFloatArray();
//
//        float[] u = calcVecMinusVec(b, a);
//        float[] v = calcVecMinusVec(c, a);
//        float[] w = calcVecMinusVec(p, a);
//
//        // (t, r, s) = 1/det(-d, u, v) * (det(w, u, v), det(−d, w, v), det(−d, u, w))
//        // det (a, b, c) = a*(b x c)
//
//        // (t, r, s) = 1/(d x v)*u  * ( (w x u)*v, (d x v)*w, (w x u)*d )
//
//        float[] crossDV = calcCrossProduct(d, v);
//        float[] crossWU = calcCrossProduct(w, u);
//
//        float factor = 1/calcScalarProcuct(crossDV, u);
//
//        float[] vec = new float[3];
//        vec[0] = calcScalarProcuct(crossWU, v);
//        vec[1] = calcScalarProcuct(crossDV, w);
//        vec[2] = calcScalarProcuct(crossWU, d);
//
//        float[] trs = calcVecTimesScalar(vec, factor);
//
//        float[] intersectPoint = calcVecPlusVec(p, calcVecTimesScalar(d, trs[0]));
//        System.arraycopy(intersectPoint, 0, intersectPointOut, 0, 3);
//
//        return trs[0] > 0 && isInRange(trs[1], 0, 1) && isInRange(trs[2], 0, 1) && isInRange(trs[1]+trs[2], 0, 1);
//    }

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

    public static Vec3d calcCrossProduct(Vec3d v1, Vec3d v2){
        return new Vec3d(calcCrossProduct(v1.toFloatArray(), v2.toFloatArray()));
    }

    public static Vec3d calcNormalVector(Triangle triangle){
        return calcNormalizedVector(calcCrossProduct(triangle.getV1V3(), triangle.getV1V2()));
    }

//    private static Vec3d calcNormalVector(Triangle triangle, int normalsDirection){
//        Vec3d v1v2, v1v3, cross;
//        //Vorbereitung
//        v1v2 = calcVecMinusVec(triangle.getP2(), triangle.getP1());
//        v1v3 = calcVecMinusVec(triangle.getP3(), triangle.getP1());
//
//        cross = calcCrossProduct(v1v3, v1v2);
//        cross = calcNormalizedVector(calcVecTimesScalar(cross, normalsDirection));
//
//        return cross;
//    }

    public static void calcCrossedVectorsFromNormal(Vec3d vecHorizontalOut, Vec3d vecVerticalOut, Vec3d normal){
        normal.assignPoint3d(VecMath.calcNormalizedVector(normal));
        final float eps = 0.000001f;
        vecHorizontalOut.y = 0;
        if (Math.abs(normal.x)<eps){
            vecHorizontalOut.x = 1;
            vecHorizontalOut.z = 0;
        } else if (Math.abs(normal.z)<eps) {
            vecHorizontalOut.x = 0;
            vecHorizontalOut.z = 1;
        } else {
            vecHorizontalOut.x = 1;
            vecHorizontalOut.z = -normal.x*vecHorizontalOut.x/normal.z;
        }
//        vecVerticalOut.assignPoint3d(VecMath.calcCrossProduct(normal, vecHorizontalOut));
        vecVerticalOut.assignPoint3d(VecMath.calcCrossProduct(vecHorizontalOut, normal));

        vecHorizontalOut.assignPoint3d(VecMath.calcNormalizedVector(vecHorizontalOut));
        vecVerticalOut.assignPoint3d(VecMath.calcNormalizedVector(vecVerticalOut));
    }

    public static float radToDeg(float rad){
        return (float)(rad*180/Math.PI);
    }

    public static float degToRad(float deg){
        return (float)(deg*Math.PI/180);
    }

    public static Vec3d calcRotateVecAroundAxis(Vec3d point, Vec3d axis, float angle){
        float[] rotMat = new float[16];
        float[] resPoint = new float[4];
        Matrix.setRotateM(rotMat, 0, angle, axis.x, axis.y, axis.z);
        Matrix.multiplyMV(resPoint, 0, rotMat, 0, point.toFloatArray4d(), 0);
        return new Vec3d(resPoint);
    }

    public static float calcDistancePlanePoint(Plane plane, Vec3d point){
        float[] intersectPoint = new float[3];
        float[] trs = new float[3];
//        Vec3d planeNormal = calcNormalVector(plane);
        Vec3d planeNormal = plane.getN1();
        Straight line = new Straight(point, planeNormal);
        calcPlaneLineIntersection(intersectPoint, trs, plane, line);

        Vec3d distanceVec = calcVecTimesScalar(planeNormal, trs[0]);
        float distance;
        distance = trs[0]<0 ? -1 : 1;
        distance *= calcVectorLength(distanceVec);

        return distance;
    }

    public static Plane calcPlaneFromPointAndNormal(Vec3d point, Vec3d normal){
        Vec3d firstCycleDir = new Vec3d();
        Vec3d secondCycleDir = new Vec3d();
        normal = normal.copy();
        VecMath.calcCrossedVectorsFromNormal(secondCycleDir, firstCycleDir, normal);

        Vec3d p2 = VecMath.calcVecPlusVec(point, firstCycleDir);
        Vec3d p3 = VecMath.calcVecPlusVec(point, secondCycleDir);

        return new Plane(point, p2, p3);
    }
}
