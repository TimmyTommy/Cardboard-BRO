package de.tinf13aibi.cardboardbro.Entities;

import android.opengl.GLES20;
import android.opengl.Matrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Engine.Constants;
import de.tinf13aibi.cardboardbro.Engine.DrawingContext;
import de.tinf13aibi.cardboardbro.Engine.IState;
import de.tinf13aibi.cardboardbro.Engine.StateSelectAction;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Geometry.GeometryDatabase;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dth on 01.12.2015.
 */
public class ButtonEntity extends BaseEntity implements ITriangulatedEntity {
    private Class<?> mNextStateClass;
    private FloatBuffer mTextureCoords;
    private int mMVPMatrixHandle;
    private int mMVMatrixHandle;
    private int mLightPosHandle;
    private int mTextureUniformHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private int mNormalHandle;
    private int mTextureCoordinateHandle;
    private int mTextureHandle = 0;
    private char mKey = ' ';

    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    @Override
    public void setPositionAndOrientation(Vec3d position, Vec3d baseNormal, boolean fix) {
        //TODO
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
        //TODO
        return null;
    }

    public ButtonEntity(int program){
        super(program);
//        fillBuffers(GeometryDatabase.CUBE_COORDS, GeometryDatabase.CUBE_NORMALS, GeometryDatabase.CUBE_COLORS);
        fillBuffers(GeometryDatabase.CUBE_COORDS, GeometryDatabase.CUBE_NORMALS, GeometryDatabase.BUTTON_COLORS);
        fillBufferTextures(GeometryDatabase.BUTTON_TEXTURE_COORDS);
        calcAbsoluteTriangles();
    }

    protected void fillBufferTextures(float[] textureCoords){
        ByteBuffer bbColors = ByteBuffer.allocateDirect(textureCoords.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        mTextureCoords = bbColors.asFloatBuffer();
        mTextureCoords.put(textureCoords);
        mTextureCoords.position(0);
    }

    public char getKey() {
        return mKey;
    }

    public ButtonEntity setKey(char key) {
        mKey = key;
        return this;
    }

    public IState getNextState(DrawingContext drawingContext) {
        try {
            return (IState)mNextStateClass.getConstructor(DrawingContext.class).newInstance(drawingContext);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new StateSelectAction(drawingContext);
    }

    public ButtonEntity setNextState(Class<?> nextStateClass) {
        mNextStateClass = nextStateClass;
        return this;
    }

    public ButtonEntity setTextureHandle(int textureHandle) {
        mTextureHandle = textureHandle;
        return this;
    }

    protected void fillButtonParameters(int program){
        mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(program, "u_LightPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(program, "u_Texture");

        mPositionHandle = GLES20.glGetAttribLocation(program, "a_Position");
//        mColorHandle = GLES20.glGetAttribLocation(program, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(program, "a_Normal");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(program, "a_TexCoordinate");
    }

    @Override
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        GLES20.glUseProgram(mProgram);
        fillButtonParameters(mProgram); //muss erstmal hier sein da draw() nur in onDrawEye() aufgerufen wird und somit GLES20-Context vorhanden ist

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        float[] modelView = new float[16];
        float[] modelViewProjection = new float[16];

        Matrix.multiplyMM(modelView, 0, view, 0, mModel, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(mLightPosHandle, 1, lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, modelViewProjection, 0);

        GLES20.glVertexAttribPointer(mPositionHandle, Constants.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mVertices);
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, mNormals);
//        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, mColors);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureCoords);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
//        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVerticesCount);
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("class", this.getClass().toString());
        json.put("mModel", super.getModelToJson());
        json.put("mBaseModel", super.getBaseModelToJson());

        return json;
    }

    @Override
    public void loadFromJsonObject(JSONObject jsonEntity) throws JSONException {
        setModelFromJson(jsonEntity.optJSONArray("mModel"));
        setBaseModelFromJson(jsonEntity.optJSONArray("mBaseModel"));
    }
}