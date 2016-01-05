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

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonSet;
import de.tinf13aibi.cardboardbro.Entities.CuboidEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderCanvasEntity;
import de.tinf13aibi.cardboardbro.Enums.AppState;
import de.tinf13aibi.cardboardbro.Enums.EntityDisplayType;
import de.tinf13aibi.cardboardbro.Entities.FloorEntity;
import de.tinf13aibi.cardboardbro.Entities.IEntity;
import de.tinf13aibi.cardboardbro.Entities.LineEntity;
import de.tinf13aibi.cardboardbro.Entities.PolyLineEntity;
import de.tinf13aibi.cardboardbro.Enums.Programs;
import de.tinf13aibi.cardboardbro.Enums.Shaders;
import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {
    private ArrayList<IEntity> mEntityList = new ArrayList<>();

    private ButtonSet mEntityActionButtons = new ButtonSet();
    private ButtonSet mEntityCreateButtons = new ButtonSet();
    private ButtonSet mKeyboardButtons = new ButtonSet();

    private static final String TAG = "MainActivity";

    //TODO: evtl nach User auslagern
//    private AppState mAppState = AppState.SelectAction;  //TODO: Bei Appstart default: SelectAction
    private AppState mAppState = AppState.WaitForBeginFreeDraw; //Zu Testzwecken manuell AppState setzen

    private User mUser = new User();
    private boolean mMoving = false;

//    private int score = 0;
//    private float objectDistance = 12f;

    private Vibrator vibrator;
    private CardboardOverlayView overlayView;

    private void beginDrawingLine(Vec3d point){
        PolyLineEntity polyLineEntity = new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram));
        polyLineEntity.addVert(point);
        mEntityList.add(polyLineEntity);
        vibrator.vibrate(50);
    }

    private void continueDrawingLine(Vec3d point){
        IEntity entity = mEntityList.get(mEntityList.size()-1);
        if (entity instanceof PolyLineEntity) {
            PolyLineEntity polyLineEntity = (PolyLineEntity) entity;
            polyLineEntity.addVert(point);
        }
    }

    private void endDrawingLine(Vec3d point){
        IEntity entity = mEntityList.get(mEntityList.size()-1);
        if (entity instanceof PolyLineEntity) {
            PolyLineEntity polyLineEntity = (PolyLineEntity) entity;
            polyLineEntity.addVert(point);
        }
        vibrator.vibrate(50);
    }

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

     /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_ui);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        overlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        overlayView.show3DToast("Push the button to move around.");
        cardboardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        overlayView.show3DToast("Accelerating");
//                        mMoving = true;
                        if (mAppState == AppState.WaitForBeginFreeDraw) {
                            overlayView.show3DToast("Begin drawing");
                            beginDrawingLine(mUser.getArmCrosshair().getPosition());
                            mAppState = AppState.DrawingFreeHand;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
//                        overlayView.show3DToast("Slowing down");
//                        mMoving = false;
                        if (mAppState == AppState.DrawingFreeHand) {
                            overlayView.show3DToast("End drawing");
                            endDrawingLine(mUser.getArmCrosshair().getPosition());
                            mAppState = AppState.WaitForBeginFreeDraw;
                        }
                        break;
                }
                return false;
            }
        });
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
        Matrix.translateM(entity.getModel(), 0, 0, Constants.FLOOR_DEPTH, 0);
//        mEntityList.add(entity); //TODO: später wieder einblenden
    }

    private void setupFloor(){
        BaseEntity entity = new FloorEntity(ShaderCollection.getProgram(Programs.GridProgram));
        Matrix.translateM(entity.getModel(), 0, 0, Constants.FLOOR_DEPTH, 0);
        mEntityList.add(entity);
    }

    private void setupEntityCreateButtonSet(){
        AppState[] appStates = new AppState[]{AppState.SelectAction, AppState.WaitForBeginFreeDraw, AppState.WaitForPolyLinePoint,
                                              AppState.WaitForSphereCenterpoint, AppState.WaitForCylinderCenterPoint, AppState.WaitForCuboidBasePoint1, AppState.WaitForKeyboardInput};
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

        entity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 1, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, -1, 2, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 2, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 0, 1, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 3, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, -1, 0, 1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 1, 0, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 0, 0, -1.25f);
        Matrix.scaleM(entity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(entity);

        entity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        Matrix.translateM(entity.getModel(), 0, 0.05f, 0, -0.50f);
        Matrix.scaleM(entity.getModel(), 0, 0.055f, 0.055f, 0.055f);
        mEntityList.add(entity);

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

        setupCylinderCanvas();
        setupFloor();

        setupEntityActionButtonSet();
        setupEntityCreateButtonSet();
        setupTestObjects();

        mUser.createCrosshairs(ShaderCollection.getProgram(Programs.LineProgram));

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

        Vec3d acceleration = VecMath.calcVecTimesScalar(mUser.getEyeForward(), mMoving ? 5:0);
        mUser.move(acceleration);

        mUser.calcEyeLookingAt(mEntityList);

        ArrayList<IEntity> entityListForArm;
        switch (mAppState){
            case SelectAction:          entityListForArm = mEntityActionButtons.getButtonEntities(); break;
            case SelectEntityToCreate:  entityListForArm = mEntityCreateButtons.getButtonEntities(); break;
            case WaitForKeyboardInput:  entityListForArm = mKeyboardButtons.getButtonEntities(); break;
            default:                    entityListForArm = mEntityList; break;
        }
        mUser.calcArmPointingAt(entityListForArm);

        if (mAppState == AppState.DrawingFreeHand) {
            continueDrawingLine(mUser.getArmCrosshair().getPosition());
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

    //Nicht verwenden, da es kein "onCardboardTriggerRelease" gibt
//    @Override
//    public void onCardboardTrigger() {
//        Log.i(TAG, "onCardboardTrigger");
//          overlayView.show3DToast("onCardboardTrigger");
//        // Always give user feedback.
//        vibrator.vibrate(50);
//    }

}
