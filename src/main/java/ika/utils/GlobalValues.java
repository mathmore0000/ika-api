package ika.utils;

public enum GlobalValues {
    MIN_REQUEST(1),
    MAX_REQUEST(500);


    private final int GlobalValue;
    GlobalValues(int GlobalValue) { this.GlobalValue = GlobalValue; }
    public int getValue() { return GlobalValue; }
}
