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
import com.thalmic.myo.Hub;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;

import de.tinf13aibi.cardboardbro.Enums.InputAction;
import de.tinf13aibi.cardboardbro.Enums.Programs;
import de.tinf13aibi.cardboardbro.Enums.Shaders;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoData;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoDeviceListener;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoListenerTarget;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoStatus;

public class MainActivity extends CardboardActivity implements MyoListenerTarget, CardboardView.StereoRenderer {
    private static final String TAG = "MainActivity";
    private MyoData mMyoData = new MyoData();

    private StateMachine mStateMachine;

    private Vibrator mVibrator;
    private CardboardOverlayView mOverlayView;

    private Date mClickTime; //TODO: nur temporär zum imitieren von "MYO-Gesten"

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
//                        mOverlayView.show3DToast("Accelerating");
//                        mStateMachine.setUserMoving(true);
                        mClickTime = new Date();
                        break;
                    case MotionEvent.ACTION_UP:
//                        mOverlayView.show3DToast("Slowing down");
                        mStateMachine.setUserMoving(false);

                        Date diffBetweenDownAndUp = new Date(new Date().getTime() - mClickTime.getTime());
                        float timeSeconds = diffBetweenDownAndUp.getTime() * 0.001f;

                        if (timeSeconds <= 1) { //Wechsel von FIST auf REST imitieren
                            OnPoseChange(Pose.FIST, Pose.REST);
                        } else if (timeSeconds > 1) {  //Wechsel von FINGERS_SPREAD auf REST imitieren
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
                mOverlayView.show3DToast("Could not initialize MYO Hub");
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
            mOverlayView.show3DToast("Could not initialize MYO Listener");
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

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlayView.show3DToast("Push the button to move around.");

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

        mStateMachine = new StateMachine(mVibrator, mOverlayView);


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

        mStateMachine.getUser().setHeadView(headView);
        mStateMachine.getUser().setArmForward(mStateMachine.getUser().getEyeForward());
        //TODO: ArmForward (Armrichtung) von MYO zuweisen
//        mStateMachine.getUser().setArmForward(mMyoData.getArmForwardVec());

        mStateMachine.processAppStateOnNewFrame();
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
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, mStateMachine.getUser().getCamera(), 0);

        // Set the position of the light
        final float[] lightPosInEyeSpace = new float[4];
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, Constants.LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices for calculating cube position and light.
        float[] perspective = eye.getPerspective(Constants.Z_NEAR, Constants.Z_FAR);

        mStateMachine.processAppStateOnDrawEye(view, perspective, lightPosInEyeSpace);
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
        mMyoData.setPose(newPose);
        reactOnPoseChange(previousPose, newPose);
    }

    @Override
    public void OnArmForwardUpdate(Quaternion armForward) {
        mMyoData.setArmForward(armForward);
    }

    @Override
    public void OnArmCenterUpdate(Quaternion armForwardCenter) {
        mOverlayView.show3DToast("TODO: react OnArmCenterUpdate");
        mMyoData.setArmForwardCenter(armForwardCenter);
    }

    @Override
    public void OnUpdateStatus(MyoStatus status) {
        mMyoData.setMyoStatus(status);
        reactOnStatus(status);
    }

    private void reactOnPoseChange(Pose previousPose, Pose newPose){
        InputAction inputAction = getInputActionByPoseChange(previousPose, newPose);
        mStateMachine.processInputActionAndAppState(inputAction);
    }

    private void reactOnStatus(MyoStatus status){
        mOverlayView.show3DToast("TODO: react on Status: " + status.getValue());
    }

    private InputAction getInputActionByPoseChange(Pose previousPose, Pose newPose){
        if (newPose == Pose.REST) {
            switch (previousPose) {
                case FIST:
                    return InputAction.DoSelect;
                case FINGERS_SPREAD:
                    return InputAction.DoStateBack;
                case WAVE_OUT:
                    return InputAction.DoUndo;
            }
        }
        return InputAction.DoNothing;
    }

    //Nicht verwenden, da es kein "onCardboardTriggerRelease" gibt
//    @Override
//    public void onCardboardTrigger() {
//        Log.i(TAG, "onCardboardTrigger");
//          mOverlayView.show3DToast("onCardboardTrigger");
//        // Always give user feedback.
//        mVibrator.vibrate(50);
//    }

}
