package com.lexidome.lexagame.chatsupp;

public interface Orl {
    void onStart();
    void onCancel();
    void onFinish(long recordTime,boolean limitReached);
    void onLessThanSecond();
}
