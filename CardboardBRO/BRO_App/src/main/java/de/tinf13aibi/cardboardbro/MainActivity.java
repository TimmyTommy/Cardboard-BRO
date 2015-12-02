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

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.CuboidEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderCanvasEntity;
import de.tinf13aibi.cardboardbro.Entities.FloorEntity;
import de.tinf13aibi.cardboardbro.Entities.IEntity;
import de.tinf13aibi.cardboardbro.Entities.LineEntity;

public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {
    private List<IEntity> mEntityList;
    private static final String TAG = "MainActivity";

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 2.0f, 0.0f, 1.0f };
    private final float[] lightPosInEyeSpace = new float[4];

    private float[] modelCube;
    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelView;

    private int rotationPos = 0;
    private Boolean rotationDir = true;
    private int score = 0;
    private float objectDistance = 12f;
    private float floorDepth = 10f;

    private Vibrator vibrator;
    private CardboardOverlayView overlayView;

    /**
    * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
    *
    * @param label Label to report in case of error.
    */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
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

        modelCube = new float[16];
        camera = new float[16];
        view = new float[16];
        modelView = new float[16];
        headView = new float[16];
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        overlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        overlayView.show3DToast("Pull the magnet when you find an object.");
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
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
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        int vertexShader = ShaderFunctions.loadGLShader(GLES20.GL_VERTEX_SHADER, getResources().openRawResource(R.raw.light_vertex));
        int gridShader = ShaderFunctions.loadGLShader(GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.grid_fragment));
        int passthroughShader = ShaderFunctions.loadGLShader(GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.passthrough_fragment));

        mEntityList = new ArrayList<>();
        //Create Cylinder Canvas
        BaseEntity mEntity = new CylinderCanvasEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, 0, -2 * floorDepth, 0);
        mEntityList.add(mEntity);
        //Create Floor
        mEntity = new FloorEntity(vertexShader, gridShader);
        Matrix.translateM(mEntity.getModel(), 0, 0, -floorDepth, 0);
        mEntityList.add(mEntity);
        //Create Buttons
        for (int i=0; i<7; i++) {
            mEntity = new ButtonEntity(vertexShader, passthroughShader);
            Matrix.translateM(mEntity.getModel(), 0, 0.18f-0.06f*i, -0.18f, -0.30f);
            Matrix.scaleM(mEntity.getModel(), 0, 0.025f, 0.025f, 0.006f);
            mEntityList.add(mEntity);
        }

        mEntity = new CuboidEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, 0, 0, -1.25f);
        Matrix.scaleM(mEntity.getModel(), 0, 0.01f, 0.01f, 0.01f);
        mEntityList.add(mEntity);

        //Test Lines
        int lineVertexShader = ShaderFunctions.loadGLShader(GLES20.GL_VERTEX_SHADER, getResources().openRawResource(R.raw.vertex_line));
        int lineFragmentShader = ShaderFunctions.loadGLShader(GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.fragment_line));

        mEntity = new LineEntity(lineVertexShader, lineFragmentShader);
        ((LineEntity)mEntity).setVerts(-0.05f, 0, 0, 0.05f, 0, 0);
        Matrix.translateM(mEntity.getModel(), 0, 0, 0, -0.25f);
        mEntityList.add(mEntity);

        mEntity = new LineEntity(lineVertexShader, lineFragmentShader);
        ((LineEntity)mEntity).setVerts(0, -0.05f, 0, 0, 0.05f, 0);
        Matrix.translateM(mEntity.getModel(), 0, 0, 0, -0.25f);
        mEntityList.add(mEntity);
