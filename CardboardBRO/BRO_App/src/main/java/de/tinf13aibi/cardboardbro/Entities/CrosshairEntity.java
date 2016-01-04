package de.tinf13aibi.cardboardbro.Entities;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 16.12.2015.
 */
public class CrosshairEntity extends BaseEntity implements IEntity {
    private Vec3d mPosition = new Vec3d();
    private Vec3d mNormal = new Vec3d(0, 0, 1);
    private float mDistance = 0;

    private Vec3d mVerticalVec = new Vec3d();
    private Vec3d mHoroizontalVec = new Vec3d();

    private ArrayList<LineEntity> mLines = new ArrayList<>();

    public Vec3d getPosition(){
        return mPosition;
    }

    @Override
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        for (LineEntity lineEntity : mLines) {
            lineEntity.draw(view, perspective, lightPosInEyeSpace);
        }
    }

    private void calcCrossVectors(Vec3d normal){
        final float eps = 0.000001f;
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
        mVerticalVec.assignPoint3d(VecMath.calcCrossProduct(normal, mHoroizontalVec));

        mHoroizontalVec.assignPoint3d(VecMath.calcNormalizedVector(mHoroizontalVec));
        mVerticalVec.assignPoint3d(VecMath.calcNormalizedVector(mVerticalVec));

        mHoroizontalVec.assignPoint3d(VecMath.calcVecTimesScalar(mHoroizontalVec, mDistance / 10));
        mVerticalVec.assignPoint3d(VecMath.calcVecTimesScalar(mVerticalVec, mDistance/10));
        mNormal.assignPoint3d(VecMath.calcVecTimesScalar(mNormal, mDistance / 10));
    }

    private void calcCrossedLines(){
        LineEntity line;
        line = mLines.get(0);
        line.setVerts(mPosition, VecMath.calcVecPlusVec(mPosition, mHoroizontalVec));
        line.setColor(1, 0, 0, 1);
        line = mLines.get(1);
        line.setVerts(mPosition, VecMath.calcVecMinusVec(mPosition, mHoroizontalVec));
        line.setColor(1, 0, 0, 1);

        line = mLines.get(2);
        line.setVerts(mPosition, VecMath.calcVecPlusVec(mPosition, mVerticalVec));
        line.setColor(0, 1, 0, 1);
        line = mLines.get(3);
        line.setVerts(mPosition, VecMath.calcVecMinusVec(mPosition, mVerticalVec));
        line.setColor(0, 1, 0, 1);

        //Normal
        line = mLines.get(4);
        line.setVerts(mPosition, VecMath.calcVecPlusVec(mPosition, mNormal));
        line.setColor(0, 0, 1, 1);
        line = mLines.get(5);
        line.setVerts(mPosition, VecMath.calcVecMinusVec(mPosition, mNormal));
        line.setColor(0, 0, 1, 1);
    }

    public void setPosition(Vec3d position, Vec3d normal, float distance) {
//        mNormal = normal;
//        mPosition = position;
        mNormal = VecMath.calcNormalizedVector(normal);
        Vec3d translation = VecMath.calcVecTimesScalar(mNormal, 0.0001f);
        mPosition = VecMath.calcVecPlusVec(position, translation);
        mDistance = distance;
        calcCrossVectors(mNormal);
        calcCrossedLines();
    }

    public CrosshairEntity(int program){
        super();
        mLines.add(new LineEntity(program));
        mLines.add(new LineEntity(program));
        mLines.add(new LineEntity(program));
        mLines.add(new LineEntity(program));
        mLines.add(new LineEntity(program));
        mLines.add(new LineEntity(program));
    }
}
