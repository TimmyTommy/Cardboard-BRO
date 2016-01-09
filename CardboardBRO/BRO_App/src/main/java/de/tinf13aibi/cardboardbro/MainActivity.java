/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tinf13aibi.cardboardbro;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

import java.util.ArrayList;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonSet;
import de.tinf13aibi.cardboardbro.Entities.CubeEntity;
import de.tinf13aibi.cardboardbro.Entities.CuboidEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderCanvasEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderEntity;
import de.tinf13aibi.cardboardbro.Enums.AppState;
import de.tinf13aibi.cardboardbro.Enums.AppStateGroup;
import de.tinf13aibi.cardboardbro.Enums.EntityDisplayType;
import de.tinf13aibi.cardboardbro.Entities.FloorEntity;
import de.tinf13aibi.cardboardbro.Entities.IEntity;
import de.tinf13aibi.cardboardbro.Entities.PolyLineEntity;
import de.tinf13aibi.cardboardbro.Enums.Programs;
import de.tinf13aibi.cardboardbro.Enums.Shaders;
import de.tinf13aibi.cardboardbro.Geometry.CollisionPlanePoint;
import de.tinf13aibi.cardboardbro.Geometry.CollisionTrianglePoint;
import de.tinf13aibi.cardboardbro.Geometry.Plane;
import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoData;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoDeviceListener;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoListenerTarget;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoStatus;

public class MainActivity extends CardboardActivity implements MyoListenerTarget, CardboardView.StereoRenderer {
    private static final String TAG = "MainActivity";

    private ArrayList<IEntity> mEntityList = new ArrayList<>();
    private ButtonSet mEntityActionButtons = new ButtonSet();
    private ButtonSet mEntityCreateButtons = new ButtonSet();
    private ButtonSet mKeyboardButtons = new ButtonSet();
    private Plane mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(new Vec3d(), new Vec3d(0, 1, 0));

    private MyoData mMyoData = new MyoData();

    //TODO: evtl nach User auslagern
//    private AppState mAppState = AppState.SelectAction;  //TODO: Bei Appstart default: SelectAction
//    private AppState mAppState = AppState.WaitForBeginFreeLine; //Zu Testzwecken manuell AppState setzen
//    private AppState mAppState = AppState.WaitForBeginPolyLinePoint; //Zu Testzwecken manuell AppState setzen
//    private AppState mAppState = AppState.WaitForCylinderCenterPoint; //Zu Testzwecken manuell AppState setzen
    private AppState mAppState = AppState.WaitForCuboidBasePoint1; //Zu Testzwecken manuell AppState setzen

    private IEntity mEditingEntity;

    private Drawing mDrawing = new Drawing();

    private User mUser = new User();
    private StateMachine mStateMachine = new StateMachine(mUser);

    private Date mClickTime; //TODO: nur temporär zum imitieren von "MYO-Gesten"

    private Vibrator vibrator;
    private CardboardOverlayView overlayView;

