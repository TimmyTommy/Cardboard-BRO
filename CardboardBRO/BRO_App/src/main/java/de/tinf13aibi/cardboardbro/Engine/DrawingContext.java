package de.tinf13aibi.cardboardbro.Engine;

import org.json.JSONException;
import org.json.JSONObject;

import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Shader.Programs;
import de.tinf13aibi.cardboardbro.Shader.ShaderCollection;
import de.tinf13aibi.cardboardbro.UiMain.MainActivity;

/**
 * Created by dthom on 21.01.2016.
 */
public class DrawingContext implements IState{
    private MainActivity mMainActivity;
    private User mUser = new User();
    private Drawing mDrawing = new Drawing();
    private IEntity mEditingEntity;
    private IState mState;
    private DrawingRenderer mDrawingRenderer;

    public DrawingContext(MainActivity mainActivity){
        mMainActivity = mainActivity;
        mState = new StateSelectAction(this);
        mDrawingRenderer = new DrawingRenderer(this);
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("mDrawing", mDrawing.toJsonObject());
        return json;
    }

    public void loadFromJson(JSONObject jsonDrawingContext) throws JSONException {
        JSONObject jsonDrawing = jsonDrawingContext.optJSONObject("mDrawing");
        if (jsonDrawing != null) {
            mDrawing.loadFromJson(jsonDrawing);
        }
    }

    @Override
    public void processOnNewFrame(float[] headView, Vec3d armForwardVec) {
        if (isActiveDrawingContext()) mState.processOnNewFrame(headView, armForwardVec);
    }

    @Override
    public void processOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        if (isActiveDrawingContext()) mState.processOnDrawEye(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        if (isActiveDrawingContext()) mState.processInputAction(inputAction);
    }

    @Override
    public void processUserMoving(Vec3d acceleration, InputAction inputAction) {
        if (isActiveDrawingContext()) mState.processUserMoving(acceleration, inputAction);
    }

    public IEntity getEditingEntity() {
        return mEditingEntity;
    }

    public void setEditingEntity(IEntity editingEntity) {
        mEditingEntity = editingEntity;
    }

    public void setActiveDrawingContext(){
        mMainActivity.getCardboardView().setRenderer(mDrawingRenderer);
        mMainActivity.getCardboardView().setTransitionViewEnabled(true);
        mMainActivity.setCardboardView(mMainActivity.getCardboardView());
    }

    public boolean isActiveDrawingContext(){
        return this.equals(mMainActivity.getActiveDrawingContext());
    }

    public void initUser(){
        mUser.createCrosshairs(ShaderCollection.getProgram(Programs.LineProgram));
    }

    public void initDrawing(){
        mDrawing.init();
    }

    public IState getState() {
        return mState;
    }

    public void setState(IState state) {
        mState = state;
    }

    public MainActivity getMainActivity() {
        return mMainActivity;
    }

    public User getUser() {
        return mUser;
    }

    public Drawing getDrawing() {
        return mDrawing;
    }
}
