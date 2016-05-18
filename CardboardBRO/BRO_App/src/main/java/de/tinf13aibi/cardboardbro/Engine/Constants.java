package de.tinf13aibi.cardboardbro.Engine;

/**
 * Created by dth on 27.11.2015.
 */
public interface Constants {
//    float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 0.5f, 0.0f, 1.0f }; //TODO zum test
    float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 15f, 0.0f, 1.0f }; //TODO die richtige
    int COORDS_PER_VERTEX = 3;
//    int CYCLE_SEGMENTS = 72; //360 / 72 = 5° pro Segment
//    int CYCLE_SEGMENTS = 36; //360 / 36 = 10° pro Segment
    int CYCLE_SEGMENTS = 18; //360 / 18 = 20° pro Segment
//    int CYCLE_SEGMENTS = 10; //aus performancegründen erstmal wenig segmente
    int HALFCYCLE_SEGMENTS = CYCLE_SEGMENTS/2;

    float Z_NEAR = 0.1f;
    float Z_FAR = 500.0f;
    float CANVAS_CYL_RADIUS = 10.0f;
    float CANVAS_CYL_HEIGHT = 15.0f;
    float CANVAS_CYL_DEPTH = -0.1f;

    float TIME_DELTA = 0.3f;
    float FLOOR_DEPTH = 0f;

    boolean DEBUG_FLAG = false;
}
