package de.tinf13aibi.cardboardbro.Engine;

/**
 * Created by dthom on 09.01.2016.
 */
public enum InputAction {
    DoNothing,      //Default
    DoBeginSelect,  //MYO: Faust machen     Controller: Button X Down
    DoEndSelect,    //MYO: Faust loslassen  Controller: Button X Up
    DoStateBack,    //MYO: Finger spreizen  Controller: Button B
    DoCenter,       //MYO: Doppeltippen     Controller: Button Select
    DoMoveIn3D,     //Button am Cardboard
    DoMoveInPlane,  //Controller: Analogstick
    DoMoveUp,       //Controller: Button A
    DoMoveDown,     //Controller: Button Y
    DoUndo          //MYO: Links-Wischen    Controller: Button Y
}
