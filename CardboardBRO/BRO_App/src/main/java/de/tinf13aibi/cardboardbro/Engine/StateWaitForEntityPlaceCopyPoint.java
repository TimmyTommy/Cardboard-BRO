package de.tinf13aibi.cardboardbro.Engine;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.ITriangulatedEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;
import de.tinf13aibi.cardboardbro.Geometry.VecMath;

/**
 * Created by dthom on 18.05.2016.
 */
public class StateWaitForEntityPlaceCopyPoint extends StateBase implements IState {
    public StateWaitForEntityPlaceCopyPoint(DrawingContext drawingContext) {
        super(drawingContext);
        ITriangulatedEntity entity = (ITriangulatedEntity) mDrawingContext.getEditingEntity();
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
            case DoStateBack:
                placeEntityAbort();
                break;
            case DoEndSelect:
                moveEntityToPosition(mUser.getArmCrosshair().getPosition(), mUser.getArmCrosshair().getNormal(), true);
        }
    }

    private void placeEntityAbort() {
        //if (mDrawingContext.getEditingEntity() instanceof TextEntity) {
        //mDrawing.getEntityList().remove(mDrawingContext.getEditingEntity());
        //}
        mDrawing.getEntityList().remove(mDrawingContext.getEditingEntity());
        changeState(new StateSelectEntityToCopy(mDrawingContext), "Abort Copy");
        mDrawingContext.setEditingEntity(null);
        //mDrawing.setTempWorkingPlane(null);
    }

    private void moveEntityToPosition(Vec3d pos, Vec3d baseNormal, Boolean fix) {
        if (mDrawingContext.getEditingEntity() instanceof ITriangulatedEntity) {
            ITriangulatedEntity entity = (ITriangulatedEntity) mDrawingContext.getEditingEntity();
            //Vec3d normal = VecMath.calcVecTimesScalar(baseNormal, -1);
            Vec3d normal = VecMath.calcNormalizedVector(baseNormal);
            entity.setPositionAndOrientation(pos, normal, fix);
            //TODO: if instance of TextEntity...
            //textEntity.updatePosition(mUser.getInvHeadView(), pos);
            if (fix) {
                //mDrawing.setTempWorkingPlane(VecMath.calcPlaneFromPointAndNormal(pos, baseNormal));
                changeState(new StateSelectEntityToCopy(mDrawingContext), "Placed Copied Entity");
            }
        }
    }
}
