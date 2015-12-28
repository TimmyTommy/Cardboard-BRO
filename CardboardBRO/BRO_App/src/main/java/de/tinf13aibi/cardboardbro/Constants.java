package de.tinf13aibi.cardboardbro;

/**
 * Created by dth on 27.11.2015.
 */
public interface Constants {
    float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 2.0f, 0.0f, 1.0f };
    int COORDS_PER_VERTEX = 3;
    int CYLINDER_SEGMENTS = 72; //360 / 72 = 5° pro Segment
    float Z_NEAR = 0.1f;
    float Z_FAR = 1000.0f;
    float CYL_RADIUS = 50.0f;
    float CAMERA_Z = 0.001f;//0.01f;
    float TIME_DELTA = 0.3f;

    float YAW_LIMIT = 0.12f;
    float PITCH_LIMIT = 0.12f;

}
