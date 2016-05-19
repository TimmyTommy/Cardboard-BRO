package de.tinf13aibi.cardboardbro.Engine;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 20.01.2016.
 */
public class DrawingRenderer implements CardboardView.StereoRenderer{
    private DrawingContext mDrawingContext;

    public DrawingRenderer(DrawingContext drawingContext){
        mDrawingContext = drawingContext;
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        float[] headView = new float[16];
        headTransform.getHeadView(headView, 0);

        Vec3d armForwardVec = mDrawingContext.getMainActivity().getMyoData().getArmForwardVec();
        if (Constants.DEBUG_FLAG) {
            armForwardVec = mDrawingContext.getUser().getEyeForward(); //TODO: ArmForward (Armrichtung) von MYO zuweisen
        }

        mDrawingContext.processOnNewFrame(headView, armForwardVec);

        checkGLError("onReadyToDraw");
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("colorParam");

        // Apply the eye transformation to the camera.
        float[] view = new float[16];
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, mDrawingContext.getUser().getCamera(), 0);

        // Set the position of the light
        final float[] lightPosInEyeSpace = new float[4];
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, Constants.LIGHT_POS_IN_WORLD_SPACE, 0);

        float[] perspective = eye.getPerspective(Constants.Z_NEAR, Constants.Z_FAR);

        mDrawingContext.processOnDrawEye(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        Log.i("DrawingRenderer", "onSurfaceCreated");
        mDrawingContext.addLogEntry(this.getClass().getSimpleName(), "onSurfaceCreated");

        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        mDrawingContext.getMainActivity().initPrograms();

        mDrawingContext.initUser();
        mDrawingContext.initDrawing();

        mDrawingContext.getMainActivity().loadDrawingContext();

        checkGLError("onSurfaceCreated");
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
//        Log.i("DrawingRenderer", "onFinishFrame");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i("DrawingRenderer", "onSurfaceChanged");
        mDrawingContext.addLogEntry(this.getClass().getSimpleName(), "onSurfaceChanged");
    }

    @Override
    public void onRendererShutdown() {
        Log.i("DrawingRenderer", "onRendererShutdown");
        mDrawingContext.addLogEntry(this.getClass().getSimpleName(), "onRendererShutdown");
    }

    private static void checkGLError(String label) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("DrawingRenderer", label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }
}
