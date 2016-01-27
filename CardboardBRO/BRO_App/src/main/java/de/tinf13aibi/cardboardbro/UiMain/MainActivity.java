package de.tinf13aibi.cardboardbro.UiMain;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

import java.util.Date;

import de.tinf13aibi.cardboardbro.Engine.DrawingContext;
import de.tinf13aibi.cardboardbro.Engine.InputAction;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoData;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoDeviceListener;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoListenerTarget;
import de.tinf13aibi.cardboardbro.GestureUtils.MyoStatus;
import de.tinf13aibi.cardboardbro.R;
import de.tinf13aibi.cardboardbro.Shader.Programs;
import de.tinf13aibi.cardboardbro.Shader.ShaderCollection;
import de.tinf13aibi.cardboardbro.Shader.Shaders;
import de.tinf13aibi.cardboardbro.Shader.Textures;

public class MainActivity extends CardboardActivity implements MyoListenerTarget, SensorEventListener {
    private MyoData mMyoData = new MyoData();
    private DrawingContext mActiveDrawingContext;

    private Vibrator mVibrator;
    private CardboardOverlayView mOverlayView;
    private CardboardView mCardboardView;

    private Date mClickTime; //TODO: nur temporär zum imitieren von "MYO-Gesten"


    public CardboardView getCardboardView(){
        return mCardboardView;
    }

    public Vibrator getVibrator() {
        return mVibrator;
    }

    public CardboardOverlayView getOverlayView() {
        return mOverlayView;
    }

    public MyoData getMyoData() {
        return mMyoData;
    }

    public DrawingContext getActiveDrawingContext() {
        return mActiveDrawingContext;
    }

    private void initOnStepListener(){
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            mOverlayView.show3DToast("Count sensor not available!");
//            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        mActiveDrawingContext.getUser().step(); //TODO noch verbessern
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void initOnTouchListener(){
        mCardboardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //TODO Moving später wieder aktiviedern
//                        mActiveDrawingContext.processUserMoving(true);
                        mClickTime = new Date();
                        break;
                    case MotionEvent.ACTION_UP:
                        //TODO Moving später wieder aktiviedern
//                        mActiveDrawingContext.processUserMoving(false);

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_ui);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlayView.show3DToast("Push the button to move around.");

        mCardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        mCardboardView.setRestoreGLStateEnabled(false);

        mActiveDrawingContext = new DrawingContext(this);
        mActiveDrawingContext.setActiveDrawingContext();

        initOnTouchListener();
        initOnStepListener();
        initializeMyoHub();
        OnUpdateStatus(MyoDeviceListener.getInstance().getStatus());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyoDeviceListener.getInstance().removeTarget(this);
        //TODO: save mActiveDrawingContext
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyoDeviceListener.getInstance().addTarget(this);

        OnUpdateStatus(MyoDeviceListener.getInstance().getStatus());
        //TODO: load mActiveDrawingContext
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyoDeviceListener.getInstance().removeTarget(this);
        //TODO: save mActiveDrawingContext
    }

