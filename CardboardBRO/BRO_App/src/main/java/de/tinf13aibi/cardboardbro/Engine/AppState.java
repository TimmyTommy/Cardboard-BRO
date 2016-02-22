package de.tinf13aibi.cardboardbro.Engine;

/**
 * Created by dthom on 05.01.2016.
 */
public enum AppState {
    Unknown("Unknown"),
    SelectAction("SelectAction"),
        SelectEntityToDelete("SelectEntityToDelete"),
        SelectEntityToMove("SelectEntityToMove"),
            MoveEntity("MoveEntity"),
        SelectEntityToCreate("SelectEntityToCreate"),
            WaitForBeginFreeLine("WaitForBeginFreeLine"),
                WaitForEndFreeLine("WaitForEndFreeLine"),
            WaitForBeginPolyLinePoint("WaitForBeginPolyLinePoint"),
                WaitForNextPolyLinePoint("WaitForNextPolyLinePoint"),
            WaitForSphereCenterPoint("WaitForSphereCenterPoint"),
                WaitForSphereRadiusPoint("WaitForSphereRadiusPoint"),
            WaitForCylinderCenterPoint("WaitForCylinderCenterPoint"),
                WaitForCylinderRadiusPoint("WaitForCylinderRadiusPoint"),
                    WaitForCylinderHeightPoint("WaitForCylinderHeightPoint"),
            WaitForCuboidBasePoint1("WaitForCuboidBasePoint1"),
                WaitForCuboidBasePoint2("WaitForCuboidBasePoint2"),
                    WaitForCuboidHeightPoint("WaitForCuboidHeightPoint"),
            WaitForKeyboardInput("WaitForKeyboardInput"),
                WaitForPlaceTextPoint("WaitForPlaceTextPoint");


    private String mStringValue;

    AppState(String value){
        mStringValue = value;
    }

    @Override
    public String toString() {
        return mStringValue;
    }

    public String getValue() {
        return mStringValue;
    }
}
