package com.myAgeEducation.cbseClass2.maths.calendar;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class QuestionTemplate {

    public enum CalendarQuestionType {

        WEEKDAY_COUNT,
        MONTH_NAME,
        YEAR,
        DAYS_IN_MONTH,
        FIRST_DAY,
        LAST_DAY,
        //WEEKEND_COUNT,
        //HIGHLIGHTED_DATE,
        DAY_OF_DATE,
        NEXT_DAY,
        PREVIOUS_DAY
    }

    private final String template;
    private final boolean includeMonthYear;

    public QuestionTemplate(String template, boolean includeMonthYear) {
        this.template = template;
        this.includeMonthYear = includeMonthYear;
    }

    public String format(DayOfWeek weekday, Month month, int year) {
        String day = weekday.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        if (includeMonthYear) {
            return String.format(template,
                    day,
                    month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    year);
        }
        return String.format(template, day);
    }

    public static final List<QuestionTemplate> TEMPLATES = Arrays.asList(
            new QuestionTemplate("How many %ss are there in %s %d?", true),
            new QuestionTemplate("How many %ss are there in the given calendar?", false),
            new QuestionTemplate("How many %ss do you see in the given calendar?", false),
            new QuestionTemplate("Count the number of %ss in the calendar shown below.", false),
            new QuestionTemplate("Find the number of %ss in %s %d.", true)
    );

    public static final List<QuestionTemplate> MONTH_TYPE_TEMPLATES = Arrays.asList(
            new QuestionTemplate("Which month calendar is shown below?", false),
            new QuestionTemplate("Which month calendar is shown in the picture below?", false),
            new QuestionTemplate("Identify the month shown in the calendar.", false),
            new QuestionTemplate("Which month's calendar is displayed?", false)
    );

    public static final String[] DAYS_IN_MONTH_TEMPLATE = {
            "How many days are there in %s %d?",
            "How many days are there in the given month?",
            "Count the number of days in the calendar shown below.",
            "How many days does this month have?",
            "Find the total number of days in the given calendar."
    };

    public static final String[] YEAR_TEMPLATE = {
            "Which year's calendar is shown below?",
            "Identify the year of the given calendar.",
            "Which year does this calendar belong to?"
    };
}
