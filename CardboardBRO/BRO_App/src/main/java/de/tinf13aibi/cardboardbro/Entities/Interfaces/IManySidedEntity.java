package de.tinf13aibi.cardboardbro.Entities.Interfaces;

import de.tinf13aibi.cardboardbro.Entities.Triangulated.CuboidEntity;

/**
 * Created by dthom on 10.01.2016.
 */
public interface IManySidedEntity extends ITriangulatedEntity{
    CuboidEntity getHitBox();
}
