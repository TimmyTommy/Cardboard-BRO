package de.tinf13aibi.cardboardbro.Entities;

import android.opengl.GLES20;
import android.opengl.Matrix;

import org.json.JSONArray;
import org.json.JSONException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Engine.Constants;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Triangle;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dth on 27.11.2015.
 */

//TODO weitere Entities: (evtl. Kegel, Kegelstumpf, Pyramide, Pyramidenstumpf, Torus, Spheroid, Ellipsoid)
//Siehe hierfür: http://exchange.autodesk.com/autocadmechanical/deu/online-help/AMECH_PP/2012/DEU/attachments/ill_primitives.png
public abstract class BaseEntity implements IEntity {
    protected EntityDisplayType displayType = EntityDisplayType.Absolute;
    protected float[] mCoords = new float[]{};
    protected int mVerticesCount = 0;
    protected FloatBuffer mVertices;
    protected FloatBuffer mColors;
    protected FloatBuffer mNormals;
//    protected FloatBuffer mFoundColors;

    protected ArrayList<Triangle> mTriangles = new ArrayList<>();

    protected int mProgram;

    protected int mPositionParam;
    protected int mNormalParam;
    protected int mColorParam;
    protected int mModelParam;
    protected int mModelViewParam;
    protected int mModelViewProjectionParam;
    protected int mLightPosParam;

    protected float[] mModel;
    protected float[] mBaseModel;

    protected JSONArray getModelToJson() throws JSONException {
        return new JSONArray(mModel);
    }

    protected JSONArray getBaseModelToJson() throws JSONException {
        return new JSONArray(mBaseModel);
    }

    protected JSONArray getCoordsToJson() throws JSONException {
        return new JSONArray(mCoords);
    }

    protected void setModelFromJson(JSONArray json) throws JSONException {
        mModel = new float[json.length()];
        for (int i = 0; i < json.length(); i++) {
            mModel[i] = (float)json.optDouble(i);
        }
    }

    protected void setBaseModelFromJson(JSONArray json) throws JSONException {
        mBaseModel = new float[json.length()];
        for (int i = 0; i < json.length(); i++) {
            mBaseModel[i] = (float)json.optDouble(i);
        }
    }

    protected void setCoordsFromJson(JSONArray json) throws JSONException {
        mCoords = new float[json.length()];
        for (int i = 0; i < json.length(); i++) {
            mCoords[i] = (float)json.optDouble(i);
        }
    }

    public int getProgram() {
        return mProgram;
    }

    public ArrayList<Vec3d> getAbsoluteCoords(){
        int length = mCoords.length/3;
        if (this instanceof ButtonEntity){
            length = 6; //Nur FrontFace => 6 Punkte
        }
        ArrayList<Vec3d> absoluteCoords = new ArrayList<>();
        for (int i = 0; i<length; i++){
            float[] coord = new float[4];
            System.arraycopy(mCoords, i*3, coord, 0, 3);
            coord[3] = 1;
            Matrix.multiplyMV(coord, 0, mModel, 0, coord, 0);
            absoluteCoords.add(new Vec3d(coord));
        }
        return absoluteCoords;
    }

    protected void calcAbsoluteTriangles(){
        ArrayList<Vec3d> absoluteCoords = getAbsoluteCoords();
        mTriangles = new ArrayList<>();
        if (absoluteCoords.size()%3==0) {
            for (int i = 0; i<absoluteCoords.size()/3; i++) {
                mTriangles.add(new Triangle(absoluteCoords.get(i*3), absoluteCoords.get(i*3+1), absoluteCoords.get(i*3+2)));
            }
        }
    }

    protected ArrayList<Triangle> getAbsoluteTriangles(){
        return mTriangles;
    }

    public BaseEntity(){
        mModel = new float[16];
        mBaseModel = new float[16];
        Matrix.setIdentityM(mModel, 0);
        Matrix.setIdentityM(mBaseModel, 0);
    }

    public BaseEntity(int program){
        this();
        mProgram = program;
    }

    protected void fillParameters(int program){
        //TODO auslagern nach ShaderCollection Programs in eine ProgramClass mit den folgenden Attibuten + int mProgram
        mPositionParam = GLES20.glGetAttribLocation(program, "a_Position");
        mNormalParam = GLES20.glGetAttribLocation(program, "a_Normal");
        mColorParam = GLES20.glGetAttribLocation(program, "a_Color");

        mModelParam = GLES20.glGetUniformLocation(program, "u_Model");
        mModelViewParam = GLES20.glGetUniformLocation(program, "u_MVMatrix");
        mModelViewProjectionParam = GLES20.glGetUniformLocation(program, "u_MVP");
        mLightPosParam = GLES20.glGetUniformLocation(program, "u_LightPos");

        GLES20.glEnableVertexAttribArray(mPositionParam);
        GLES20.glEnableVertexAttribArray(mNormalParam);
        GLES20.glEnableVertexAttribArray(mColorParam);
    }

    protected void fillBufferVertices(float[] coords){
        mCoords = coords;
        mVerticesCount = coords.length / Constants.COORDS_PER_VERTEX;
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(mVerticesCount * Constants.COORDS_PER_VERTEX * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        mVertices = bbVertices.asFloatBuffer();
        mVertices.put(coords);
        mVertices.position(0);
    }

    protected void fillBufferNormals(float[] normals){
        ByteBuffer bbNormals = ByteBuffer.allocateDirect(normals.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        mNormals = bbNormals.asFloatBuffer();
        mNormals.put(normals);
        mNormals.position(0);
    }

    protected void fillBufferColors(float[] colors){
        ByteBuffer bbColors = ByteBuffer.allocateDirect(colors.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        mColors = bbColors.asFloatBuffer();
        mColors.put(colors);
        mColors.position(0);
    }

    protected void fillBuffers(float[] coords, float[] normals, float[] colors){
        fillBufferVertices(coords);
        fillBufferNormals(normals);
        fillBufferColors(colors);
    }

    public float[] getModel(){
        return mModel;
    }

    public void changedModel(){
        calcAbsoluteTriangles();
    }

    public float[] getBaseModel() {
        return mBaseModel;
    }

    public void resetModelToBase(){
        System.arraycopy(mBaseModel, 0, mModel, 0, 16);
    }

    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        GLES20.glUseProgram(mProgram);
        fillParameters(mProgram); //muss erstmal hier sein da draw() nur in onDrawEye() aufgerufen wird und somit GLES20-Context vorhanden ist

        float[] modelView = new float[16];
        float[] modelViewProjection = new float[16];

        Matrix.multiplyMM(modelView, 0, view, 0, mModel, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);

//        GLES20.glUseProgram(mProgram);
        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(mLightPosParam, 1, lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(mModelParam, 1, false, mModel, 0);
        GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(mPositionParam, Constants.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mVertices);
        GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT, false, 0, mNormals);
        GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false, 0, mColors);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVerticesCount);
    }

    public EntityDisplayType getDisplayType() {
        return displayType;
    }

    public BaseEntity setDisplayType(EntityDisplayType displayType) {
        this.displayType = displayType;
        return this;
    }
}
