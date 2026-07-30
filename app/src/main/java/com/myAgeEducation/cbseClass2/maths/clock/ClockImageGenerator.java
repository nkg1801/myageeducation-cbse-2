package com.myAgeEducation.cbseClass2.maths.clock;
import android.graphics.*;
import com.myAgeEducation.cbseClass2.Util;
import java.util.Random;

public class ClockImageGenerator {
    public static Bitmap generateClock(int hour, int minute, int size, Util.BackgroundTheme theme) {

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        bgPaint.setColor(getBackgroundColor(theme));

        canvas.drawRect(0, 0, size, size, bgPaint);

        float cx = size / 2f;
        float cy = size / 2f;
        float radius = size * 0.42f;

        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(size * 0.02f);
        circlePaint.setColor(Color.BLACK);

        canvas.drawCircle(cx, cy, radius, circlePaint);

        Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.BLACK);

        Paint numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        numberPaint.setColor(Color.rgb(25,45,220));
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTextSize(size * 0.07f);

        // Draw ticks & numbers
        for(int i=0;i<60;i++){

            double angle=Math.toRadians(i*6-90);

            float startRadius=(i%5==0)?radius*0.88f:radius*0.94f;
            float endRadius=radius;

            float x1=(float)(cx+Math.cos(angle)*startRadius);
            float y1=(float)(cy+Math.sin(angle)*startRadius);

            float x2=(float)(cx+Math.cos(angle)*endRadius);
            float y2=(float)(cy+Math.sin(angle)*endRadius);

            tickPaint.setStrokeWidth((i%5==0)?6:2);

            canvas.drawLine(x1,y1,x2,y2,tickPaint);

            if(i%5==0){

                int num=i/5;
                if(num==0) num=12;

                float tx=(float)(cx+Math.cos(angle)*radius*0.74f);
                float ty=(float)(cy+Math.sin(angle)*radius*0.74f+numberPaint.getTextSize()/3);

                canvas.drawText(String.valueOf(num),tx,ty,numberPaint);
            }
        }

        drawHands(canvas,cx,cy,radius,hour,minute,size);
        return bitmap;
    }

    private static void drawHands(Canvas canvas,float cx,float cy,float radius, int hour,int minute,int size){

        Paint minutePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        minutePaint.setColor(Color.RED);
        minutePaint.setStrokeWidth(size*0.015f);
        Paint hourPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        hourPaint.setColor(new Color().rgb(0,140,0));
        hourPaint.setStrokeWidth(size*0.03f);
        double minuteAngle=Math.toRadians(minute*6-90);
        double hourAngle=Math.toRadians((hour%12+minute/60.0)*30-90);
        float mx=(float)(cx+Math.cos(minuteAngle)*radius*0.82f);
        float my=(float)(cy+Math.sin(minuteAngle)*radius*0.82f);
        float hx=(float)(cx+Math.cos(hourAngle)*radius*0.55f);
        float hy=(float)(cy+Math.sin(hourAngle)*radius*0.55f);
        canvas.drawLine(cx,cy,mx,my,minutePaint);
        canvas.drawLine(cx,cy,hx,hy,hourPaint);
        Paint center=new Paint(Paint.ANTI_ALIAS_FLAG);
        center.setColor(Color.BLACK);
        canvas.drawCircle(cx,cy,size*0.02f,center);
    }

    private static int getBackgroundColor(Util.BackgroundTheme theme) {
        switch (theme) {

            case LIGHT_BLUE:
                return Color.parseColor("#E3F2FD");

            case LIGHT_GREEN:
                return Color.parseColor("#E8F5E9");

            case LIGHT_YELLOW:
                return Color.parseColor("#FFFDE7");

            case LIGHT_PINK:
                return Color.parseColor("#FCE4EC");

            case LIGHT_PURPLE:
                return Color.parseColor("#F3E5F5");

            case LIGHT_ORANGE:
                return Color.parseColor("#FFF3E0");

            case LIGHT_CYAN:
                return Color.parseColor("#E0F7FA");

            case CREAM:
                return Color.parseColor("#FFF8E1");

            case RANDOM:

                int[] colors = {

                        Color.parseColor("#E3F2FD"),
                        Color.parseColor("#E8F5E9"),
                        Color.parseColor("#FFFDE7"),
                        Color.parseColor("#FCE4EC"),
                        Color.parseColor("#F3E5F5"),
                        Color.parseColor("#FFF3E0"),
                        Color.parseColor("#E0F7FA"),
                        Color.parseColor("#FFF8E1")

                };

                return colors[new Random().nextInt(colors.length)];

            default:
                return Color.WHITE;
        }
    }
}