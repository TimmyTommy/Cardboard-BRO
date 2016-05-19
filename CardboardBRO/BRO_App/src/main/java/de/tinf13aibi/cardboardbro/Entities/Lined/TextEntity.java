package de.tinf13aibi.cardboardbro.Entities.Lined;

import android.opengl.GLES20;
import android.opengl.Matrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Engine.Constants;
import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.ButtonSet;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IManySidedEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Entities.Triangulated.CuboidEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;
import de.tinf13aibi.cardboardbro.Shader.Programs;
import de.tinf13aibi.cardboardbro.Shader.ShaderCollection;
import de.tinf13aibi.cardboardbro.Shader.Textures;

/**
 * Created by dthom on 15.04.2016.
 */
public class TextEntity extends BaseEntity implements IManySidedEntity {
    private String mText = "";
    private Vec3d mPosition = new Vec3d();
    private float mSize = 0.1f;
    private float[] mFacing = new float[16];
    private CuboidEntity mHitBox;
    //private ArrayList<TextCharEntity> mTextCharArray = new ArrayList<>();

    private ButtonSet mCharSet = new ButtonSet();

//    public void setColor(float red, float green, float blue, float alpha) {
//        for (TextCharEntity entity : mTextCharArray) {
//            entity.setColor(red, green, blue, alpha);
//        }
//    }

    @Override
    public CuboidEntity getHitBox() {
        return mHitBox;
    }

    @Override
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
//        for (TextCharEntity entity : mTextCharArray) {
//            entity.draw(view, perspective, lightPosInEyeSpace);
//        }
        mCharSet.draw(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("class", this.getClass().toString());
        json.put("mModel", super.getModelToJson());
        json.put("mBaseModel", super.getBaseModelToJson());

        json.put("mText", mText);
        json.put("mPosition", mPosition.toJsonArray());
        json.put("mSize", mSize);
        json.put("mFacing", new JSONArray(mFacing));

        return json;
    }

    @Override
    public void loadFromJsonObject(JSONObject jsonEntity) throws JSONException {
        setModelFromJson(jsonEntity.optJSONArray("mModel"));
        setBaseModelFromJson(jsonEntity.optJSONArray("mBaseModel"));

        mText = jsonEntity.optString("mText");
        mPosition = new Vec3d(jsonEntity.optJSONArray("mPosition"));
        mSize = (float)jsonEntity.optDouble("mSize");

        JSONArray jsonFacing = jsonEntity.optJSONArray("mFacing");
        for (int i = 0; i < jsonFacing.length(); i++) {
            mFacing[i] = (float)jsonFacing.optDouble(i);
        }

        updateSize(mSize);
    }

    public TextEntity(){
        super();
        mHitBox = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
    }

    public TextEntity(String text, float[] facing, Vec3d pos){
        super();
        mText = text;
        setFacing(facing);
        mPosition = pos;
        mHitBox = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));
        transformTextToTextCharEntities(text);
    }

    private void calcHitbox(){
        Vec3d min = new Vec3d(100, 100, 100);
        Vec3d max = new Vec3d(-100, -100, -100);
        for (Triangle triangle : mTriangles) {
            Vec3d tmin = triangle.getMinPoint();
            Vec3d tmax = triangle.getMaxPoint();

            min.x = min.x<tmin.x ? min.x : tmin.x;
            min.y = min.y<tmin.y ? min.y : tmin.y;
            min.z = min.z<tmin.z ? min.z : tmin.z;

            max.x = max.x>tmax.x ? max.x : tmax.x;
            max.y = max.y>tmax.y ? max.y : tmax.y;
            max.z = max.z>tmax.z ? max.z : tmax.z;
        }

        Vec3d delta = VecMath.calcVecMinusVec(max, min);
        mHitBox.setAttributes(min, new Vec3d(0, 1, 0), delta.z, delta.x, delta.y, new float[]{0.5f, 0.5f, 1, 0.5f});
    }

    private void transformTextToTextCharEntities(String text){
//        mTextCharArray.clear();
//        for (int i=0; i<text.length(); i++) {
//            char c = text.charAt(i);
//            TextCharEntity entity;
//            entity = new TextCharEntity(mProgram, c);
//            mTextCharArray.add(entity);
//        }

        mCharSet = new ButtonSet();
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            Textures tex = Textures.parseValue(c);

            ButtonEntity entity = new ButtonEntity(ShaderCollection.getProgram(Programs.BodyTexturedProgram))
                    .setTextureHandle(ShaderCollection.getTexture(tex));

            float x = -2f*mSize*text.length()/2 + 2f* mSize * i;

            Matrix.translateM(entity.getBaseModel(), 0, x, 0, 0);
            Matrix.scaleM(entity.getBaseModel(), 0, mSize, mSize, mSize);

            if (tex != Textures.TextureNone) {
                mCharSet.addButton(entity);
            }
        }

        updatePosition(mFacing, mPosition);
    }

    public void updatePosition(float[] facing, Vec3d position){
        mPosition = position;
        setFacing(facing);
        mCharSet.setButtonsRelativeToCamera(facing, position);
        calcAbsoluteTriangles();
        calcHitbox();
    }

    public void updateSize(float size){
        float limitedSize = size > 5 ? 5 : size;
        mSize = limitedSize;
        transformTextToTextCharEntities(mText);
    }

    public Vec3d getPosition() {
        return mPosition;
    }

    public void setPosition(Vec3d position) {
        mPosition = position;
        updatePosition(mFacing, mPosition);
    }

    public float[] getFacing() {
        return mFacing;
    }

    public void setFacing(float[] facing) {
        System.arraycopy(facing, 0, mFacing, 0, facing.length);
        //updatePosition(mFacing, mPosition);
    }

    public float getSize() {
        return mSize;
    }

    public String getText() {
        return mText;
    }

    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    @Override
    public void setPositionAndOrientation(Vec3d position, Vec3d baseNormal, boolean fix) {
        //TODO
        updatePosition(mFacing, position);
        //mBaseNormal = baseNormal;
        //recreateGeometry(fix);
    }

    @Override
    public Vec3d getBaseVert() {
        return new Vec3d();
    }

    @Override
    public Vec3d getBaseNormal() {
        return new Vec3d();
    }

    @Override
    public ITriangulatedEntity clone() {
        TextEntity ent = new TextEntity(mText, mFacing, mPosition);
        return ent;
    }

    @Override
    protected void calcAbsoluteTriangles(){
        mTriangles.clear();
        ArrayList<IEntity> entities = mCharSet.getButtonEntities();
        for (IEntity entity : entities) {
            mTriangles.addAll( ((ITriangulatedEntity)entity).getAbsoluteTriangles() );
        }
    }
}
