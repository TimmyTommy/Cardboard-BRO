package de.tinf13aibi.cardboardbro.GestureUtils;

/**
 * Created by felix on 24.03.2015.
 */

public enum MyoStatus {
    UNKNOWN("unknown"),
    DISCONNECTED("disconnected"),
    DETACHED("detached"),
    UNSYNCED("unsynced"),
    LOCKED("locked"),
    UNCENTERED("locked"),
    SEARCHING_MYO("Searching MYO"),
    //    ESCAPED("escaped"),
    IDLE("idle");

    private String mStringValue;

    MyoStatus(String value){
        mStringValue = value;
    }

    @Override
    public String toString() {
        return mStringValue;
    }

    public String getValue() {
        return mStringValue;
    }

    public static MyoStatus parseString(String value) {
        for(MyoStatus v : values())
            if(v.getValue().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException();
    }
}
