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
import de.tinf13aibi.cardboardbro.Entities.CuboidEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderCanvasEntity;
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
    private static final String TAG = "MainActivity";

    private User mUser = new User();
    private boolean mMoving = false;
    private boolean mDrawingLine = false;

    private int rotationPos = 0;
    private Boolean rotationDir = true;
//    private int score = 0;
//    private float objectDistance = 12f;

    private Vibrator vibrator;
    private CardboardOverlayView overlayView;

    private void beginDrawingLine(Vec3d point){
        PolyLineEntity polyLineEntity = new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram));
        polyLineEntity.addVert(point);
        mEntityList.add(polyLineEntity);
        mDrawingLine = true;
        vibrator.vibrate(50);
    }

    private void continueDrawingLine(Vec3d point){
        IEntity entity = mEntityList.get(mEntityList.size()-1);
        if (entity instanceof PolyLineEntity) {
            PolyLineEntity polyLineEntity = (PolyLineEntity) entity;
            polyLineEntity.addVert(point);
        } else {
            mDrawingLine = false;
        }
    }

    private void endDrawingLine(Vec3d point){
        IEntity entity = mEntityList.get(mEntityList.size()-1);
        if (entity instanceof PolyLineEntity) {
            PolyLineEntity polyLineEntity = (PolyLineEntity) entity;
            polyLineEntity.addVert(point);
        }
        mDrawingLine = false;
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
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
//                        overlayView.show3DToast("Accelerating");
//                        mMoving = true;
                        overlayView.show3DToast("Begin drawing");
                        beginDrawingLine(mUser.getArmCrosshair().getPosition());
                        break;
                    case MotionEvent.ACTION_UP:
//                        overlayView.show3DToast("Slowing down");
//                        mMoving = false;
                        overlayView.show3DToast("End drawing");
                        endDrawingLine(mUser.getArmCrosshair().getPosition());
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

    private void setupButtons(){
        for (int i=0; i<5; i++) {
            BaseEntity entity = new ButtonEntity(ShaderCollection.getProgram(Programs.BodyProgram));
            entity.setDisplayType(EntityDisplayType.RelativeToCamera);
            float y = -0.13f;
            Matrix.translateM(entity.getBaseModel(), 0, 0.12f-0.06f*i, /*-1.3f*/y, -0.3f);
            Matrix.scaleM(entity.getBaseModel(), 0, 0.025f, 0.025f, 0.006f);
//            float y = -0.065f;
//            Matrix.translateM(mEntity.getBaseModel(), 0, 0.06f-0.03f*i, y, -0.15f);
//            Matrix.scaleM(mEntity.getBaseModel(), 0, 0.0125f, 0.0125f, 0.003f);
            mEntityList.add(entity);
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

        setupButtons();
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
        mUser.calcArmPointingAt(mEntityList);

        if (mDrawingLine){
            continueDrawingLine(mUser.getArmCrosshair().getPosition());
        }

        checkGLError("onReadyToDraw");

        // Build the Model part of the ModelView matrix.
        for (IEntity entity : mEntityList) {
            if (entity.getDisplayType() == EntityDisplayType.RelativeToCamera) {
                //Test: rotiere Buttons
                if (rotationPos < -25){
                    rotationDir = true;
                } else if (rotationPos > 25) {
                    rotationDir = false;
                }
                int direction = rotationDir ? 1 : -1;
                rotationPos += direction;
                Matrix.rotateM(entity.getBaseModel(), 0, Constants.TIME_DELTA * 8, 0f, 0f, direction);

                entity.resetModelToBase();

                float[] finalOp = new float[16];
                float[] anOp = new float[16];
                Matrix.setIdentityM(finalOp, 0);

//                Matrix.setIdentityM(anOp, 0);
//                Matrix.scaleM(anOp, 0, 0.01f, 0.01f, 0.01f);
//                Matrix.multiplyMM(finalOp, 0, anOp, 0, finalOp, 0);

//                Matrix.setIdentityM(anOp, 0);
//                Matrix.translateM(anOp, 0, 0, 0, -0.50f);
//                Matrix.multiplyMM(finalOp, 0, anOp, 0, finalOp, 0);

//                Matrix.setIdentityM(anOp, 0);
//                Matrix.multiplyMM(anOp, 0, mUser.getInvHeadView(), 0, anOp, 0);
//                Matrix.multiplyMM(finalOp, 0, anOp, 0, finalOp, 0);

//                float[] quat = new float[4];
//                headTransform.getQuaternion(quat, 0);
//                for (int k=0; k<3; k++){
//                    quat[k] *= -1;
//                }
//                float[] rotMat = VecMath.calcQuaternionToMatrix(quat);
//                Matrix.multiplyMM(finalOp, 0, rotMat, 0, finalOp, 0);

                Matrix.multiplyMM(finalOp, 0, mUser.getInvHeadView(), 0, finalOp, 0);

                Matrix.setIdentityM(anOp, 0);
                Vec3d pos = mUser.getPosition();
                Matrix.translateM(anOp, 0, pos.x, pos.y, pos.z);
                Matrix.multiplyMM(finalOp, 0, anOp, 0, finalOp, 0);

                Matrix.multiplyMM(entity.getModel(), 0, finalOp, 0, entity.getModel(), 0);
            }
        }
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
            if (entity.getDisplayType() == EntityDisplayType.RelativeToCamera) {
                entity.draw(view, perspective, lightPosInEyeSpace);
//                //Objekte die sich mit Camera mitbewegen sollen, werden mithilfe der IdentMat gezeichnet
//                float[] identMat = new float[16];
//                Matrix.setIdentityM(identMat, 0);
//                entity.draw(identMat, perspective, lightPosInEyeSpace);
            } else {
                if (entity instanceof PolyLineEntity) {
                    entity.draw(view, perspective, lightPosInEyeSpace);
                } else {
                    entity.draw(view, perspective, lightPosInEyeSpace);
                }

            }
        }
        mUser.drawCrosshairs(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

//    @Override
//    public void onCardboardTrigger() {
//        Log.i(TAG, "onCardboardTrigger");
//
//        if (isLookingAtObject()) {
//            score++;
//            overlayView.show3DToast("Found it! Look around for another one.\nScore = " + score);
//            hideObject();
//        } else {
//            overlayView.show3DToast("Look around to find the object!");
//        }
//        // Always give user feedback.
//        vibrator.vibrate(50);
//    }
//
//    /**
//    * Find a new random position for the object.
//    *
//    * <p>We'll rotate it around the Y-axis so it's out of sight, and then up or down by a little bit.
//    */
//    private void hideObject() {
//        float[] rotationMatrix = new float[16];
//        float[] posVec = new float[4];
//        // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
//        // the object's distance from the user.
//        float angleXZ = (float) Math.random() * 180 + 90;
//        Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
//        float oldObjectDistance = objectDistance;
//        objectDistance = (float) Math.random() * 15 + 5;
//        float objectScalingFactor = objectDistance / oldObjectDistance;
//        Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor, objectScalingFactor);
//        Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelCube, 12);
//
//        // Now get the up or down angle, between -20 and 20 degrees.
//        float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane, between -40 and 40.
//        angleY = (float) Math.toRadians(angleY);
//        float newY = (float) Math.tan(angleY) * objectDistance;
//
//        Matrix.setIdentityM(modelCube, 0);
//        Matrix.translateM(modelCube, 0, posVec[0], newY, posVec[2]);
//    }
//
//    /**
//    * Check if user is looking at object by calculating where the object is in eye-space.
//    *
//    * @return true if the user is looking at the object.
//    */
//    private boolean isLookingAtObject() {
//        float[] initVec = { 0, 0, 0, 1.0f };
//        float[] objPositionVec = new float[4];
//
//        // Convert object space to camera space. Use the headView from onNewFrame.
//        Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
//        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);
//
//        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
//        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);
//
//        return Math.abs(pitch) < Constants.PITCH_LIMIT && Math.abs(yaw) < Constants.YAW_LIMIT;
//    }
}