    //Draw FreeLine
    private void drawFreeLineBegin(Vec3d point){
        mEditingEntity = new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram));
        ((PolyLineEntity)mEditingEntity).addVert(point);
        mEntityList.add(mEditingEntity);

        vibrator.vibrate(50);
        overlayView.show3DToast("Begin FreeDraw --> WaitForEndFreeLine");
        mAppState = AppState.WaitForEndFreeLine;
    }

    private void drawFreeLinePoint(Vec3d point){
        if (mEditingEntity instanceof PolyLineEntity) {
            ((PolyLineEntity)mEditingEntity).addVert(point);
        }
    }

    private void drawFreeLineEnd(Vec3d point){
        drawFreeLinePoint(point);
        vibrator.vibrate(50);
        overlayView.show3DToast("End FreeDraw --> WaitForBeginFreeLine");
        mAppState = AppState.WaitForBeginFreeLine;
    }

    private void drawFreeLineAbort(Boolean leave){
        vibrator.vibrate(50);
        if (leave){
            overlayView.show3DToast("Leave FreeLine Mode --> SelectEntityToCreate");
            mAppState = AppState.SelectEntityToCreate;
        } else {
            if (mEditingEntity instanceof PolyLineEntity) {
                mEntityList.remove(mEditingEntity);
            }
            overlayView.show3DToast("Delete Drawed FreeLine --> WaitForBeginFreeLine");
            mAppState = AppState.WaitForBeginFreeLine;
        }
        mEditingEntity = null;
    }

    //Draw PolyLine
    private void drawPolyLineBegin(Vec3d point){
        mEditingEntity = new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram));
        ((PolyLineEntity)mEditingEntity).addVert(point);
        ((PolyLineEntity)mEditingEntity).addVert(point);
        mEntityList.add(mEditingEntity);
        vibrator.vibrate(50);
        overlayView.show3DToast("Begin PolyLine --> WaitForNextPolyLinePoint");
        mAppState = AppState.WaitForNextPolyLinePoint;
    }

    private void drawPolyLineNextPoint(Vec3d point, Boolean fix){
        if (mEditingEntity instanceof PolyLineEntity) {
            PolyLineEntity polyLineEntity = (PolyLineEntity) mEditingEntity;
            polyLineEntity.changeLastVert(point);
            if (fix) {
                polyLineEntity.addVert(point);
                vibrator.vibrate(50);
                overlayView.show3DToast("Update PolyLine --> WaitForNextPolyLinePoint");
                mAppState = AppState.WaitForNextPolyLinePoint;
            }
        }
    }

    private void drawPolyLineEnd(){
        if (mEditingEntity instanceof PolyLineEntity) {
            ((PolyLineEntity)mEditingEntity).delLastVert();
        }
        vibrator.vibrate(50);
        overlayView.show3DToast("End PolyLine --> WaitForBeginPolyLinePoint");
        mAppState = AppState.WaitForBeginPolyLinePoint;
    }

    private void drawPolyLineLeave(){
        mEditingEntity = null;
        vibrator.vibrate(50);
        overlayView.show3DToast("Leave PolyLine Mode --> SelectEntityToCreate");
        mAppState = AppState.SelectEntityToCreate;
    }

    //Draw Cylinder
    private void drawCylinderBeginCenter(Vec3d point, Vec3d baseNormal){
        mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(point, baseNormal);
        mEditingEntity = new CylinderEntity(ShaderCollection.getProgram(Programs.BodyProgram));

        float[] color = new float[]{0.7f, 0.3f, 0.5f, 1};
        ((CylinderEntity)mEditingEntity).setAttributes(point, baseNormal, 0.1f, 0.1f, color);
        mEntityList.add(mEditingEntity);

        vibrator.vibrate(50);
        overlayView.show3DToast("Begin Draw Cylinder --> WaitForCylinderRadiusPoint");
        mAppState = AppState.WaitForCylinderRadiusPoint;
    }

    private void drawCylinderRadius(Vec3d point, Boolean fix){
        if (mEditingEntity instanceof CylinderEntity) {
            CylinderEntity cylinderEntity = (CylinderEntity) mEditingEntity;
            cylinderEntity.setRadius(VecMath.calcVectorLength(VecMath.calcVecMinusVec(point, cylinderEntity.getCenter())));
            if (fix){

                //TODO das ganze auslagern und mTempWorkingPlane updaten, je nachdem in welche richtung user guckt
                Vec3d baseNormal = cylinderEntity.getBaseNormal().copy();
                Vec3d firstCycleDir = new Vec3d();
                Vec3d secondCycleDir = new Vec3d();
                VecMath.calcCrossedVectorsFromNormal(secondCycleDir, firstCycleDir, baseNormal);

//            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cylinderEntity.getCenter(), firstCycleDir);
                mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cylinderEntity.getCenter(), secondCycleDir);
                vibrator.vibrate(50);
                overlayView.show3DToast("Update Cylinder Radius --> WaitForCylinderHeightPoint");
                mAppState = AppState.WaitForCylinderHeightPoint;
            }
        }
    }

    private void drawCylinderEndHeight(Vec3d point, Boolean fix){
        if (mEditingEntity instanceof CylinderEntity) {
            CylinderEntity cylinderEntity = (CylinderEntity) mEditingEntity;
            Plane basePlane = VecMath.calcPlaneFromPointAndNormal(cylinderEntity.getCenter(), cylinderEntity.getBaseNormal());

            float distance = VecMath.calcDistancePlanePoint(basePlane, point);
            cylinderEntity.setHeight(distance);
        }
        if (fix){
            vibrator.vibrate(50);
            overlayView.show3DToast("End Draw Cylinder --> WaitForCylinderCenterPoint");
            mAppState = AppState.WaitForCylinderCenterPoint;
        }
    }

    private void drawCylinderAbort(Boolean leave){
        vibrator.vibrate(50);
        if (leave){
            overlayView.show3DToast("Leave Cylinder Mode --> SelectEntityToCreate");
            mAppState = AppState.SelectEntityToCreate;
        } else {
            if (mEditingEntity instanceof CylinderEntity) {
                mEntityList.remove(mEditingEntity);
            }
            overlayView.show3DToast("Delete Drawed Cylinder --> WaitForCylinderCenterPoint");
            mAppState = AppState.WaitForCylinderCenterPoint;
        }
        mEditingEntity = null;
    }

    //Draw Cylinder
    private void drawCuboidBeginBasePoint1(Vec3d point, Vec3d baseNormal){
        mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(point, baseNormal);
        mEditingEntity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));

        float[] color = new float[]{0.7f, 0.7f, 0.5f, 1};
        ((CuboidEntity)mEditingEntity).setAttributes(point, baseNormal, 0.1f, 0.1f, 0.1f, color);
        mEntityList.add(mEditingEntity);

        vibrator.vibrate(50);
        overlayView.show3DToast("Begin Draw Cuboid --> WaitForCuboidBasePoint2");
        mAppState = AppState.WaitForCuboidBasePoint2;
    }

    private void drawCuboidBeginBasePoint2(CollisionTrianglePoint collisionPoint, Boolean fix){
        if (mEditingEntity instanceof CuboidEntity && collisionPoint instanceof CollisionPlanePoint) {
            CuboidEntity cuboidEntity = (CuboidEntity) mEditingEntity;
            Vec3d pos = ((CollisionPlanePoint) collisionPoint).mTRS.copy();
            cuboidEntity.setDepthAndWidth(pos.y, pos.z);
            if (fix){

                //TODO das ganze auslagern und mTempWorkingPlane updaten, je nachdem in welche richtung user guckt
                Vec3d baseNormal = cuboidEntity.getBaseNormal().copy();
                Vec3d depthDir = new Vec3d();
                Vec3d widthDir = new Vec3d();
                VecMath.calcCrossedVectorsFromNormal(widthDir, depthDir, baseNormal);

//            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cuboidEntity.getBaseVert(), depthDir);
                mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cuboidEntity.getBaseVert(), widthDir);
                vibrator.vibrate(50);
                overlayView.show3DToast("Update Cuboid Point2 --> WaitForCuboidHeightPoint");
                mAppState = AppState.WaitForCuboidHeightPoint;
            }
        }
    }

    private void drawCuboidEndHeight(Vec3d point, Boolean fix){
        if (mEditingEntity instanceof CuboidEntity) {
            CuboidEntity cuboidEntity = (CuboidEntity) mEditingEntity;
            Plane basePlane = VecMath.calcPlaneFromPointAndNormal(cuboidEntity.getBaseVert(), cuboidEntity.getBaseNormal());

            float distance = VecMath.calcDistancePlanePoint(basePlane, point);
            cuboidEntity.setHeight(distance);
        }
        if (fix){
            vibrator.vibrate(50);
            overlayView.show3DToast("End Draw Cuboid --> WaitForCuboidBasePoint1");
            mAppState = AppState.WaitForCuboidBasePoint1;
        }
    }

    private void drawCuboidAbort(Boolean leave){
        vibrator.vibrate(50);
        if (leave){
            overlayView.show3DToast("Leave Cuboid Mode --> SelectEntityToCreate");
            mAppState = AppState.SelectEntityToCreate;
        } else {
            if (mEditingEntity instanceof CuboidEntity) {
                mEntityList.remove(mEditingEntity);
            }
            overlayView.show3DToast("Delete Drawed Cuboid --> WaitForCuboidBasePoint1");
            mAppState = AppState.WaitForCuboidBasePoint1;
        }
        mEditingEntity = null;
    }

    private void processPoseAndAppStateFreeLine(Pose pose, AppState appState){
        switch (appState){
            case WaitForBeginFreeLine:
                switch (pose){
                    case FIST: drawFreeLineBegin(mUser.getArmCrosshair().getPosition()); break;
                    case FINGERS_SPREAD: drawFreeLineAbort(true); break;
                }
                break;
            case WaitForEndFreeLine:
                switch (pose){
                    case FIST: drawFreeLineEnd(mUser.getArmCrosshair().getPosition()); break;
                    case FINGERS_SPREAD: drawFreeLineAbort(false); break;
                }
                break;
        }
    }

    private void processPoseAndAppStatePolyLine(Pose pose, AppState appState){
        switch (appState){
            case WaitForBeginPolyLinePoint:
                switch (pose){
                    case FIST: drawPolyLineBegin(mUser.getArmCrosshair().getPosition()); break;
                    case FINGERS_SPREAD: drawPolyLineLeave(); break;
                }
                break;
            case WaitForNextPolyLinePoint:
                switch (pose){
                    case FIST: drawPolyLineNextPoint(mUser.getArmCrosshair().getPosition(), true); break;
                    case FINGERS_SPREAD: drawPolyLineEnd(); break;
                }
                break;
        }
    }

    private void processPoseAndAppStateCylinder(Pose pose, AppState appState){
        switch (appState){
            case WaitForCylinderCenterPoint:
                switch (pose){
                    case FIST: drawCylinderBeginCenter(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal()); break;
                    case FINGERS_SPREAD: drawCylinderAbort(true); break;
                }
                break;
            case WaitForCylinderRadiusPoint:
                switch (pose){
                    case FIST: drawCylinderRadius(mUser.getArmCrosshair().getPosition(), true); break;
                    case FINGERS_SPREAD: drawCylinderAbort(false); break;
                }
                break;
            case WaitForCylinderHeightPoint:
                switch (pose){
                    case FIST: drawCylinderEndHeight(mUser.getArmCrosshair().getPosition(), true); break;
                    case FINGERS_SPREAD: drawCylinderAbort(false); break;
                }
                break;
        }
    }

    private void processPoseAndAppStateCuboid(Pose pose, AppState appState){
        switch (appState){
            case WaitForCuboidBasePoint1:
                switch (pose){
                    case FIST: drawCuboidBeginBasePoint1(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal()); break;
                    case FINGERS_SPREAD: drawCuboidAbort(true); break;
                }
                break;
            case WaitForCuboidBasePoint2:
                switch (pose){
//                    case FIST: drawCuboidBeginBasePoint2(mUser.getArmCrosshair().getPosition(), true); break;
                    case FIST: drawCuboidBeginBasePoint2(mUser.getArmPointingAt(), true); break;
                    case FINGERS_SPREAD: drawCuboidAbort(false); break;
                }
                break;
            case WaitForCuboidHeightPoint:
                switch (pose){
                    case FIST: drawCuboidEndHeight(mUser.getArmCrosshair().getPosition(), true); break;
                    case FINGERS_SPREAD: drawCuboidAbort(false); break;
                }
                break;
        }
    }

    private void processPoseAndAppState(Pose pose, AppState appState){
        AppStateGroup appStateGroup = AppStateGroup.getFromAppState(appState);
        switch (appStateGroup) {
            case G_DrawFreeLine: processPoseAndAppStateFreeLine(pose, appState); break;
            case G_DrawPolyLine: processPoseAndAppStatePolyLine(pose, appState); break;
            case G_DrawCylinder: processPoseAndAppStateCylinder(pose, appState); break;
            case G_DrawCuboid:   processPoseAndAppStateCuboid(pose, appState); break;
            case G_DrawSphere: break;
            case G_WriteText: break;
            case G_SelectButton: break;
            case G_MoveEntity: break;
            case G_DeleteEntity: break;
            case G_Unknown: mAppState = AppState.SelectAction; break;
        }
    }

    private void initOnTouchListener(){
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);
        cardboardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //TODO Moving später wieder aktiviedern
