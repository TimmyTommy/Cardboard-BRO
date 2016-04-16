package de.tinf13aibi.cardboardbro.Geometry.Simple;

import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 17.12.2015.
 */
public class Triangle {
    protected Vec3d p1, p2, p3;
    protected Vec3d n1, n2, n3;
    protected Vec3d v1v2, v1v3;

    public Triangle(Vec3d p1, Vec3d p2, Vec3d p3){
        this.p1 = p1.copy();
        this.p2 = p2.copy();
        this.p3 = p3.copy();
        calcVariables();
    }
//    public Triangle(){
//        p1 = new Vec3d();
//        p2 = new Vec3d();
//        p3 = new Vec3d();
//    }

    public Triangle(float[] triangle){
        this.p1 = new Vec3d(triangle[0], triangle[1], triangle[2]);
        this.p2 = new Vec3d(triangle[3], triangle[4], triangle[5]);
        this.p3 = new Vec3d(triangle[6], triangle[7], triangle[8]);
        calcVariables();
    }

    public float[] toFloatArray(){
        float[] triangle = new float[9];
        System.arraycopy(p1.toFloatArray(), 0, triangle, 0, 3);
        System.arraycopy(p2.toFloatArray(), 0, triangle, 3, 3);
        System.arraycopy(p3.toFloatArray(), 0, triangle, 6, 3);
        return triangle;
    }

    public float[] getNormalsFloatArray(){
        float[] normals = new float[9];
        System.arraycopy(n1.toFloatArray(), 0, normals, 0, 3);
        System.arraycopy(n2.toFloatArray(), 0, normals, 3, 3);
        System.arraycopy(n3.toFloatArray(), 0, normals, 6, 3);
        return normals;
    }

    public Vec3d getP1() {
        return p1;
    }

    public void setP1(Vec3d p1) {
        this.p1 = p1;
        calcVariables();
    }

    public Vec3d getP2() {
        return p2;
    }

    public void setP2(Vec3d p2) {
        this.p2 = p2;
        calcVariables();
    }

    public Vec3d getP3() {
        return p3;
    }

    public void setP3(Vec3d p3) {
        this.p3 = p3;
        calcVariables();
    }

    public void setPoints(Vec3d p1, Vec3d p2, Vec3d p3){
        this.p1 = p1.copy();
        this.p2 = p2.copy();
        this.p3 = p3.copy();

        calcVariables();
    }

    private void calcVariables(){
        v1v2 = VecMath.calcVecMinusVec(p2, p1);
        v1v3 = VecMath.calcVecMinusVec(p3, p1);

//        n1 = VecMath.calcNormalVector(v1v3, v1v2);
        n1 = VecMath.calcNormalVector(this);
        n2 = n1.copy();
        n3 = n1.copy();
    }

    public Vec3d getN1() {
        return n1;
    }

    public Vec3d getN2() {
        return n2;
    }

    public Vec3d getN3() {
        return n3;
    }

    public Vec3d getV1V2() {
        return v1v2;
    }

    public Vec3d getV1V3() {
        return v1v3;
    }

    public Vec3d getMinPoint(){
        Vec3d min = p1.copy();
        min.x = min.x<p2.x ? min.x : p2.x;
        min.y = min.y<p2.y ? min.y : p2.y;
        min.z = min.z<p2.z ? min.z : p2.z;

        min.x = min.x<p3.x ? min.x : p3.x;
        min.y = min.y<p3.y ? min.y : p3.y;
        min.z = min.z<p3.z ? min.z : p3.z;

        return min;
    }

    public Vec3d getMaxPoint(){
        Vec3d max = p1.copy();
        max.x = max.x>p2.x ? max.x : p2.x;
        max.y = max.y>p2.y ? max.y : p2.y;
        max.z = max.z>p2.z ? max.z : p2.z;

        max.x = max.x>p3.x ? max.x : p3.x;
        max.y = max.y>p3.y ? max.y : p3.y;
        max.z = max.z>p3.z ? max.z : p3.z;

        return max;
    }
}
