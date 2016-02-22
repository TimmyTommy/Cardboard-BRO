package de.tinf13aibi.cardboardbro.Engine;

/**
 * Created by dthom on 08.01.2016.
 */
public enum AppStateGroup {
    G_Unknown,
    G_SelectButton,
    G_DrawFreeLine,
    G_DrawPolyLine,
    G_DrawCylinder,
    G_DrawCuboid,
    G_DrawSphere,
    G_WriteText,
    G_DeleteEntity,
    G_MoveEntity;


    public static AppStateGroup getFromAppState(AppState appState) {
        switch (appState){
            case SelectAction:
            case SelectEntityToCreate:
                return G_SelectButton;

            case WaitForBeginFreeLine:
            case WaitForEndFreeLine:
                return G_DrawFreeLine;

            case WaitForBeginPolyLinePoint:
            case WaitForNextPolyLinePoint:
                return G_DrawPolyLine;

            case WaitForCylinderCenterPoint:
            case WaitForCylinderRadiusPoint:
            case WaitForCylinderHeightPoint:
                return G_DrawCylinder;

            case WaitForCuboidBasePoint1:
            case WaitForCuboidBasePoint2:
            case WaitForCuboidHeightPoint:
                return G_DrawCuboid;

            case WaitForSphereCenterPoint:
            case WaitForSphereRadiusPoint:
                return G_DrawSphere;

            case WaitForKeyboardInput:
            case WaitForPlaceTextPoint:
                return G_WriteText;

            case SelectEntityToMove:
            case MoveEntity:
                return G_MoveEntity;

            case SelectEntityToDelete:
                return G_DeleteEntity;

            default:
                return G_Unknown;

        }
    }
}
