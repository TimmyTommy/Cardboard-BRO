package de.tinf13aibi.cardboardbro;

import android.os.Vibrator;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.ButtonEntity;
import de.tinf13aibi.cardboardbro.Entities.CuboidEntity;
import de.tinf13aibi.cardboardbro.Entities.CylinderEntity;
import de.tinf13aibi.cardboardbro.Entities.IEntity;
import de.tinf13aibi.cardboardbro.Entities.PolyLineEntity;
import de.tinf13aibi.cardboardbro.Enums.AppState;
import de.tinf13aibi.cardboardbro.Enums.AppStateGroup;
import de.tinf13aibi.cardboardbro.Enums.InputAction;
import de.tinf13aibi.cardboardbro.Enums.Programs;
import de.tinf13aibi.cardboardbro.Geometry.CollisionPlanePoint;
import de.tinf13aibi.cardboardbro.Geometry.CollisionTrianglePoint;
import de.tinf13aibi.cardboardbro.Geometry.Plane;
import de.tinf13aibi.cardboardbro.Geometry.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 09.01.2016.
 */
public class StateMachine {
//    private AppState mAppState = AppState.SelectAction;  //Bei Appstart default: SelectAction
//    private AppState mAppState = AppState.WaitForBeginFreeLine; //Zu Testzwecken manuell AppState setzen
//    private AppState mAppState = AppState.WaitForBeginPolyLinePoint; //Zu Testzwecken manuell AppState setzen
//    private AppState mAppState = AppState.WaitForCylinderCenterPoint; //Zu Testzwecken manuell AppState setzen
    private AppState mAppState = AppState.WaitForCuboidBasePoint1; //Zu Testzwecken manuell AppState setzen

    private Vibrator mVibrator;
    private CardboardOverlayView mOverlayView;
    private User mUser;
    private Drawing mDrawing;
    private ArrayList<IEntity> mEntityList;
    private IEntity mEditingEntity;

    public StateMachine(Vibrator vibrator, CardboardOverlayView overlayView){
        mUser = new User();
        mUser.createCrosshairs(ShaderCollection.getProgram(Programs.LineProgram));

        mDrawing = new Drawing().init();
        mEntityList = mDrawing.getEntityList();
        mVibrator = vibrator;
        mOverlayView = overlayView;
    }

    public void setUserMoving(Boolean moving){
        mUser.setMoving(moving);
    }

