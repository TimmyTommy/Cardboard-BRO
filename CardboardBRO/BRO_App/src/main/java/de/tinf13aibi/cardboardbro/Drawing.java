package de.tinf13aibi.cardboardbro;

import android.opengl.Matrix;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonSet;
import de.tinf13aibi.cardboardbro.Entities.CubeEntity;
import de.tinf13aibi.cardboardbro.Entities.CuboidEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderCanvasEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderEntity;
import de.tinf13aibi.cardboardbro.Entities.FloorEntity;
import de.tinf13aibi.cardboardbro.Entities.IEntity;
import de.tinf13aibi.cardboardbro.Entities.PlaneEntity;
import de.tinf13aibi.cardboardbro.Entities.PolyLineEntity;
import de.tinf13aibi.cardboardbro.Entities.SphereEntity;
import de.tinf13aibi.cardboardbro.Enums.AppState;
import de.tinf13aibi.cardboardbro.Enums.EntityDisplayType;
import de.tinf13aibi.cardboardbro.Enums.Programs;
import de.tinf13aibi.cardboardbro.Geometry.Plane;
import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 09.01.2016.
 */
public class Drawing {
    private ArrayList<IEntity> mEntityList = new ArrayList<>();
    private ButtonSet mEntityActionButtons = new ButtonSet();
    private ButtonSet mEntityCreateButtons = new ButtonSet();
    private ButtonSet mKeyboardButtons = new ButtonSet();
    private Plane mTempWorkingPlane;
    private CylinderCanvasEntity mCylinderCanvasEntity;
    private FloorEntity mFloorEntity;

    public void drawEntityList(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        for (int i = 0; i < mEntityList.size(); i++) {
            mEntityList.get(i).draw(view, perspective, lightPosInEyeSpace);
        }
        mCylinderCanvasEntity.draw(view, perspective, lightPosInEyeSpace);
        mFloorEntity.draw(view, perspective, lightPosInEyeSpace);
    }

    public void drawTempWorkingPlane(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        if (mTempWorkingPlane!= null){
            Vec3d center = mTempWorkingPlane.getP1();
//            Vec3d normal = VecMath.calcNormalVector(mTempWorkingPlane);
            Vec3d normal = mTempWorkingPlane.getN1();
            float[] color = new float[]{0, 1, 0, 1f};
            PlaneEntity planeEntity = new PlaneEntity(ShaderCollection.getProgram(Programs.BodyProgram), center, normal, color);
            planeEntity.draw(view, perspective, lightPosInEyeSpace);
        }
    }

    public Drawing init(){
        setupCylinderCanvas();
        setupFloor();

        setupEntityActionButtonSet();
        setupEntityCreateButtonSet();
        setupTestObjects();

        return this;
    }

    private void setupCylinderCanvas(){
        mCylinderCanvasEntity = new CylinderCanvasEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(mCylinderCanvasEntity.getModel(), 0, 0, Constants.CANVAS_CYL_DEPTH, 0);
        mCylinderCanvasEntity.changedModel();
//        BaseEntity entity = new CylinderCanvasEntity(ShaderCollection.getProgram(Programs.BodyProgram));
//        Matrix.translateM(entity.getModel(), 0, 0, Constants.CANVAS_CYL_DEPTH, 0);
//        mEntityList.add(entity);
    }

    private void setupFloor(){
        mFloorEntity = new FloorEntity(ShaderCollection.getProgram(Programs.GridProgram));
        Matrix.translateM(mFloorEntity.getModel(), 0, 0, Constants.FLOOR_DEPTH, 0);
        mFloorEntity.changedModel();
//        BaseEntity entity = new FloorEntity(ShaderCollection.getProgram(Programs.GridProgram));
//        Matrix.translateM(entity.getModel(), 0, 0, Constants.FLOOR_DEPTH, 0);
//        mEntityList.add(entity);
    }

    private void setupEntityCreateButtonSet(){
        AppState[] appStates = new AppState[]{  AppState.SelectAction, AppState.WaitForBeginFreeLine, AppState.WaitForBeginPolyLinePoint, AppState.WaitForSphereCenterPoint,
                AppState.WaitForCylinderCenterPoint, AppState.WaitForCuboidBasePoint1, AppState.WaitForKeyboardInput};
        for (int i=0; i<7; i++) {
            ButtonEntity entity = new ButtonEntity(ShaderCollection.getProgram(Programs.BodyProgram));
            entity.setDisplayType(EntityDisplayType.RelativeToCamera);
            entity.setNextState(appStates[i]);
            float y = -0.13f;
            Matrix.translateM(entity.getBaseModel(), 0, 0.18f-0.06f*i, y, -0.3f);
            Matrix.scaleM(entity.getBaseModel(), 0, 0.025f, 0.025f, 0.006f);
//            float y = -0.065f;
//            Matrix.translateM(mEntity.getBaseModel(), 0, 0.06f-0.03f*i, y, -0.15f);
//            Matrix.scaleM(mEntity.getBaseModel(), 0, 0.0125f, 0.0125f, 0.003f);
//            mEntityList.add(entity);
            mEntityCreateButtons.addButton(entity);
        }
    }

