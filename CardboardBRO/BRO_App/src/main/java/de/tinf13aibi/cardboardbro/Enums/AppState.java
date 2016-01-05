package de.tinf13aibi.cardboardbro.Enums;

/**
 * Created by dthom on 05.01.2016.
 */
public enum AppState {
    Unknown,
    SelectAction,
        SelectEntityToDelete,
        SelectEntityToMove,
            MoveEntity,
        SelectEntityToCreate,
            WaitForBeginFreeDraw,
                DrawingFreeHand,
            WaitForPolyLinePoint,
            WaitForSphereCenterpoint,
                WaitForSphereRadiusPoint,
            WaitForCylinderCenterPoint,
                WaitForCylinderRadiusPoint,
                    WaitForCylinderHeightPoint,
            WaitForCuboidBasePoint1,
                WaitForCuboidBasePoint2,
                    WaitForCuboidHeightPoint,
            WaitForKeyboardInput,
                WaitForPlaceTextPoint
}
