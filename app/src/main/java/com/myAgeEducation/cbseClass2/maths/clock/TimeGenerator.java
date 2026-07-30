package com.myAgeEducation.cbseClass2.maths.clock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TimeGenerator {

    private final List<ClockTime> times = new ArrayList<>();
    private int currentIndex = 0;

    public TimeGenerator() {
        generateTimes();
    }

    private void generateTimes() {

        times.clear();

        for (int hour = 1; hour <= 12; hour++) {
            for (int minute = 0; minute < 60; minute += 5) {
                times.add(new ClockTime(hour, minute));
            }
        }

        Collections.shuffle(times, new Random());
        currentIndex = 0;
    }

    public ClockTime nextTime() {

        if (currentIndex >= times.size()) {

            // Reshuffle after all 144 have been used
            Collections.shuffle(times, new Random());
            currentIndex = 0;
        }

        return times.get(currentIndex++);
    }

    public int totalTimes() {
        return times.size();   // 144
    }

    public int remainingTimes() {
        return times.size() - currentIndex;
    }
}