//                        overlayView.show3DToast("Accelerating");
//                        mUser.setMoving(true);
                        mClickTime = new Date();
                        break;
                    case MotionEvent.ACTION_UP:
                        //TODO Moving später wieder aktiviedern
//                        overlayView.show3DToast("Slowing down");
                        mUser.setMoving(false);
                        Date diffBetweenDownAndUp = new Date(new Date().getTime() - mClickTime.getTime());
                        float timeSeconds = diffBetweenDownAndUp.getTime() * 0.001f;

                        if (timeSeconds <= 1) { //TODO : Wechsel von FIST auf REST imitieren
                            OnPoseChange(Pose.FIST, Pose.REST);
                        } else if (timeSeconds > 1) {  //TODO : Wechsel von FINGERS_SPREAD auf REST imitieren
                            OnPoseChange(Pose.FINGERS_SPREAD, Pose.REST);
                        }
                        //TODO : MYO-Waveout imitieren
                }
                return false;
            }
        });
    }

    private void initializeMyoHub() {
        MyoDeviceListener.getInstance().addTarget(this);
        Hub myoHub = Hub.getInstance();
        try {
            if (!myoHub.init(this)) {
                overlayView.show3DToast("Could not initialize MYO Hub");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initializeMYOListenerForHub(myoHub);
    }

    private void initializeMYOListenerForHub(Hub hub) {
        try {
            hub.removeListener(MyoDeviceListener.getInstance());
            hub.addListener(MyoDeviceListener.getInstance());
            hub.setLockingPolicy(Hub.LockingPolicy.NONE);
            if (hub.getConnectedDevices().size() == 0) {
//                hub.attachToAdjacentMyo(); //TODO später aktivieren
            }
        } catch (Exception e) {
            overlayView.show3DToast("Could not initialize MYO Listener");
        }
    }

     /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_ui);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        overlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        overlayView.show3DToast("Push the button to move around.");

        initOnTouchListener();
        initializeMyoHub();
        OnUpdateStatus(MyoDeviceListener.getInstance().getStatus());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyoDeviceListener.getInstance().removeTarget(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OnUpdateStatus(MyoDeviceListener.getInstance().getStatus());
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    private void initShaders(){
        ShaderCollection.loadGLShader(Shaders.BodyVertexShader, GLES20.GL_VERTEX_SHADER, getResources().openRawResource(R.raw.light_vertex));
        ShaderCollection.loadGLShader(Shaders.BodyFragmentShader, GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.passthrough_fragment));
        ShaderCollection.loadGLShader(Shaders.GridVertexShader, GLES20.GL_VERTEX_SHADER, getResources().openRawResource(R.raw.light_vertex));
        ShaderCollection.loadGLShader(Shaders.GridFragmentShader, GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.grid_fragment));
        ShaderCollection.loadGLShader(Shaders.LineVertexShader, GLES20.GL_VERTEX_SHADER, getResources().openRawResource(R.raw.vertex_line));
        ShaderCollection.loadGLShader(Shaders.LineFragmentShader, GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.fragment_line));
    }

    private void initPrograms(){
        initShaders();
        ShaderCollection.addProgram(Programs.BodyProgram, Shaders.BodyVertexShader, Shaders.BodyFragmentShader);
        ShaderCollection.addProgram(Programs.GridProgram, Shaders.GridVertexShader, Shaders.GridFragmentShader);
        ShaderCollection.addProgram(Programs.LineProgram, Shaders.LineVertexShader, Shaders.LineFragmentShader);
    }

    private void setupCylinderCanvas(){
        BaseEntity entity = new CylinderCanvasEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 0, Constants.CANVAS_CYL_DEPTH, 0);
        mEntityList.add(entity);
    }

    private void setupFloor(){
        BaseEntity entity = new FloorEntity(ShaderCollection.getProgram(Programs.GridProgram));
        Matrix.translateM(entity.getModel(), 0, 0, Constants.FLOOR_DEPTH, 0);
        mEntityList.add(entity);
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
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, -1, 2, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 2, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 0, 1, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 3, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, -1, 0, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 0, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 0, 0, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CubeEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 0.05f, 0, -0.50f);
        Matrix.scaleM(entity.getModel(), 0, 0.055f, 0.055f, 0.055f);
        mEntityList.add(entity);

        CylinderEntity cylEntity = new CylinderEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        cylEntity.setColor(new float[]{1, 0, 0, 1});
        cylEntity.setHeight(2);
        cylEntity.setRadius(0.5f);
        Matrix.translateM(cylEntity.getModel(), 0, 1f, 1, -5.0f);
        mEntityList.add(cylEntity);

        cylEntity = new CylinderEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        cylEntity.setColor(new float[]{0, 0, 1, 1});
        cylEntity.setHeight(-1);
        cylEntity.setRadius(1);
        Matrix.translateM(cylEntity.getModel(), 0, -1f, 0, -5.0f);
        mEntityList.add(cylEntity);

        CuboidEntity cuboidEntity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        cuboidEntity.setAttributes(new Vec3d(2f, 0, -5.0f), new Vec3d(0,1,0), -1, -2, -1, new float[]{1, 1, 0, 1});
        mEntityList.add(cuboidEntity);