//        mEntity = new LineEntity(lineVertexShader, lineFragmentShader);
//        ((LineEntity)mEntity).setVerts(-0.1f, 0, 0, 0.1f, 0, 0);
//        Matrix.translateM(mEntity.getModel(), 0, 0, 0, -0.15f);
////        Matrix.translateM(mEntity.getModel(), 0, -0.5f, -0.05f, -0.25f);
//        mEntityList.add(mEntity);
//
//        mEntity = new LineEntity(lineVertexShader, lineFragmentShader);
////        Matrix.rotateM(mEntity.getModel(), 0, 90, 0.0f, 0.0f, 1.0f);
////        Matrix.translateM(mEntity.getModel(), 0, -0.5f, -0.02f, -0.25f);
//        ((LineEntity)mEntity).setVerts(0, -0.1f, 0, 0, 0.1f, 0);
//        Matrix.translateM(mEntity.getModel(), 0, 0, 0, -0.15f);
////        Matrix.translateM(mEntity.getModel(), 0, -0.5f, -0.05f, -0.25f);
//        mEntityList.add(mEntity);
        checkGLError("onSurfaceCreated");
    }

    /**
    * Prepares OpenGL ES before we draw a frame.
    *
    * @param headTransform The head transformation in the new frame.
    */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the Model part of the ModelView matrix.
        for (IEntity entity : mEntityList) {
            if (entity instanceof ButtonEntity) {
                if (rotationPos < -250){
                    rotationDir = true;
                } else if (rotationPos > 250) {
                    rotationDir = false;
                }
                int direction = rotationDir ? 1 : -1;
                rotationPos += direction;
                Matrix.rotateM(entity.getModel(), 0, Constants.TIME_DELTA, 0f, direction, 0f);
            } else if (entity instanceof CuboidEntity){
//                Matrix.rotateM(entity.getModel(), 0, Constants.TIME_DELTA, 0.5f, 0.5f, 1.0f);
            }

        }
        //Matrix.rotateM(modelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, Constants.CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.75f, 0.0f);
        headTransform.getHeadView(headView, 0);
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
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
        // Set the position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);
        // Build the ModelView and ModelViewProjection matrices for calculating cube position and light.
        float[] perspective = eye.getPerspective(Constants.Z_NEAR, Constants.Z_FAR);

        for (IEntity entity : mEntityList) {
            if (entity instanceof ButtonEntity) {
                entity.draw(camera, perspective, lightPosInEyeSpace);
            } else if (entity instanceof LineEntity) {
                entity.draw(headView, perspective, lightPosInEyeSpace);
//                entity.draw(view, perspective, lightPosInEyeSpace);
            } else {
                entity.draw(view, perspective, lightPosInEyeSpace);
            }
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    @Override
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");

        if (isLookingAtObject()) {
            score++;
            overlayView.show3DToast("Found it! Look around for another one.\nScore = " + score);
            hideObject();
        } else {
            overlayView.show3DToast("Look around to find the object!");
        }
        // Always give user feedback.
        vibrator.vibrate(50);
    }

    /**
    * Find a new random position for the object.
    *
    * <p>We'll rotate it around the Y-axis so it's out of sight, and then up or down by a little bit.
    */
    private void hideObject() {
        float[] rotationMatrix = new float[16];
        float[] posVec = new float[4];
        // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
        // the object's distance from the user.
        float angleXZ = (float) Math.random() * 180 + 90;
        Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
        float oldObjectDistance = objectDistance;
        objectDistance = (float) Math.random() * 15 + 5;
        float objectScalingFactor = objectDistance / oldObjectDistance;
        Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor, objectScalingFactor);
        Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelCube, 12);

        // Now get the up or down angle, between -20 and 20 degrees.
        float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane, between -40 and 40.
        angleY = (float) Math.toRadians(angleY);
        float newY = (float) Math.tan(angleY) * objectDistance;

        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, posVec[0], newY, posVec[2]);
    }

    /**
    * Check if user is looking at object by calculating where the object is in eye-space.
    *
    * @return true if the user is looking at the object.
    */
    private boolean isLookingAtObject() {
        float[] initVec = { 0, 0, 0, 1.0f };
        float[] objPositionVec = new float[4];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < Constants.PITCH_LIMIT && Math.abs(yaw) < Constants.YAW_LIMIT;
    }

    private boolean isPointingAtObject() {
        float[] initVec = { 0, 0, 0, 1.0f };
        float[] objPositionVec = new float[4];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < Constants.PITCH_LIMIT && Math.abs(yaw) < Constants.YAW_LIMIT;
    }
}
