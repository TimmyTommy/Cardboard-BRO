package de.tinf13aibi.cardboardbro.Engine;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Entities.Lined.TextEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by Tommy on 20.04.2016.
 */
public class StateWaitForEntityPlacePoint extends StateBase implements IState {
    public StateWaitForEntityPlacePoint(DrawingContext drawingContext) {
        super(drawingContext);
        ITriangulatedEntity entity = (ITriangulatedEntity)mDrawingContext.getEditingEntity();
        mPosition = entity.getBaseVert();
        mBaseNormal = entity.getBaseNormal();
    }

    private Vec3d mPosition;
    private Vec3d mBaseNormal;

    @Override
    public void processOnDrawEye(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        super.processOnDrawEye(view, perspective, lightPosInEyeSpace);
    }

    @Override
    public void processOnNewFrame(float[] headView, Vec3d armForwardVec) {
        super.processOnNewFrame(headView, armForwardVec);

        ArrayList<IEntity> entities = mDrawing.getEntityListWithFloorAndCanvas();
        entities.remove(mDrawingContext.getEditingEntity());
        mUser.calcArmPointingAt(entities);

        moveEntityToPosition(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal(), false);
    }

    @Override
    public void processInputAction(InputAction inputAction) {
        switch (inputAction) {
            case DoStateBack: placeEntityAbort(); break;
            case DoEndSelect: moveEntityToPosition(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal(), true);
        }
    }

    private void placeEntityAbort() {
        //if (mDrawingContext.getEditingEntity() instanceof TextEntity) {
            //mDrawing.getEntityList().remove(mDrawingContext.getEditingEntity());
        //}
        moveEntityToPosition(mPosition, mBaseNormal, true);
        changeState(new StateSelectEntityToMove(mDrawingContext), "Abort Text");
        mDrawingContext.setEditingEntity(null);
        //mDrawing.setTempWorkingPlane(null);
    }

    private void moveEntityToPosition(Vec3d pos, Vec3d baseNormal, Boolean fix) {
        if (mDrawingContext.getEditingEntity() instanceof ITriangulatedEntity) {
            ITriangulatedEntity entity = (ITriangulatedEntity)mDrawingContext.getEditingEntity();
            //Vec3d normal = VecMath.calcVecTimesScalar(baseNormal, -1);
            Vec3d normal = VecMath.calcNormalizedVector(baseNormal);
            entity.setPositionAndOrientation(pos, normal, fix);
            //TODO: if instance of TextEntity...
            //textEntity.updatePosition(mUser.getInvHeadView(), pos);
            if (fix) {
                //mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(pos, baseNormal));
                changeState(new StateSelectEntityToMove(mDrawingContext), "Placed Entity");
            }
        }
    }
}
