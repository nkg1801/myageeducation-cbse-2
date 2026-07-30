package com.myAgeEducation.cbseClass2.maths.calendar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class CalendarImageGenerator {

    private static final int PADDING = 30;
    private static final float MONTH_TEXT_SIZE = 100;
    private static final float WEEKDAY_TEXT_SIZE = 60f;
    private static final float DATE_TEXT_SIZE = 72f;

    private static final float HEADER_HEIGHT = 180f;
    private static final float WEEKDAY_Y = 270f;
    private static final float DATE_START_Y = 330f;

    private static final int SUNDAY_BG =
            Color.parseColor("#FFEBEE");   // Light Red

    private static final int SATURDAY_BG =
            Color.parseColor("#E3F2FD");   // Light Blue

    private static final int SUNDAY_TEXT =
            Color.parseColor("#E53935");

    private static final int SATURDAY_TEXT =
            Color.parseColor("#1E88E5");

    private static final int WEEKDAY_TEXT =
            Color.BLACK;

    public static Bitmap generateCalendar(int year,
            int month,
            int size) {

        Bitmap bitmap = Bitmap.createBitmap(
                size,
                size,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setTextAlign(Paint.Align.CENTER);

        float width = size;
        float height = size;

        drawHeader(
                canvas,
                paint,
                width,
                year,
                month);

        drawWeekHeader(
                canvas,
                paint,
                width);

        drawDates(
                canvas,
                paint,
                width,
                height,
                year,
                month);



        return bitmap;
    }

    //-----------------------------------------------------
    // Header
    //-----------------------------------------------------

    private static void drawHeader(
            Canvas canvas,
            Paint paint,
            float width,
            int year,
            int month) {

        //float headerHeight = HEADER_HEIGHT;

        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(Color.parseColor("#4CAF50"));

        canvas.drawRoundRect(
                20,
                20,
                width - 20,
                HEADER_HEIGHT,
                20,
                20,
                bg);

        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(MONTH_TEXT_SIZE);

        String monthName =
                new DateFormatSymbols().getMonths()[month];

        canvas.drawText(
                monthName + " " + year,
                width / 2,
                140,
                paint);
    }

    //-----------------------------------------------------
    // Week Header
    //-----------------------------------------------------

    private static void drawWeekHeader(
            Canvas canvas,
            Paint paint,
            float width) {

        String[] days = {
                "Sun",
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri",
                "Sat"
        };

        float cellWidth =
                (width - PADDING * 2) / 7f;

        paint.setColor(Color.BLACK);
        paint.setTextSize(WEEKDAY_TEXT_SIZE);
        paint.setTypeface(Typeface.DEFAULT_BOLD);

        for (int i = 0; i < 7; i++) {

            float x = PADDING + cellWidth * i + cellWidth / 2;

            if (i == 0) {
                // Sunday
                paint.setColor(Color.parseColor("#E53935"));   // Red
            }
            else if (i == 6) {
                // Saturday
                paint.setColor(Color.parseColor("#1E88E5"));   // Blue
            }
            else {
                // Weekdays
                paint.setColor(Color.BLACK);
            }

            canvas.drawText(days[i], x, WEEKDAY_Y, paint);
        }

        Paint line = new Paint();

        line.setColor(Color.LTGRAY);

        canvas.drawLine(
                PADDING,
                WEEKDAY_Y + 30,
                width - PADDING,
                WEEKDAY_Y + 30,
                line);
    }

    //-----------------------------------------------------
    // Dates
    //-----------------------------------------------------

    private static void drawDates(Canvas canvas, Paint paint, float width, float height, int year, int month) {

        Paint sundayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sundayPaint.setColor(SUNDAY_BG);
        sundayPaint.setStyle(Paint.Style.FILL);

        Paint saturdayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        saturdayPaint.setColor(SATURDAY_BG);
        saturdayPaint.setStyle(Paint.Style.FILL);

        Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2);

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                year,
                month,
                1);

        int firstDay =
                calendar.get(Calendar.DAY_OF_WEEK);

        int daysInMonth =
                calendar.getActualMaximum(
                        Calendar.DAY_OF_MONTH);

        //float top = 330;

        float cellWidth =
                (width - PADDING * 2) / 7f;

        float cellHeight =
                (height - DATE_START_Y  - 20) / 6f;

        paint.setTypeface(Typeface.DEFAULT);

        paint.setTextSize(cellHeight * 0.50f);

        Paint grid = new Paint();

        grid.setColor(Color.parseColor("#DDDDDD"));

        int currentDay = 1;

        for (int row = 0; row < 6; row++) {

            for (int col = 0; col < 7; col++) {

                float left =
                        PADDING + col * cellWidth;

                float topCell =
                        DATE_START_Y  + row * cellHeight;

                float right =
                        left + cellWidth;

                float bottom =
                        topCell + cellHeight;

                /*canvas.drawRect(
                        left,
                        topCell,
                        right,
                        bottom,
                        grid);*/

                grid = new Paint(Paint.ANTI_ALIAS_FLAG);
                grid.setStyle(Paint.Style.STROKE);
                grid.setStrokeWidth(2);
                grid.setColor(Color.LTGRAY);

                //canvas.drawRect(left, topCell, right, bottom, grid);

                // Fill weekend cells
                if (col == 0) {

                    canvas.drawRect(
                            left,
                            topCell,
                            right,
                            bottom,
                            sundayPaint);

                }
                else if (col == 6) {

                    canvas.drawRect(
                            left,
                            topCell,
                            right,
                            bottom,
                            saturdayPaint);

                }

// Draw grid border
                canvas.drawRect(
                        left,
                        topCell,
                        right,
                        bottom,
                        gridPaint);

                if (row == 0 &&
                        col < firstDay - 1)
                    continue;

                if (currentDay > daysInMonth)
                    continue;

                /*drawDate(
                        canvas,
                        paint,
                        left,
                        topCell,
                        cellWidth,
                        cellHeight,
                        currentDay);*/

                drawDate(
                        canvas,
                        paint,
                        left,
                        topCell,
                        cellWidth,
                        cellHeight,
                        currentDay,
                        col);

                currentDay++;
            }
        }
    }

    //-----------------------------------------------------
    // Draw One Date
    //-----------------------------------------------------

    private static void drawDateOld(
            Canvas canvas,
            Paint paint,
            float left,
            float top,
            float cellWidth,
            float cellHeight,
            int day, int column) {

        // Sunday
        if (column == 0) {
            paint.setColor(Color.parseColor("#E53935"));
        }

        // Saturday
        else if (column == 6) {
            paint.setColor(Color.parseColor("#1E88E5"));
        }

        // Monday-Friday
        else {
            paint.setColor(Color.BLACK);
        }

        //paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT_BOLD);

        paint.setTextSize(cellHeight * 0.5f);

        Paint.FontMetrics fm = paint.getFontMetrics();

        float x = left + cellWidth / 2f;

        float y = top + cellHeight / 2f
                - (fm.ascent + fm.descent) / 2f;

        canvas.drawText(String.valueOf(day), x, y, paint);
    }

    private static void drawDate(
            Canvas canvas,
            Paint paint,
            float left,
            float top,
            float cellWidth,
            float cellHeight,
            int day,
            int column) {

        switch (column) {

            case 0:
                paint.setColor(SUNDAY_TEXT);
                break;

            case 6:
                paint.setColor(SATURDAY_TEXT);
                break;

            default:
                paint.setColor(WEEKDAY_TEXT);
        }

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(cellHeight * 0.50f);

        Paint.FontMetrics fm = paint.getFontMetrics();

        float x = left + cellWidth / 2f;

        float y = top + cellHeight / 2f
                - (fm.ascent + fm.descent) / 2f;

        canvas.drawText(
                String.valueOf(day),
                x,
                y,
                paint);
    }

}