    @Override
    public void OnPoseChange(Pose previousPose, Pose newPose) {
        mMyoData.setPose(newPose);
        InputAction inputAction = getInputActionByPoseChange(previousPose, newPose);
        mActiveDrawingContext.processInputAction(inputAction);
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

    private void initShaders(){
        ShaderCollection.loadGLShader(Shaders.BodyVertexShader, GLES20.GL_VERTEX_SHADER, getResources().openRawResource(R.raw.vertex_body));
        ShaderCollection.loadGLShader(Shaders.BodyFragmentShader, GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.fragment_body));
        ShaderCollection.loadGLShader(Shaders.GridVertexShader, GLES20.GL_VERTEX_SHADER, getResources().openRawResource(R.raw.vertex_body));
        ShaderCollection.loadGLShader(Shaders.GridFragmentShader, GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.fragment_grid));
        ShaderCollection.loadGLShader(Shaders.LineVertexShader, GLES20.GL_VERTEX_SHADER, getResources().openRawResource(R.raw.vertex_line));
        ShaderCollection.loadGLShader(Shaders.LineFragmentShader, GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.fragment_line));
        ShaderCollection.loadGLShader(Shaders.BodyTexturedVertexShader, GLES20.GL_VERTEX_SHADER, getResources().openRawResource(R.raw.vertex_textured_body));
        ShaderCollection.loadGLShader(Shaders.BodyTexturedFragmentShader, GLES20.GL_FRAGMENT_SHADER, getResources().openRawResource(R.raw.fragment_textured_body));

    }

    private void initTextures(){
        ShaderCollection.loadTexture(this, Textures.TextureButtonCreateEntity,  R.drawable.button_create);
        ShaderCollection.loadTexture(this, Textures.TextureButtonMoveEntity,    R.drawable.button_move);
        ShaderCollection.loadTexture(this, Textures.TextureButtonDeleteEntity,  R.drawable.button_delete);

        ShaderCollection.loadTexture(this, Textures.TextureButtonBack,      R.drawable.button_back);
        ShaderCollection.loadTexture(this, Textures.TextureButtonFreeLine,  R.drawable.button_freeline);
        ShaderCollection.loadTexture(this, Textures.TextureButtonPolyLine,  R.drawable.button_polyline);
        ShaderCollection.loadTexture(this, Textures.TextureButtonCylinder,  R.drawable.button_cylinder);
        ShaderCollection.loadTexture(this, Textures.TextureButtonCuboid,    R.drawable.button_cuboid);
        ShaderCollection.loadTexture(this, Textures.TextureButtonSphere,    R.drawable.button_sphere);
        ShaderCollection.loadTexture(this, Textures.TextureButtonText,      R.drawable.button_text);

        ShaderCollection.loadTexture(this, Textures.TextureNone,                R.drawable.blank);
        ShaderCollection.loadTexture(this, Textures.TextureKey0,                R.drawable.n0);
        ShaderCollection.loadTexture(this, Textures.TextureKey1,                R.drawable.n1);
        ShaderCollection.loadTexture(this, Textures.TextureKey2,                R.drawable.n2);
        ShaderCollection.loadTexture(this, Textures.TextureKey3,                R.drawable.n3);
        ShaderCollection.loadTexture(this, Textures.TextureKey4,                R.drawable.n4);
        ShaderCollection.loadTexture(this, Textures.TextureKey5,                R.drawable.n5);
        ShaderCollection.loadTexture(this, Textures.TextureKey6,                R.drawable.n6);
        ShaderCollection.loadTexture(this, Textures.TextureKey7,                R.drawable.n7);
        ShaderCollection.loadTexture(this, Textures.TextureKey8,                R.drawable.n8);
        ShaderCollection.loadTexture(this, Textures.TextureKey9,                R.drawable.n9);
        ShaderCollection.loadTexture(this, Textures.TextureKeyBackSpc,          R.drawable.backspc);
        ShaderCollection.loadTexture(this, Textures.TextureKeyQ,                R.drawable.q);
        ShaderCollection.loadTexture(this, Textures.TextureKeyW,                R.drawable.w);
        ShaderCollection.loadTexture(this, Textures.TextureKeyE,                R.drawable.e);
        ShaderCollection.loadTexture(this, Textures.TextureKeyR,                R.drawable.r);
        ShaderCollection.loadTexture(this, Textures.TextureKeyT,                R.drawable.t);
        ShaderCollection.loadTexture(this, Textures.TextureKeyZ,                R.drawable.z);
        ShaderCollection.loadTexture(this, Textures.TextureKeyU,                R.drawable.u);
        ShaderCollection.loadTexture(this, Textures.TextureKeyI,                R.drawable.i);
        ShaderCollection.loadTexture(this, Textures.TextureKeyO,                R.drawable.o);
        ShaderCollection.loadTexture(this, Textures.TextureKeyP,                R.drawable.p);
        ShaderCollection.loadTexture(this, Textures.TextureKeyÜ,                R.drawable.ue);
        ShaderCollection.loadTexture(this, Textures.TextureKeyA,                R.drawable.a);
        ShaderCollection.loadTexture(this, Textures.TextureKeyS,                R.drawable.s);
        ShaderCollection.loadTexture(this, Textures.TextureKeyD,                R.drawable.d);
        ShaderCollection.loadTexture(this, Textures.TextureKeyF,                R.drawable.f);
        ShaderCollection.loadTexture(this, Textures.TextureKeyG,                R.drawable.g);
        ShaderCollection.loadTexture(this, Textures.TextureKeyH,                R.drawable.h);
        ShaderCollection.loadTexture(this, Textures.TextureKeyJ,                R.drawable.j);
        ShaderCollection.loadTexture(this, Textures.TextureKeyK,                R.drawable.k);
        ShaderCollection.loadTexture(this, Textures.TextureKeyL,                R.drawable.l);
        ShaderCollection.loadTexture(this, Textures.TextureKeyÖ,                R.drawable.oe);
        ShaderCollection.loadTexture(this, Textures.TextureKeyÄ,                R.drawable.ae);
        ShaderCollection.loadTexture(this, Textures.TextureKeyY,                R.drawable.y);
        ShaderCollection.loadTexture(this, Textures.TextureKeyX,                R.drawable.x);
        ShaderCollection.loadTexture(this, Textures.TextureKeyC,                R.drawable.c);
        ShaderCollection.loadTexture(this, Textures.TextureKeyV,                R.drawable.v);
        ShaderCollection.loadTexture(this, Textures.TextureKeyB,                R.drawable.b);
        ShaderCollection.loadTexture(this, Textures.TextureKeyN,                R.drawable.n);
        ShaderCollection.loadTexture(this, Textures.TextureKeyM,                R.drawable.m);
        ShaderCollection.loadTexture(this, Textures.TextureKeySmallerThan,      R.drawable.smaller);
        ShaderCollection.loadTexture(this, Textures.TextureKeyBiggerThan,       R.drawable.bigger);
        ShaderCollection.loadTexture(this, Textures.TextureKeyComma,            R.drawable.comma);
        ShaderCollection.loadTexture(this, Textures.TextureKeyDot,              R.drawable.dot);
        ShaderCollection.loadTexture(this, Textures.TextureKeyPlus,             R.drawable.plus);
        ShaderCollection.loadTexture(this, Textures.TextureKeyMinus,            R.drawable.minus);
        ShaderCollection.loadTexture(this, Textures.TextureKeyStar,             R.drawable.star);
        ShaderCollection.loadTexture(this, Textures.TextureKeySlash,            R.drawable.slash);
        ShaderCollection.loadTexture(this, Textures.TextureKeyQuestionMark,     R.drawable.question);
        ShaderCollection.loadTexture(this, Textures.TextureKeyExclamationMark,  R.drawable.exclam);
        ShaderCollection.loadTexture(this, Textures.TextureKeySpace,            R.drawable.space);
        ShaderCollection.loadTexture(this, Textures.TextureKeyEnter,            R.drawable.enter);
    }

    public void initPrograms(){
        initShaders();
        initTextures();
        ShaderCollection.addProgram(Programs.BodyProgram, Shaders.BodyVertexShader, Shaders.BodyFragmentShader);
        ShaderCollection.addProgram(Programs.GridProgram, Shaders.GridVertexShader, Shaders.GridFragmentShader);
        ShaderCollection.addProgram(Programs.LineProgram, Shaders.LineVertexShader, Shaders.LineFragmentShader);
        ShaderCollection.addProgram(Programs.BodyTexturedProgram, Shaders.BodyTexturedVertexShader, Shaders.BodyTexturedFragmentShader);
    }

}
