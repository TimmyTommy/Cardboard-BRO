package de.tinf13aibi.cardboardbro;

/**
 * Created by dth on 27.11.2015.
 */
public interface Constants {
    float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 2.0f, 0.0f, 1.0f };
    int COORDS_PER_VERTEX = 3;
//    int CYLINDER_SEGMENTS = 72; //360 / 72 = 5° pro Segment
//    int CYLINDER_SEGMENTS = 36; //360 / 72 = 10° pro Segment
    int CYLINDER_SEGMENTS = 10; //aus performancegründen erstmal wenig segmente
    //TODO: für performance eine "hitbox" um RUNDE objekte machen
    //TODO: erstmal nur Collision mit Hinbox prüfen, wenn ja dann mit rundem Objekt
    float Z_NEAR = 0.1f;
    float Z_FAR = 1000.0f;
    float CANVAS_CYL_RADIUS = 50.0f;
    float CANVAS_CYL_HEIGHT = 65.0f;
    float CANVAS_CYL_DEPTH = -15.0f;

    float TIME_DELTA = 0.3f;
    float FLOOR_DEPTH = -10f;

}
