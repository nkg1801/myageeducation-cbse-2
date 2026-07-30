package com.myAgeEducation.cbseClass2.maths.clock;

import com.myAgeEducation.cbsecommon.Question;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ClockQuestionGenerator {
    private static final Random RANDOM = new Random();
    public static final List<String> QUESTION_TEMPLATES = Arrays.asList(
            "What time does the clock show?",
            "What is the time on the clock?",
            "What time is shown on the clock?",
            "Can you tell the time shown on the clock?",
            "What time is it according to the clock?",
            "Read the time shown on the clock.",
            "Look at the clock and tell the time.",
            "Identify the time displayed on the clock.",
            "What time does the clock indicate?",
            "Can you tell what time the clock says?",
            "Look at the clock. What time do you see?",
            "Tell the time shown on the clock face.",
            "Observe the clock and state the time.",
            "Based on the clock image, what time is shown?",
            "What time is represented on the analog clock?",
            "Which of the following times is shown on the clock?",
            "Select the correct time shown on the clock.",
            "Choose the time represented by the clock."
    );

    public static Question generateQuestion()
    {
        String questionText = QUESTION_TEMPLATES.get(RANDOM.nextInt(QUESTION_TEMPLATES.size()));
        TimeGenerator timeGenerator = new TimeGenerator();
        Question question = new Question();
        question.setQuestion(questionText);
        ClockTime clockTime = timeGenerator.nextTime();
        question.setImage("use_clock_generator_code;" + clockTime.hour + "_" + clockTime.minute);
        int twelveHourFormat = clockTime.hour	> 12 ? clockTime.hour - 12 : clockTime.hour;
        String minute = clockTime.minute < 10 ? "0" + clockTime.minute : String.valueOf(clockTime.minute);
        String answer = twelveHourFormat + ":" + minute;

        List<String> options = generateOptions(clockTime);
        question.setOption1(options.get(0));
        question.setOption2(options.get(1));
        question.setOption3(options.get(2));
        question.setOption4(options.get(3));
        question.setAnswer(answer);
        return question;
    }

    private static List<String> generateOptions(ClockTime correctTime) {

        Set<String> options = new HashSet<>();
        String correct = formatTime(correctTime);
        options.add(correct);
        Random random = new Random();

        while (options.size() < 4) {
            int type = random.nextInt(4);
            int hour = correctTime.getHour();
            int minute = correctTime.getMinute();

            switch (type) {

                // Wrong hour
                case 0:
                    hour = (hour % 12) + 1;
                    break;

                // Previous hour
                case 1:
                    hour = hour == 1 ? 12 : hour - 1;
                    break;

                // +5 minutes
                case 2:
                    minute += 5;
                    if (minute >= 60) {
                        minute = 0;
                        hour = (hour % 12) + 1;
                    }
                    break;

                // -5 minutes
                case 3:
                    minute -= 5;
                    if (minute < 0) {
                        minute = 55;
                        hour = hour == 1 ? 12 : hour - 1;
                    }
                    break;
            }

            options.add(formatTime(new ClockTime(hour, minute)));
        }

        List<String> list = new ArrayList<>(options);
        Collections.shuffle(list);
        return list;
    }

    private static String formatTime(ClockTime time) {
        return String.format("%d:%02d",
                time.getHour(),
                time.getMinute());
    }
}