    public void processAppStateOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace){
        //TODO: Zeichenreihenfolge prüfen und evtl sortieren //evtl Linien zu erst oder linien als letztes
        mDrawing.drawEntityList(view, perspective, lightPosInEyeSpace);
        mUser.drawCrosshairs(view, perspective, lightPosInEyeSpace);
        switch (mAppState) {
            case SelectAction:          mDrawing.getEntityActionButtons().draw(view, perspective, lightPosInEyeSpace); break;
            case SelectEntityToCreate:  mDrawing.getEntityCreateButtons().draw(view, perspective, lightPosInEyeSpace); break;
            case WaitForKeyboardInput:  mDrawing.getKeyboardButtons().draw(view, perspective, lightPosInEyeSpace);
                break;
        }
        mDrawing.drawTempWorkingPlane(view, perspective, lightPosInEyeSpace);
    }

    public void processInputActionAndAppState(InputAction inputAction){
        AppStateGroup appStateGroup = AppStateGroup.getFromAppState(mAppState);
        switch (appStateGroup) {
            case G_SelectButton: processInputAndAppStateSelectButton(inputAction, mAppState); break;
            case G_DrawFreeLine: processInputAndAppStateFreeLine(inputAction, mAppState); break;
            case G_DrawPolyLine: processInputAndAppStatePolyLine(inputAction, mAppState); break;
            case G_DrawCylinder: processInputAndAppStateCylinder(inputAction, mAppState); break;
            case G_DrawCuboid:   processInputAndAppStateCuboid(inputAction, mAppState); break;
            case G_DrawSphere: break;
            case G_WriteText: break;
            case G_MoveEntity: break;
            case G_DeleteEntity: break;
            case G_Unknown: changeState(AppState.SelectAction, "Leave Unknown State"); break;
        }
    }

    public void processAppStateOnNewFrame(){
        mUser.move();

        mUser.calcEyeLookingAt(mEntityList);

        ArrayList<IEntity> entityListForArm;
        switch (mAppState){
            case SelectAction:          entityListForArm = mDrawing.getEntityActionButtons().getButtonEntities(); break;
            case SelectEntityToCreate:  entityListForArm = mDrawing.getEntityCreateButtons().getButtonEntities(); break;
            case WaitForKeyboardInput:  entityListForArm = mDrawing.getKeyboardButtons().getButtonEntities(); break;
            default:                    entityListForArm = mEntityList; break;
        }

//        CylinderEntity cylEnt = (CylinderEntity) mEntityList.get(mEntityList.size() - 1);
//        Vec3d cylCenter = cylEnt.getCenter();
//            Vec3d dir = VecMath.calcCrossProduct(new Vec3d(0, 1, 0), mUser.getArmForward());
//            Vec3d pnt = VecMath.calcVecPlusVec(new Vec3d(cylCenter.x, 0, cylCenter.z), dir);
//            Vec3d baseNormal = cylEnt.getBaseNormal().copy();
//            Vec3d firstCycleDir = new Vec3d();
//            Vec3d secondCycleDir = new Vec3d();
//            VecMath.calcCrossedVectorsFromNormal(secondCycleDir, firstCycleDir, baseNormal);
//            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cylCenter, firstCycleDir);
//            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cylCenter, mUser.getArmForward());

        //Get Point in Space on TempWorkingPlane
        switch (mAppState){
            case WaitForCylinderRadiusPoint:
            case WaitForCylinderHeightPoint:
            case WaitForCuboidBasePoint2:
            case WaitForCuboidHeightPoint:
                mUser.calcArmPointingAt(mDrawing.getTempWorkingPlane()); break;
            default:
                mUser.calcArmPointingAt(entityListForArm); break;
        }

        //Dynamic Drawing
        switch (mAppState){
            case WaitForEndFreeLine:         drawFreeLinePoint(mUser.getArmCrosshair().getPosition());              break;
            case WaitForNextPolyLinePoint:   drawPolyLineNextPoint(mUser.getArmCrosshair().getPosition(), false);   break;
            case WaitForCylinderRadiusPoint: drawCylinderRadius(mUser.getArmCrosshair().getPosition(), false);      break;
            case WaitForCylinderHeightPoint: drawCylinderEndHeight(mUser.getArmCrosshair().getPosition(), false);   break;
            case WaitForCuboidBasePoint2:    drawCuboidBeginBasePoint2(mUser.getArmPointingAt(), false);            break;
            case WaitForCuboidHeightPoint:   drawCuboidEndHeight(mUser.getArmCrosshair().getPosition(), false);     break;
        }

        mDrawing.getEntityActionButtons().rotateStep();
        mDrawing.getEntityCreateButtons().rotateStep();

        mDrawing.getEntityActionButtons().setButtonsRelativeToCamera(mUser.getInvHeadView(), mUser.getPosition());
        mDrawing.getEntityCreateButtons().setButtonsRelativeToCamera(mUser.getInvHeadView(), mUser.getPosition());
    }

    private void processInputAndAppStateSelectButton(InputAction inputAction, AppState appState){
        switch (appState){
            case SelectEntityToCreate:
            case SelectAction:
                switch (inputAction){
                    case DoStateBack: changeState(AppState.SelectAction, "Go Back"); break;
                    case DoSelect:  selectButton(mUser.getArmPointingAt()); break;
                }
                break;
        }
    }

    private void processInputAndAppStateFreeLine(InputAction inputAction, AppState appState){
        switch (appState){
            case WaitForBeginFreeLine:
                switch (inputAction){
                    case DoSelect: drawFreeLineBegin(mUser.getArmCrosshair().getPosition()); break;
                    case DoStateBack: drawFreeLineAbort(true); break;
                }
                break;
            case WaitForEndFreeLine:
                switch (inputAction){
                    case DoSelect: drawFreeLineEnd(mUser.getArmCrosshair().getPosition()); break;
                    case DoStateBack: drawFreeLineAbort(false); break;
                }
                break;
        }
    }

    private void processInputAndAppStatePolyLine(InputAction inputAction, AppState appState){
        switch (appState){
            case WaitForBeginPolyLinePoint:
                switch (inputAction){
                    case DoSelect: drawPolyLineBegin(mUser.getArmCrosshair().getPosition()); break;
                    case DoStateBack: drawPolyLineLeave(); break;
                }
                break;
            case WaitForNextPolyLinePoint:
                switch (inputAction){
                    case DoSelect: drawPolyLineNextPoint(mUser.getArmCrosshair().getPosition(), true); break;
                    case DoStateBack: drawPolyLineEnd(); break;
                }
                break;
        }
    }

    private void processInputAndAppStateCylinder(InputAction inputAction, AppState appState){
        switch (appState){
            case WaitForCylinderCenterPoint:
                switch (inputAction){
                    case DoSelect: drawCylinderBeginCenter(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal()); break;
                    case DoStateBack: drawCylinderAbort(true); break;
                }
                break;
            case WaitForCylinderRadiusPoint:
                switch (inputAction){
                    case DoSelect: drawCylinderRadius(mUser.getArmCrosshair().getPosition(), true); break;
                    case DoStateBack: drawCylinderAbort(false); break;
                }
                break;
            case WaitForCylinderHeightPoint:
                switch (inputAction){
                    case DoSelect: drawCylinderEndHeight(mUser.getArmCrosshair().getPosition(), true); break;
                    case DoStateBack: drawCylinderAbort(false); break;
                }
                break;
        }
    }

    private void processInputAndAppStateCuboid(InputAction inputAction, AppState appState){
        switch (appState){
            case WaitForCuboidBasePoint1:
                switch (inputAction){
                    case DoSelect: drawCuboidBeginBasePoint1(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal()); break;
                    case DoStateBack: drawCuboidAbort(true); break;
                }
                break;
            case WaitForCuboidBasePoint2:
                switch (inputAction){
//                    case FIST: drawCuboidBeginBasePoint2(mUser.getArmCrosshair().getPosition(), true); break;
                    case DoSelect: drawCuboidBeginBasePoint2(mUser.getArmPointingAt(), true); break;
                    case DoStateBack: drawCuboidAbort(false); break;
                }
                break;
            case WaitForCuboidHeightPoint:
                switch (inputAction){
                    case DoSelect: drawCuboidEndHeight(mUser.getArmCrosshair().getPosition(), true); break;
                    case DoStateBack: drawCuboidAbort(false); break;
                }
                break;
        }
    }

    //Change States
    private void selectButton(CollisionTrianglePoint collisionPoint){
        if (collisionPoint!=null){
            if (collisionPoint.entity != null){
                if (collisionPoint.entity instanceof ButtonEntity) {
                    ButtonEntity buttonEntity = (ButtonEntity)collisionPoint.entity;
                    changeState(buttonEntity.getNextState(), "Change state");
                }
            }
        }
    }

    private void changeState(AppState appState, String message){
        mVibrator.vibrate(50);
        mOverlayView.show3DToast(message + " -> " + appState.toString());
        mAppState = appState;
    }

    //Draw FreeLine
    private void drawFreeLineBegin(Vec3d point){
        mEditingEntity = new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram));
        ((PolyLineEntity)mEditingEntity).addVert(point);
        mEntityList.add(mEditingEntity);
        changeState(AppState.WaitForEndFreeLine, "Begin FreeDraw");
    }

    private void drawFreeLinePoint(Vec3d point){
        if (mEditingEntity instanceof PolyLineEntity) {
            ((PolyLineEntity)mEditingEntity).addVert(point);
        }
    }

    private void drawFreeLineEnd(Vec3d point){
        drawFreeLinePoint(point);
        changeState(AppState.WaitForBeginFreeLine, "End FreeDraw");
    }

    private void drawFreeLineAbort(Boolean leave){
        if (leave){
            changeState(AppState.SelectEntityToCreate, "Leave FreeLine Mode");
        } else {
            if (mEditingEntity instanceof PolyLineEntity) {
                mEntityList.remove(mEditingEntity);
            }
            changeState(AppState.WaitForBeginFreeLine, "Delete Drawed FreeLine");
        }
        mEditingEntity = null;
    }

    //Draw PolyLine
    private void drawPolyLineBegin(Vec3d point){
        mEditingEntity = new PolyLineEntity(ShaderCollection.getProgram(Programs.LineProgram));
        ((PolyLineEntity)mEditingEntity).addVert(point);
        ((PolyLineEntity)mEditingEntity).addVert(point);
        mEntityList.add(mEditingEntity);
        changeState(AppState.WaitForNextPolyLinePoint, "Begin PolyLine");
    }

    private void drawPolyLineNextPoint(Vec3d point, Boolean fix){
        if (mEditingEntity instanceof PolyLineEntity) {
            PolyLineEntity polyLineEntity = (PolyLineEntity) mEditingEntity;
            polyLineEntity.changeLastVert(point);
            if (fix) {
                polyLineEntity.addVert(point);
                changeState(AppState.WaitForNextPolyLinePoint, "Update PolyLine");
            }
        }
    }

    private void drawPolyLineEnd(){
        if (mEditingEntity instanceof PolyLineEntity) {
            ((PolyLineEntity)mEditingEntity).delLastVert();
        }
        changeState(AppState.WaitForBeginPolyLinePoint, "End PolyLine");
    }

    private void drawPolyLineLeave(){
        mEditingEntity = null;
        changeState(AppState.SelectEntityToCreate, "Leave PolyLine Mode");
    }

    //Draw Cylinder
    private void drawCylinderBeginCenter(Vec3d point, Vec3d baseNormal){
        mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(point, baseNormal));
        mEditingEntity = new CylinderEntity(ShaderCollection.getProgram(Programs.BodyProgram));

        float[] color = new float[]{0.7f, 0.3f, 0.5f, 1};
        ((CylinderEntity)mEditingEntity).setAttributes(point, baseNormal, 0.1f, 0.1f, color);
        mEntityList.add(mEditingEntity);

        changeState(AppState.WaitForCylinderRadiusPoint, "Begin Draw Cylinder");
    }

    private void drawCylinderRadius(Vec3d point, Boolean fix){
        if (mEditingEntity instanceof CylinderEntity) {
            CylinderEntity cylinderEntity = (CylinderEntity) mEditingEntity;
            cylinderEntity.setRadius(VecMath.calcVectorLength(VecMath.calcVecMinusVec(point, cylinderEntity.getCenter())));
            if (fix){

                //TODO das ganze auslagern und mTempWorkingPlane updaten, je nachdem in welche richtung user guckt
                Vec3d baseNormal = cylinderEntity.getBaseNormal().copy();
                Vec3d firstCycleDir = new Vec3d();
                Vec3d secondCycleDir = new Vec3d();
                VecMath.calcCrossedVectorsFromNormal(secondCycleDir, firstCycleDir, baseNormal);

//            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cylinderEntity.getCenter(), firstCycleDir);
                mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(cylinderEntity.getCenter(), secondCycleDir));

                changeState(AppState.WaitForCylinderHeightPoint, "Update Cylinder Radius");
            }
        }
    }

    private void drawCylinderEndHeight(Vec3d point, Boolean fix){
        if (mEditingEntity instanceof CylinderEntity) {
            CylinderEntity cylinderEntity = (CylinderEntity) mEditingEntity;
            Plane basePlane = VecMath.calcPlaneFromPointAndNormal(cylinderEntity.getCenter(), cylinderEntity.getBaseNormal());

            float distance = VecMath.calcDistancePlanePoint(basePlane, point);
            cylinderEntity.setHeight(distance);
        }
        if (fix){
            changeState(AppState.WaitForCylinderCenterPoint, "End Draw Cylinder");
            mDrawing.setTempWorkingPlane(null);
        }
    }

    private void drawCylinderAbort(Boolean leave){
        if (leave){
            changeState(AppState.SelectEntityToCreate, "Leave Cylinder Mode");
        } else {
            if (mEditingEntity instanceof CylinderEntity) {
                mEntityList.remove(mEditingEntity);
            }
            changeState(AppState.WaitForCylinderCenterPoint, "Delete Drawed Cylinder");
        }
        mEditingEntity = null;
        mDrawing.setTempWorkingPlane(null);
    }

    //Draw Cylinder
    private void drawCuboidBeginBasePoint1(Vec3d point, Vec3d baseNormal){
        mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(point, baseNormal));
        mEditingEntity = new CuboidEntity(ShaderCollection.getProgram(Programs.BodyProgram));

        float[] color = new float[]{0.7f, 0.7f, 0.5f, 1};
        ((CuboidEntity) mEditingEntity).setAttributes(point, baseNormal, 0.1f, 0.1f, 0.1f, color);
        mEntityList.add(mEditingEntity);

        changeState(AppState.WaitForCuboidBasePoint2, "Begin Draw Cuboid");
    }

    private void drawCuboidBeginBasePoint2(CollisionTrianglePoint collisionPoint, Boolean fix){
        if (mEditingEntity instanceof CuboidEntity && collisionPoint instanceof CollisionPlanePoint) {
            CuboidEntity cuboidEntity = (CuboidEntity) mEditingEntity;
            Vec3d pos = ((CollisionPlanePoint) collisionPoint).mTRS.copy();
            cuboidEntity.setDepthAndWidth(pos.y, pos.z);
            if (fix){

                //TODO das ganze auslagern und mTempWorkingPlane updaten, je nachdem in welche richtung user guckt
                Vec3d baseNormal = cuboidEntity.getBaseNormal().copy();
                Vec3d depthDir = new Vec3d();
                Vec3d widthDir = new Vec3d();
                VecMath.calcCrossedVectorsFromNormal(widthDir, depthDir, baseNormal);

//            mTempWorkingPlane = VecMath.calcPlaneFromPointAndNormal(cuboidEntity.getBaseVert(), depthDir);
//                mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(cuboidEntity.getBaseVert(), widthDir));
                mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(collisionPoint.collisionPos, widthDir));

                changeState(AppState.WaitForCuboidHeightPoint, "Update Cuboid Point2");
            }
        }
    }

    private void drawCuboidEndHeight(Vec3d point, Boolean fix){
        if (mEditingEntity instanceof CuboidEntity) {
            CuboidEntity cuboidEntity = (CuboidEntity) mEditingEntity;
            Plane basePlane = VecMath.calcPlaneFromPointAndNormal(cuboidEntity.getBaseVert(), cuboidEntity.getBaseNormal());

            float distance = VecMath.calcDistancePlanePoint(basePlane, point);
            cuboidEntity.setHeight(distance);
        }
        if (fix){
            changeState(AppState.WaitForCuboidBasePoint1, "End Draw Cuboid");
            mDrawing.setTempWorkingPlane(null);
        }
    }

    private void drawCuboidAbort(Boolean leave){
        if (leave){
            changeState(AppState.SelectEntityToCreate, "Leave Cuboid Mode");
        } else {
            if (mEditingEntity instanceof CuboidEntity) {
                mEntityList.remove(mEditingEntity);
            }
            changeState(AppState.WaitForCuboidBasePoint1, "Delete Drawed Cuboid");
        }
        mEditingEntity = null;
        mDrawing.setTempWorkingPlane(null);
    }

    public Drawing getDrawing() {
        return mDrawing;
    }

    public User getUser() {
        return mUser;
    }
}
