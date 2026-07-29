package com.myAgeEducation.cbseClass2;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DateTimeUtils {

    public static final String[] WEEKDAYS = {
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday"
    };

    public static int getRandomCalendarWeekday()
    {
        /*
        Calendar.SUNDAY = 1
        Calendar.MONDAY = 2
        ...
        Calendar.SATURDAY = 7
         */
        Random random = new Random();
        int dayIndex = random.nextInt(7);
        return dayIndex + 1;
    }

    /*
    Usage:
    int[] counts = CalendarUtils.getWeekdayCounts(2026, Calendar.AUGUST);

    System.out.println("Sunday = " + counts[Calendar.SUNDAY - 1]);
    System.out.println("Monday = " + counts[Calendar.MONDAY - 1]);
    System.out.println("Tuesday = " + counts[Calendar.TUESDAY - 1]);
    System.out.println("Wednesday = " + counts[Calendar.WEDNESDAY - 1]);
    System.out.println("Thursday = " + counts[Calendar.THURSDAY - 1]);
    System.out.println("Friday = " + counts[Calendar.FRIDAY - 1]);
    System.out.println("Saturday = " + counts[Calendar.SATURDAY - 1]);
     */
    public static int[] getWeekdayCounts(int year, int month) {

        int[] counts = new int[7];

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= daysInMonth; day++) {

            calendar.set(Calendar.DAY_OF_MONTH, day);

            int weekday = calendar.get(Calendar.DAY_OF_WEEK);

            counts[weekday - 1]++;
        }

        return counts;
    }

    private static final String[] WEEKDAY_COUNT_TEMPLATES = {

            "How many %ss are there in %s %d?",

            "How many %ss are there in the given calendar?",

            "Count the number of %ss in the calendar shown below.",

            "How many times does %s occur in the given calendar?",

            "Find the number of %ss in %s %d.",

            "Look at the calendar below. How many %ss are there?",

            "Count the %ss in the calendar given below.",

            "In the given calendar, how many %ss are there?",

            "How many %ss can you find in the calendar?",

            "Observe the calendar and count the %ss."
    };
}
