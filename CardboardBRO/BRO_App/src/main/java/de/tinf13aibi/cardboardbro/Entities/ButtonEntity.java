package de.tinf13aibi.cardboardbro.Entities;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Enums.AppState;
import de.tinf13aibi.cardboardbro.Geometry.GeometryDatabase;
import de.tinf13aibi.cardboardbro.Geometry.Triangle;

/**
 * Created by dth on 01.12.2015.
 */
public class ButtonEntity extends BaseEntity implements ITriangulatedEntity {
    private AppState mNextState = AppState.Unknown;

    @Override
    public ArrayList<Triangle> getAbsoluteTriangles(){
        return super.getAbsoluteTriangles();
    }

    public ButtonEntity(int program){
        super(program);
        fillBuffers(GeometryDatabase.CUBE_COORDS, GeometryDatabase.CUBE_NORMALS, GeometryDatabase.CUBE_COLORS);
        calcAbsoluteTriangles();
    }

    public AppState getNextState() {
        return mNextState;
    }

    public void setNextState(AppState nextState) {
        mNextState = nextState;
    }
}