package de.tinf13aibi.cardboardbro;

import de.tinf13aibi.cardboardbro.Enums.AppState;

/**
 * Created by dthom on 09.01.2016.
 */
public class StateMachine {
    private User mUser;
    private AppState mState = AppState.SelectAction;

    public StateMachine(User user){
        mUser = user;
    }
}
