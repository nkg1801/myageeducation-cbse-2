package com.myAgeEducation.cbseClass2;

import androidx.annotation.NonNull;

public class ClockTime {

    public final int hour;
    public final int minute;

    public ClockTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%d:%02d", hour, minute);
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }
}