    private void setupEntityActionButtonSet(){
        AppState[] appStates = new AppState[]{AppState.SelectEntityToCreate, AppState.SelectEntityToMove, AppState.SelectEntityToDelete};
        for (int i=0; i<3; i++) {
            ButtonEntity entity = new ButtonEntity(ShaderCollection.getProgram(Programs.BodyProgram));
            entity.setDisplayType(EntityDisplayType.RelativeToCamera);
            entity.setNextState(appStates[i]);
            float y = -0.13f;
            Matrix.translateM(entity.getBaseModel(), 0, 0.06f-0.06f*i, y, -0.3f);
            Matrix.scaleM(entity.getBaseModel(), 0, 0.025f, 0.025f, 0.006f);
//            float y = -0.065f;
//            Matrix.translateM(mEntity.getBaseModel(), 0, 0.06f-0.03f*i, y, -0.15f);
//            Matrix.scaleM(mEntity.getBaseModel(), 0, 0.0125f, 0.0125f, 0.003f);
//            mEntityList.add(entity);
            mEntityActionButtons.addButton(entity);
        }

    }

    private void setupTestObjects(){
        BaseEntity entity;

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 1, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        entity.changedModel();
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, -1, 2, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        entity.changedModel();
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 2, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        entity.changedModel();
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 0, 1, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        entity.changedModel();
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 3, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        entity.changedModel();
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, -1, 0, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        entity.changedModel();
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 0, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        entity.changedModel();
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 0, 0, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        entity.changedModel();
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 0.05f, 0, -0.50f);
        Matrix.scaleM(entity.getModel(), 0, 0.055f, 0.055f, 0.055f);
        entity.changedModel();
        mEntityList.add(entity);

        CylinderEntity cylEntity = new CylinderEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        cylEntity.setAttributes(new Vec3d(1f, 1, -5.0f), new Vec3d(0, 1, 0), 0.5f, 2, new float[]{1, 0, 0, 1});
        mEntityList.add(cylEntity);

        cylEntity = new CylinderEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        cylEntity.setAttributes(new Vec3d(-1f, 0, -5.0f), new Vec3d(0, 1, 0), 1, -1, new float[]{0, 0, 1, 1});
        mEntityList.add(cylEntity);

        CuboidEntity cuboidEntity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        cuboidEntity.setAttributes(new Vec3d(2f, 0, -5.0f), new Vec3d(0,1,0), -1, -2, -1, new float[]{1, 1, 0, 1});
        mEntityList.add(cuboidEntity);

        SphereEntity sphereEntity = new SphereEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        sphereEntity.setAttributes(new Vec3d(4, 2, 0), new Vec3d(0,1,0), 1, new float[]{1, 0.5f, 0, 1});
        mEntityList.add(sphereEntity);

//Test Polyline
//        PolyLineEntity polyLineEntity = new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram));
//        polyLineEntity.addVert(new Vec3d(0, 0, 0));
//        polyLineEntity.addVert(new Vec3d(0, 0, -10));
//        polyLineEntity.addVert(new Vec3d(0, 2, -10));
//        polyLineEntity.addVert(new Vec3d(0, 2, -5));
//        polyLineEntity.addVert(new Vec3d(8, 2, -5));
//        polyLineEntity.setColor(1,1,0,1);
//        mEntityList.add(polyLineEntity);
    }

    public Plane getTempWorkingPlane() {
        return mTempWorkingPlane;
    }

    public ArrayList<IEntity> getEntityList() {
        return mEntityList;
    }

    public ArrayList<IEntity> getEntityListWithFloorAndCanvas(){
        ArrayList<IEntity> collidableEntityList = new ArrayList<>();
        collidableEntityList.addAll(mEntityList);
        collidableEntityList.add(mCylinderCanvasEntity);
        collidableEntityList.add(mFloorEntity);

        return collidableEntityList;
    }

    public ButtonSet getEntityActionButtons() {
        return mEntityActionButtons;
    }

    public ButtonSet getEntityCreateButtons() {
        return mEntityCreateButtons;
    }

    public ButtonSet getKeyboardButtons() {
        return mKeyboardButtons;
    }

    public void setTempWorkingPlane(Plane tempWorkingPlane) {
        mTempWorkingPlane = tempWorkingPlane;
    }
}