//Test Polyline
//        PolyLineEntity polyLineEntity = new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram));
//        polyLineEntity.addVert(new Vec3d(0, 0, 0));
//        polyLineEntity.addVert(new Vec3d(0, 0, -10));
//        polyLineEntity.addVert(new Vec3d(0, 2, -10));
//        polyLineEntity.addVert(new Vec3d(0, 2, -5));
//        polyLineEntity.addVert(new Vec3d(8, 2, -5));
//        mEntityList.add(polyLineEntity);
    }

    /**
    * Creates the buffers we use to store information about the 3D world.
    *
    * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
    * Hence we use ByteBuffers.
    *
    * @param config The EGL configuration used when creating the surface.
    */
    @Override
    public void onSurfaceCreated(EGLConfig config){
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.
        initPrograms();

        mUser.createCrosshairs(ShaderCollection.getProgram(Programs.LineProgram));

        mDrawing.init();

        setupCylinderCanvas();
        setupFloor();

        setupEntityActionButtonSet();
        setupEntityCreateButtonSet();
        setupTestObjects();

        checkGLError("onSurfaceCreated");
    }

    /**
    * Prepares OpenGL ES before we draw a frame.
    *
    * @param headTransform The head transformation in the new frame.
    */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        float[] headView = new float[16];
        headTransform.getHeadView(headView, 0);

        mUser.setHeadView(headView);
        mUser.getArmForward().assignPoint3d(mUser.getEyeForward()); //TODO: ArmForward (Armrichtung) von MYO zuweisen

        mUser.move();

        mUser.calcEyeLookingAt(mEntityList);

        ArrayList<IEntity> entityListForArm;
        switch (mAppState){
            case SelectAction:          entityListForArm = mEntityActionButtons.getButtonEntities(); break;
            case SelectEntityToCreate:  entityListForArm = mEntityCreateButtons.getButtonEntities(); break;
            case WaitForKeyboardInput:  entityListForArm = mKeyboardButtons.getButtonEntities(); break;
            default:                    entityListForArm = mEntityList; break;
        }

        if (mAppState == AppState.WaitForCylinderRadiusPoint){
            mUser.calcArmPointingAt(mTempWorkingPlane);
        }
        else if (mAppState == AppState.WaitForCylinderHeightPoint) {
//            CylinderEntity cylEnt = (CylinderEntity) mEntityList.get(mEntityList.size() - 1);
//            Vec3d cylCenter = cylEnt.getCenter();
////            mTempWorkingPlane.setP1(new Vec3d(cylCenter.x, 0, cylCenter.z));
////            mTempWorkingPlane.setP2(new Vec3d(cylCenter.x, 1, cylCenter.z));
////            mTempWorkingPlane.setP3(new Vec3d(cylCenter.x, 0, cylCenter.z+1));
//
////            mTempWorkingPlane.setP1(new Vec3d(cylCenter.x, 0, cylCenter.z));
////            mTempWorkingPlane.setP2(new Vec3d(cylCenter.x, 1, cylCenter.z));
////            Vec3d dir = VecMath.calcCrossProduct(new Vec3d(0, 1, 0), mUser.getArmForward());
////            Vec3d pnt = VecMath.calcVecPlusVec(new Vec3d(cylCenter.x, 0, cylCenter.z), dir);
////            mTempWorkingPlane.setP3(pnt);
//
////            Vec3d baseNormal = cylEnt.getBaseNormal().copy();
////            Vec3d firstCycleDir = new Vec3d();
////            Vec3d secondCycleDir = new Vec3d();
////            VecMath.calcCrossedVectorsFromNormal(secondCycleDir, firstCycleDir, baseNormal);
////
////            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cylCenter, firstCycleDir);
//
//
////            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cylCenter, mUser.getArmForward());
            mUser.calcArmPointingAt(mTempWorkingPlane);
        }
        else if (mAppState == AppState.WaitForCuboidBasePoint2) {
            mUser.calcArmPointingAt(mTempWorkingPlane);
        }
        else if (mAppState == AppState.WaitForCuboidHeightPoint) {
            mUser.calcArmPointingAt(mTempWorkingPlane);
        }
        else {
            mUser.calcArmPointingAt(entityListForArm);
        }

        if (mAppState == AppState.WaitForEndFreeLine) {
            drawFreeLinePoint(mUser.getArmCrosshair().getPosition());
        }
        else if (mAppState == AppState.WaitForNextPolyLinePoint) {
            drawPolyLineNextPoint(mUser.getArmCrosshair().getPosition(), false);
        }
        else if (mAppState == AppState.WaitForCylinderRadiusPoint) {
            drawCylinderRadius(mUser.getArmCrosshair().getPosition(), false);
        }
        else if (mAppState == AppState.WaitForCylinderHeightPoint) {
            drawCylinderEndHeight(mUser.getArmCrosshair().getPosition(), false);
        }
        else if (mAppState == AppState.WaitForCuboidBasePoint2) {
            drawCuboidBeginBasePoint2(mUser.getArmPointingAt(), false);
        }
        else if (mAppState == AppState.WaitForCuboidHeightPoint) {
            drawCuboidEndHeight(mUser.getArmCrosshair().getPosition(), false);
        }

        mEntityActionButtons.rotateStep();
        mEntityCreateButtons.rotateStep();

        mEntityActionButtons.setButtonsRelativeToCamera(mUser.getInvHeadView(), mUser.getPosition());
        mEntityCreateButtons.setButtonsRelativeToCamera(mUser.getInvHeadView(), mUser.getPosition());

        checkGLError("onReadyToDraw");
    }

    /**
    * Draws a frame for an eye.
    *
    * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("colorParam");
        // Apply the eye transformation to the camera.
        float[] view = new float[16];
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, mUser.getCamera(), 0);

        // Set the position of the light
        final float[] lightPosInEyeSpace = new float[4];
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, Constants.LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices for calculating cube position and light.
        float[] perspective = eye.getPerspective(Constants.Z_NEAR, Constants.Z_FAR);

        for (IEntity entity : mEntityList) {
            entity.draw(view, perspective, lightPosInEyeSpace);
        }

        mUser.drawCrosshairs(view, perspective, lightPosInEyeSpace);

        switch (mAppState) {
            case SelectAction: mEntityActionButtons.draw(view, perspective, lightPosInEyeSpace); break;
            case SelectEntityToCreate: mEntityCreateButtons.draw(view, perspective, lightPosInEyeSpace); break;
            case WaitForKeyboardInput: mKeyboardButtons.draw(view, perspective, lightPosInEyeSpace); break;
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    @Override
    public void OnPoseChange(Pose previousPose, Pose newPose) {
        reactOnPoseChange(previousPose, newPose);
    }

    @Override
    public void OnArmForwardUpdate(Quaternion armForward) {
        mMyoData.setArmForward(armForward);
    }

    @Override
    public void OnArmCenterUpdate(Quaternion armForwardCenter) {
        overlayView.show3DToast("TODO: react OnArmCenterUpdate");
        mMyoData.setArmForwardCenter(armForwardCenter);
    }

    @Override
    public void OnUpdateStatus(MyoStatus status) {
        mMyoData.setMyoStatus(status);
        reactOnStatus(status);
    }

    private void reactOnPoseChange(Pose previousPose, Pose newPose){
//        overlayView.show3DToast("TODO: react on Pose Change:\n" + previousPose.toString() + " --> " + pose.toString());
        if (newPose == Pose.REST) {
            processPoseAndAppState(previousPose, mAppState);
        }
    }

    private void reactOnStatus(MyoStatus status){
        overlayView.show3DToast("TODO: react on Status: " + status.getValue());
    }

    //Nicht verwenden, da es kein "onCardboardTriggerRelease" gibt
//    @Override
//    public void onCardboardTrigger() {
//        Log.i(TAG, "onCardboardTrigger");
//          overlayView.show3DToast("onCardboardTrigger");
//        // Always give user feedback.
//        vibrator.vibrate(50);
//    }

}
