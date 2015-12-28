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
import java.util.Arrays;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.CrosshairEntity;
import de.tinf13aibi.cardboardbro.Entities.CuboidEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderCanvasEntity;
import de.tinf13aibi.cardboardbro.Entities.EntityDisplayType;
import de.tinf13aibi.cardboardbro.Entities.FloorEntity;
import de.tinf13aibi.cardboardbro.Entities.IEntity;
import de.tinf13aibi.cardboardbro.Entities.LineEntity;
import de.tinf13aibi.cardboardbro.Entities.User;
import de.tinf13aibi.cardboardbro.Geometry.CollisionDrawingSpacePoints;
import de.tinf13aibi.cardboardbro.Geometry.CollisionTrianglePoint;
import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;
import de.tinf13aibi.cardboardbro.Geometry.StraightLine;

public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {
    private ArrayList<IEntity> mEntityList;
    private static final String TAG = "MainActivity";

    private User mUser = new User();
    private boolean mMoving = false;

    //TODO nach Klasse USER auslagern
    private CollisionTrianglePoint eyeCollision, armCollision;
    private CrosshairEntity eyeCross;
    private LineEntity eyeLine;

    private int rotationPos = 0;
    private Boolean rotationDir = true;
    private int score = 0;
    private float objectDistance = 12f;
    private float floorDepth = 10f; //TODO nach Constants auslagern

    private Vibrator vibrator;
    private CardboardOverlayView overlayView;

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
        overlayView.show3DToast("Pull the magnet when you find an object.");
        cardboardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        overlayView.show3DToast("Accelerating");
                        mMoving = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        overlayView.show3DToast("Slowing down");
                        mMoving = false;
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
        for (int i=0; i<5; i++) {
            mEntity = new ButtonEntity(vertexShader, passthroughShader);
            mEntity.setDisplayType(EntityDisplayType.RelativeToCamera);
            Matrix.translateM(mEntity.getBaseModel(), 0, 1.2f-0.6f*i, -1.3f, -3f);
            Matrix.scaleM(mEntity.getBaseModel(), 0, 0.25f, 0.25f, 0.06f);
            mEntityList.add(mEntity);
        }

        mEntity = new CuboidEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, 1, 1, 1.25f);
        Matrix.scaleM(mEntity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(mEntity);

        mEntity = new CuboidEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, -1, 2, 1.25f);
        Matrix.scaleM(mEntity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(mEntity);

        mEntity = new CuboidEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, 1, 2, -1.25f);
        Matrix.scaleM(mEntity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(mEntity);

        mEntity = new CuboidEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, 0, 1, -1.25f);
        Matrix.scaleM(mEntity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(mEntity);

        mEntity = new CuboidEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, 1, 3, 1.25f);
        Matrix.scaleM(mEntity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(mEntity);

        mEntity = new CuboidEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, -1, 0, 1.25f);
        Matrix.scaleM(mEntity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(mEntity);

        mEntity = new CuboidEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, 1, 0, -1.25f);
        Matrix.scaleM(mEntity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(mEntity);

        mEntity = new CuboidEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, 0, 0, -1.25f);
        Matrix.scaleM(mEntity.getModel(), 0, 0.1f, 0.1f, 0.1f);
        mEntityList.add(mEntity);

        mEntity = new CuboidEntity(vertexShader, passthroughShader);
        Matrix.translateM(mEntity.getModel(), 0, 0.05f, 0, -0.50f);
        Matrix.scaleM(mEntity.getModel(), 0, 0.055f, 0.055f, 0.055f);
        mEntityList.add(mEntity);

        //Line Shader
        int lineVertexShader = ShaderFunctions.loadGLShader(GLES20.GL_VERTEX_SHADER, getResources().openRawResource(R.raw.vertex_line));
        int lineFragmentShader = ShaderFunctions.loadGLShader(GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.fragment_line));

        //Blickgerade
        eyeLine = new LineEntity(lineVertexShader, lineFragmentShader);
        eyeLine.setVerts(0, 0, 0, 0, 0, -1000);
        eyeLine.setColor(0, 1, 1, 1);

        eyeCross = new CrosshairEntity(lineVertexShader, lineFragmentShader);

        checkGLError("onSurfaceCreated");
    }

    private CollisionTrianglePoint getNearestCollision(StraightLine line){
        return new CollisionDrawingSpacePoints(line, mEntityList).nearestCollision;
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

        Vec3d forwardVec = new Vec3d(0, 0, -1);
        float[] forwardInv = new float[4];

        Matrix.multiplyMV(forwardInv, 0, mUser.getInvHeadView(), 0, forwardVec.toFloatArray4d(), 0);
        mUser.getEyeForward().assignFloatArray(VecMath.calcNormalizedVector(forwardInv));

        Vec3d acceleration = VecMath.calcVecTimesScalar(mUser.getEyeForward(), mMoving ? 5:0);
        mUser.move(acceleration);

        //Position von CrossEntity berechnen : TODO: nach Klasse "User" auslagern
        StraightLine eyeLine = new StraightLine(mUser.getPosition(), new Vec3d(forwardInv));
        eyeCollision = getNearestCollision(eyeLine);

        if (eyeCollision!=null) {
            Vec3d distanceVec = VecMath.calcVecMinusVec(eyeCollision.collisionPos, mUser.getPosition());
            float distance = VecMath.calcVectorLength(distanceVec);
            eyeCross.setPosition(eyeCollision.collisionPos, eyeCollision.triangleNormal, distance);
        } else {
            //calc cross some meters away from eyes
            Vec3d farPointOnEyeLine = VecMath.calcVecPlusVec(mUser.getPosition(), new Vec3d(VecMath.calcVecTimesScalar(forwardInv, 100)));
            Vec3d distanceVec = VecMath.calcVecMinusVec(farPointOnEyeLine, mUser.getPosition());
            float distance = VecMath.calcVectorLength(distanceVec);
            eyeCross.setPosition(farPointOnEyeLine, new Vec3d(forwardInv), distance);
        }
        Vec3d pointOnEyeLineBehind = VecMath.calcVecPlusVec(mUser.getPosition(), new Vec3d(VecMath.calcVecTimesScalar(forwardInv, -10)));
        this.eyeLine.setVerts(pointOnEyeLineBehind.toFloatArray(), eyeCross.getPosition().toFloatArray());

        //Todo get forward-Vector and Line from Arm (MYO)
//        CollisionTrianglePoint nearestArmCollision = getNearestCollision(armLine);

        checkGLError("onReadyToDraw");

        // Build the Model part of the ModelView matrix.
        for (IEntity entity : mEntityList) {
            if (entity instanceof ButtonEntity) {
//                //Test: rotiere Buttons
                if (rotationPos < -25){
                    rotationDir = true;
                } else if (rotationPos > 25) {
                    rotationDir = false;
                }
                int direction = rotationDir ? 1 : -1;
                rotationPos += direction;
                Matrix.rotateM(entity.getBaseModel(), 0, Constants.TIME_DELTA*8, 0f, 0f, direction);
                entity.resetModelToBase();
                //Invertierte HeadView-Matrix auf Objekte drauf rechnen, die sich mit Kopf mitbewegen sollen, weil: Headview * Head^-1 = IdentMat
//                Matrix.multiplyMM(entity.getModel(), 0, mUser.getHeadView(), 0, entity.getModel(), 0);
            }
//            else if (entity instanceof LineEntity){
//                entity.resetModelToBase();
//                if (((LineEntity)entity).getDisplayType()==EntityDisplayType.RelativeToCamera) {
//                    Matrix.multiplyMM(entity.getModel(), 0, invertedHead, 0, entity.getModel(), 0);
//                }
//            }
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
            if (((BaseEntity)entity).getDisplayType() == EntityDisplayType.RelativeToCamera) {
//                entity.draw(view, perspective, lightPosInEyeSpace);
//                entity.draw(mUser.getInvHeadView(), perspective, lightPosInEyeSpace);

                //Objekte die sich mit Camera mitbewegen sollen, werden mithilfe der IdentMat gezeichnet
                float[] identMat = new float[16];
                Matrix.setIdentityM(identMat, 0);
                entity.draw(identMat, perspective, lightPosInEyeSpace);
            } else {
                entity.draw(view, perspective, lightPosInEyeSpace);
            }
//            if (entity instanceof ButtonEntity) {
////                entity.draw(view, perspective, lightPosInEyeSpace);
////                entity.draw(mUser.getInvHeadView(), perspective, lightPosInEyeSpace);
//
//                //Objekte die sich mit Camera mitbewegen sollen, werden mithilfe der IdentMat gezeichnet
//                float[] identMat = new float[16];
//                Matrix.setIdentityM(identMat, 0);
//                entity.draw(identMat, perspective, lightPosInEyeSpace);
//            } else if (entity instanceof LineEntity) {
//                entity.draw(view, perspective, lightPosInEyeSpace);
//            } else if (entity instanceof CylinderCanvasEntity) {
//                entity.draw(view, perspective, lightPosInEyeSpace);
//            } else {
//                entity.draw(view, perspective, lightPosInEyeSpace);
//            }
        }
        eyeCross.draw(view, perspective, lightPosInEyeSpace);
//        eyeLine.draw(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

//    @Override
//    public void onCardboardTrigger() {
//        eyePoint.x += 1f;
//        centerOfView.x += 1f;
//
//        eyePoint.y++;
//        centerOfView.y++;
//        Matrix.setLookAtM(camera, 0, eyePoint.x, eyePoint.y, eyePoint.z,
//                                    centerOfView.x, centerOfView.y, centerOfView.z,
//                                    upVector.x, upVector.y, upVector.z);
//
//        Log.i("Test Normalvector", Arrays.toString(VecMath.calcNormalVector2(1)));
//        Log.i("Test Normalvec inverse", Arrays.toString(VecMath.calcNormalVector2(-1)));
//
//        float[] intersectPoint = new float[3];
//        boolean intersection = VecMath.calcTriangleLineIntersection(intersectPoint);
//
//        Log.i("intersection", Boolean.toString(intersection));
//        Log.i("intersectPoint", Arrays.toString(intersectPoint));
//
//        Log.i("Test", "(0, 0, 1)");
//        VecMath.calcCrossVectors(new Vec3d(0, 0, 1));
//
//        Log.i("Test", "(0, 1, 1)");
//        VecMath.calcCrossVectors(new Vec3d(0, 1, 1));
//
//        Log.i("Test", "(1, 0, 0)");
//        VecMath.calcCrossVectors(new Vec3d(1, 0, 0));
//
//        Log.i("Test", "(1, 1, 0)");
//        VecMath.calcCrossVectors(new Vec3d(1, 1, 0));
//
//        Log.i("Test", "(1, 0, 1)");
//        VecMath.calcCrossVectors(new Vec3d(1, 0, 1));
//
//        Log.i("Test", "(1, 1, 1)");
//        VecMath.calcCrossVectors(new Vec3d(1, 1, 1));
//
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
