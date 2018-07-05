package app.myandroidhello.com.myapplication;

import android.graphics.Typeface;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ButtonView extends View
{
    public Rect button1;
    public Rect button2;
    public Rect button3;
    public Rect button4;
    public Rect button5;
    public Rect button6;
    public Rect button7;
    public Rect button8;
    public Rect button9;
    public Rect button0;
    public Rect buttonSubmit;
    public Rect buttonStar;
    public Rect buttonHash;

    private Paint paint1;
    private Paint paint2;
    private Paint paintText;

    private MainActivity activity;

    public ButtonView(Context context)
    {
        super(context);
    }

    public ButtonView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        int col1Left = 50;
        int col1Right = 450;

        int row1Top = 100;
        int row1Bottom = 550;

        int col2Left = 525;
        int col2Right = 925;

        int col3Left = 1000;
        int col3Right = 1400;

        int row2Top = 600;
        int row2Bottom = 1050;

        int row3Top = 1100;
        int row3Bottom = 1550;

        int row4Top = 1600;
        int row4Bottom = 2050;


        // create the buttons
        button1 = new Rect(col1Left, row1Top, col1Right, row1Bottom);
        button2 = new Rect(col2Left, row1Top, col2Right, row1Bottom);
        button3 = new Rect(col3Left, row1Top, col3Right, row1Bottom);
        button4 = new Rect(col1Left, row2Top, col1Right, row2Bottom);
        button5 = new Rect(col2Left, row2Top, col2Right, row2Bottom);
        button6 = new Rect(col3Left, row2Top, col3Right, row2Bottom);
        button7 = new Rect(col1Left, row3Top, col1Right, row3Bottom);
        button8 = new Rect(col2Left, row3Top, col2Right, row3Bottom);
        button9 = new Rect(col3Left, row3Top, col3Right, row3Bottom);
        button0 = new Rect(col2Left, row4Top, col2Right, row4Bottom);
        buttonSubmit = new Rect(col1Left, row4Top, col1Right, row4Bottom);

        //Rect(int left, int top, int right, int bottom)

        // create the Paint and set its color
        paint1 = new Paint();
        paint1.setColor(Color.GRAY);
        paint2 = new Paint();
        paint2.setColor(Color.BLUE);

        //Setting up text
        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(125);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setTextAlign(Paint.Align.CENTER);

        // Initialize a typeface object to draw text on canvas
        Typeface typeface = Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD_ITALIC);

        // Set the paint font
        paintText.setTypeface(typeface);

    }

    public void setMainActivity(MainActivity a)
    {
        activity = a;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawRect(button1, paint1);
        canvas.drawRect(button2, paint2);
        canvas.drawRect(button3, paint1);
        canvas.drawRect(button4, paint2);
        canvas.drawRect(button5, paint1);
        canvas.drawRect(button6, paint2);
        canvas.drawRect(button7, paint1);
        canvas.drawRect(button8, paint2);
        canvas.drawRect(button9, paint1);
        canvas.drawRect(button0, paint2);
        canvas.drawRect(buttonSubmit, paint1);

        canvas.drawText("1", button1.centerX(), button1.centerY(), paintText);
        canvas.drawText("2", button2.centerX(), button2.centerY(), paintText);
        canvas.drawText("3", button3.centerX(), button3.centerY(), paintText);
        canvas.drawText("4", button4.centerX(), button4.centerY(), paintText);
        canvas.drawText("5", button5.centerX(), button5.centerY(), paintText);
        canvas.drawText("6", button6.centerX(), button6.centerY(), paintText);
        canvas.drawText("7", button7.centerX(), button7.centerY(), paintText);
        canvas.drawText("8", button8.centerX(), button8.centerY(), paintText);
        canvas.drawText("9", button9.centerX(), button9.centerY(), paintText);
        canvas.drawText("0", button0.centerX(), button0.centerY(), paintText);
        canvas.drawText("Submit", buttonSubmit.centerX(), buttonSubmit.centerY(), paintText);

    }


}
