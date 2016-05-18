package de.tinf13aibi.cardboardbro.Engine;

import android.opengl.Matrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonSet;
import de.tinf13aibi.cardboardbro.Entities.EntityDisplayType;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.CrosshairEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.LineEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.PolyLineEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.TextCharEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.TextEntity;
import de.tinf13aibi.cardboardbro.Entities.Triangulated.CubeEntity;
import de.tinf13aibi.cardboardbro.Entities.Triangulated.CuboidEntity;
import de.tinf13aibi.cardboardbro.Entities.Triangulated.CylinderCanvasEntity;
import de.tinf13aibi.cardboardbro.Entities.Triangulated.CylinderEntity;
import de.tinf13aibi.cardboardbro.Entities.Triangulated.FloorEntity;
import de.tinf13aibi.cardboardbro.Entities.Triangulated.PlaneEntity;
import de.tinf13aibi.cardboardbro.Entities.Triangulated.SphereEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Plane;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Shader.Programs;
import de.tinf13aibi.cardboardbro.Shader.ShaderCollection;
import de.tinf13aibi.cardboardbro.Shader.Textures;

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

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("mEntityList", getEntityListToJsonObject());


        return json;
    }

    public JSONArray getEntityListToJsonObject() throws JSONException {
        JSONArray json = new JSONArray();
        for (IEntity entity : mEntityList) {
            json.put(entity.toJsonObject());
        }
        return json;
    }

    private IEntity loadEntityFromJsonObject(JSONObject jsonEntity) throws JSONException {
        String className = jsonEntity.optString("class");
        IEntity entity = null;
        if (className != null) {
            if (className.equals(CubeEntity.class.toString())){
                entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
            } else if (className.equals(CuboidEntity.class.toString())){
                entity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
            } else if (className.equals(CylinderEntity.class.toString())){
                entity = new CylinderEntity(ShaderCollection.getProgram(Programs.BodyProgram));
            } else if (className.equals(SphereEntity.class.toString())){
                entity = new SphereEntity(ShaderCollection.getProgram(Programs.BodyProgram));
            } else if (className.equals(LineEntity.class.toString())){
                entity = new LineEntity(ShaderCollection.getProgram(Programs.LineProgram));
            } else if (className.equals(PolyLineEntity.class.toString())){
                entity = new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram));
            } else if (className.equals(TextEntity.class.toString())){
                entity = new TextEntity();
            } else if (className.equals(CylinderCanvasEntity.class.toString())){
                entity = new CylinderCanvasEntity(ShaderCollection.getProgram(Programs.BodyProgram));
            } else if (className.equals(FloorEntity.class.toString())){
                entity = new FloorEntity(ShaderCollection.getProgram(Programs.GridProgram));
            } else if (className.equals(CrosshairEntity.class.toString())){
                entity = new CrosshairEntity(ShaderCollection.getProgram(Programs.LineProgram));
            }
            if (entity != null) {
                entity.loadFromJsonObject(jsonEntity);
            }
        }
        return entity;
    }

    private void loadEntityListFromJsonObject(JSONArray jsonEntityList) throws JSONException {
        mEntityList.clear();
        for (int i = 0; i<jsonEntityList.length(); i++) {
            JSONObject jsonEntity = jsonEntityList.getJSONObject(i);
            IEntity entity = loadEntityFromJsonObject(jsonEntity);
            if (entity != null){
                mEntityList.add(entity);
            }
        }
    }

    public void loadFromJson(JSONObject jsonDrawing) throws JSONException {
        JSONArray jsonEntityList = jsonDrawing.optJSONArray("mEntityList");
        if (jsonEntityList != null) {
            loadEntityListFromJsonObject(jsonEntityList);
        }
    }

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
        setupKeyboardButtonSet();
        setupTestObjects();

        return this;
    }

    private void setupCylinderCanvas(){
        mCylinderCanvasEntity = new CylinderCanvasEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(mCylinderCanvasEntity.getModel(), 0, 0, Constants.CANVAS_CYL_DEPTH, 0);
        mCylinderCanvasEntity.changedModel();
    }

    private void setupFloor(){
        mFloorEntity = new FloorEntity(ShaderCollection.getProgram(Programs.GridProgram));
        Matrix.translateM(mFloorEntity.getModel(), 0, 0, Constants.FLOOR_DEPTH, 0);
        mFloorEntity.changedModel();
    }

    private void setupEntityCreateButtonSet(){
        Class<?>[] appStates = new Class<?>[]{  StateSelectAction.class, StateWaitForBeginFreeLine.class, StateWaitForBeginPolyLinePoint.class, StateWaitForCuboidBasePoint1.class,
                                                StateWaitForCylinderCenterPoint.class, StateWaitForSphereCenterPoint.class, StateWaitForTextInput.class};
        Textures[] textures =  new Textures[]{  Textures.TextureButtonBack, Textures.TextureButtonFreeLine, Textures.TextureButtonPolyLine, Textures.TextureButtonCuboid,
                                                Textures.TextureButtonCylinder, Textures.TextureButtonSphere, Textures.TextureButtonText};
        for (int i=0; i<7; i++) {
            ButtonEntity entity = new ButtonEntity(ShaderCollection.getProgram(Programs.BodyTexturedProgram))
                    .setNextState(appStates[i])
                    .setTextureHandle(ShaderCollection.getTexture(textures[i]));
            entity.setDisplayType(EntityDisplayType.RelativeToCamera);

//            float y = -0.13f;
            float y = 0;
            Matrix.translateM(entity.getBaseModel(), 0, -0.18f + 0.06f * i, y, -0.3f);
            Matrix.scaleM(entity.getBaseModel(), 0, 0.025f, 0.025f, 0.006f);

//            float y = -0.065f;
//            Matrix.translateM(mEntity.getBaseModel(), 0, 0.06f-0.03f*i, y, -0.15f);
//            Matrix.scaleM(mEntity.getBaseModel(), 0, 0.0125f, 0.0125f, 0.003f);
            mEntityCreateButtons.addButton(entity);
        }
    }

    private void setupEntityActionButtonSet(){
        Class<?>[] appStates = new Class<?>[]{StateSelectEntityToMove.class, StateSelectEntityToCopy.class, StateSelectEntityToCreate.class, StateSelectEntityToDelete.class};
        Textures[] textures =  new Textures[]{Textures.TextureButtonMoveEntity, Textures.TextureButtonCopyEntity, Textures.TextureButtonCreateEntity, Textures.TextureButtonDeleteEntity };
        for (int i=0; i<4; i++) {
            ButtonEntity entity = new ButtonEntity(ShaderCollection.getProgram(Programs.BodyTexturedProgram))
                    .setNextState(appStates[i])
                    .setTextureHandle(ShaderCollection.getTexture(textures[i]));
            entity.setDisplayType(EntityDisplayType.RelativeToCamera);

//            float y = -0.13f;
            float y = 0;
            Matrix.translateM(entity.getBaseModel(), 0, -0.09f+0.06f*i, y, -0.3f);
            Matrix.scaleM(entity.getBaseModel(), 0, 0.025f, 0.025f, 0.006f);

//            float y = -0.065f;
//            Matrix.translateM(mEntity.getBaseModel(), 0, 0.06f-0.03f*i, y, -0.15f);
//            Matrix.scaleM(mEntity.getBaseModel(), 0, 0.0125f, 0.0125f, 0.003f);
            mEntityActionButtons.addButton(entity);
        }
    }

    private void setupKeyboardButtonSet(){
//        char[] keyRow0 = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0','\b'};
//        char[] keyRow1 = new char[]{'Q', 'W', 'E', 'R', 'T', 'Z', 'U', 'I', 'O', 'P', 'Ü'};
//        char[] keyRow2 = new char[]{'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Ö', 'Ä'};
//        char[] keyRow3 = new char[]{'<', '>', 'Y', 'X', 'C', 'V', 'B', 'N', 'M', ',', '.'};
//        char[] keyRow4 = new char[]{'+', '-', '*', '/',      ' '     , '?', '!',   '\n'  };

        Textures[] texRow0 = new Textures[]{Textures.TextureKey1, Textures.TextureKey2, Textures.TextureKey3, Textures.TextureKey4,
                                            Textures.TextureKey5, Textures.TextureKey6, Textures.TextureKey7, Textures.TextureKey8,
                                            Textures.TextureKey9, Textures.TextureKey0, Textures.TextureKeyBackSpc};

        Textures[] texRow1 = new Textures[]{Textures.TextureKeyQ, Textures.TextureKeyW, Textures.TextureKeyE, Textures.TextureKeyR,
                                            Textures.TextureKeyT, Textures.TextureKeyZ, Textures.TextureKeyU, Textures.TextureKeyI,
                                            Textures.TextureKeyO, Textures.TextureKeyP, Textures.TextureKeyÜ};

        Textures[] texRow2 = new Textures[]{Textures.TextureKeyA, Textures.TextureKeyS, Textures.TextureKeyD, Textures.TextureKeyF,
                                            Textures.TextureKeyG, Textures.TextureKeyH, Textures.TextureKeyJ, Textures.TextureKeyK,
                                            Textures.TextureKeyL, Textures.TextureKeyÖ, Textures.TextureKeyÄ};

        Textures[] texRow3 = new Textures[]{Textures.TextureKeySmallerThan, Textures.TextureKeyBiggerThan, Textures.TextureKeyY, Textures.TextureKeyX,
                                            Textures.TextureKeyC, Textures.TextureKeyV, Textures.TextureKeyB, Textures.TextureKeyN,
                                            Textures.TextureKeyM, Textures.TextureKeyComma, Textures.TextureKeyDot};

        Textures[] texRow4 = new Textures[]{Textures.TextureKeyPlus, Textures.TextureKeyMinus, Textures.TextureKeyStar, Textures.TextureKeySlash,
                                            Textures.TextureNone, Textures.TextureKeySpace, Textures.TextureNone,
                                            Textures.TextureKeyQuestionMark, Textures.TextureKeyExclamationMark, Textures.TextureKeyEnter, Textures.TextureNone};

        Textures[][] texKeyboard = new Textures[][]{texRow0, texRow1, texRow2, texRow3, texRow4};

        for (int i = 0; i < texKeyboard.length; i++) {
            Textures[] texRow = texKeyboard[i];
            float y = 0.030f - 0.027f*i;

            float halfLength = texRow.length/2;

            for (int j = 0; j < texRow.length; j++) {
                Textures tex = texRow[j];
                ButtonEntity entity = new ButtonEntity(ShaderCollection.getProgram(Programs.BodyTexturedProgram))
                        .setTextureHandle(ShaderCollection.getTexture(tex))
                        .setKey(tex.getValue());
                entity.setDisplayType(EntityDisplayType.RelativeToCamera);

                float x = -0.027f*halfLength + 0.027f*j;
                if (tex == Textures.TextureKeySpace) {
                    Matrix.translateM(entity.getBaseModel(), 0, x, y, -0.30f);
                    Matrix.scaleM(entity.getBaseModel(), 0, 0.040f, 0.012f, 0.003f);
                } else if (tex == Textures.TextureKeyEnter) {
                    Matrix.translateM(entity.getBaseModel(), 0, x+0.0135f, y, -0.30f);
                    Matrix.scaleM(entity.getBaseModel(), 0, 0.025f, 0.012f, 0.003f);
                } else {
                    Matrix.translateM(entity.getBaseModel(), 0, x, y, -0.30f);
                    Matrix.scaleM(entity.getBaseModel(), 0, 0.012f, 0.012f, 0.003f);
                }
                if (tex != Textures.TextureNone) {
                    mKeyboardButtons.addButton(entity);
                }
            }
        }
    }

    private void setupTestObjects(){
        BaseEntity entity;

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        //Matrix.translateM(entity.getModel(), 0, 1, 1, 1.25f);
        //Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        entity.changedModel();
        mEntityList.add(entity);

//        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
//        Matrix.translateM(entity.getModel(), 0, 1, 1, 1.25f);
//        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
//        entity.changedModel();
//        mEntityList.add(entity);
//
//        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
//        Matrix.translateM(entity.getModel(), 0, -1, 2, 1.25f);
//        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
//        entity.changedModel();
//        mEntityList.add(entity);
//
//        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
//        Matrix.translateM(entity.getModel(), 0, 1, 2, -1.25f);
//        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
//        entity.changedModel();
//        mEntityList.add(entity);
//
//        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
//        Matrix.translateM(entity.getModel(), 0, 0, 1, -1.25f);
//        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
//        entity.changedModel();
//        mEntityList.add(entity);
//
//        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
//        Matrix.translateM(entity.getModel(), 0, 1, 3, 1.25f);
//        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
//        entity.changedModel();
//        mEntityList.add(entity);
//
//        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
//        Matrix.translateM(entity.getModel(), 0, -1, 0, 1.25f);
//        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
//        entity.changedModel();
//        mEntityList.add(entity);
//
//        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
//        Matrix.translateM(entity.getModel(), 0, 1, 0, -1.25f);
//        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
//        entity.changedModel();
//        mEntityList.add(entity);
//
//        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
//        Matrix.translateM(entity.getModel(), 0, 0, 0, -1.25f);
//        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
//        entity.changedModel();
//        mEntityList.add(entity);
//
//        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
//        Matrix.translateM(entity.getModel(), 0, 0.05f, 0, -0.50f);
//        Matrix.scaleM(entity.getModel(), 0, 0.055f, 0.055f, 0.055f);
//        entity.changedModel();
//        mEntityList.add(entity);

        CylinderEntity cylEntity = new CylinderEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        cylEntity.setAttributes(new Vec3d(1f, 1, -5.0f), new Vec3d(0, 1, 0), 0.5f, 2, new float[]{1, 0, 0, 1});
        mEntityList.add(cylEntity);

        cylEntity = new CylinderEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        cylEntity.setAttributes(new Vec3d(-1f, 0, -5.0f), new Vec3d(0, 1, 0), 1, 1, new float[]{0.8f, 0.8f, 0.8f, 1});
        mEntityList.add(cylEntity);

        CuboidEntity cuboidEntity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        cuboidEntity.setAttributes(new Vec3d(2f, 0, -5.0f), new Vec3d(0,1,0), 1, 2, 1, new float[]{1, 1, 0, 1});
        mEntityList.add(cuboidEntity);

        SphereEntity sphereEntity = new SphereEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        sphereEntity.setAttributes(new Vec3d(4, 2, 0), new Vec3d(0, 1, 0), 1, new float[]{1, 0.5f, 0, 1});
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
