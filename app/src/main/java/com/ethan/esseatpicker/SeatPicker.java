package com.ethan.esseatpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;

public class SeatPicker extends View {

    // background color for the main seating map
    int bgColor = Color.parseColor("#E5E8E8");

    public SeatPicker(Context context) {
        super(context);
    }

    public SeatPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
        init();
    }

    public SeatPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SeatPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * init Head Part
     */
    Paint headPaint;
    Bitmap headBitmap;
    float headHeight;

    /**
     * seat Paint
     */
    Paint seatPaint;

    float screenHeight = 100;

    /**
     * Res ID for seats
     */
    int seatSelectedResID;
    int seatSoldResID;
    int seatAvailableResID;

    // Bitmaps for seats
    /**
     * Bitmap for an available seat
     */
    Bitmap seatBitmap;

    /**
     * Bitmap for a selected seat
     */
    Bitmap selectedSeatBitmap;

    /**
     * Bitmap for a sold seat
     */
    Bitmap soldSeatBitmap;

    /**
     * 默认的座位图宽度,如果使用的自己的座位图片比这个尺寸大或者小,会缩放到这个大小
     */
    private float defaultImgW = 45;

    /**
     * 默认的座位图高度
     */
    private float defaultImgH = 40;

    /**
     * 座位图片的宽度
     */
    private int seatWidth;

    /**
     * 座位图片的高度
     */
    private int seatHeight;

    /**
     * 整个座位图的宽度
     */
    int seatBitmapWidth;

    /**
     * 整个座位图的高度
     */
    int seatBitmapHeight;

    /**
     * 座位水平间距
     */
    int spacing;

    /**
     * 座位垂直间距
     */
    int verSpacing;

    int column = 15;
    int row = 10;

    private void init(Context context, AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.SeatPicker);
        seatSelectedResID = typedArray.getResourceId(R.styleable.SeatPicker_seat_selected, R.drawable.seat_selected);
        seatSoldResID = typedArray.getResourceId(R.styleable.SeatPicker_seat_sold, R.drawable.seat_sold);
        seatAvailableResID = typedArray.getResourceId(R.styleable.SeatPicker_seat_available, R.drawable.seat_available);
        typedArray.recycle();
    }

    float xScale1 = 1;
    float yScale1 = 1;

    float lineNumberTxtHeight = 1;
    Paint.FontMetrics lineNumberPaintFontMetrics;
    private void init() {
        // Paints
        seatPaint = new Paint();
        headPaint = new Paint();
        headHeight = dip2Px(35);
        headPaint.setStyle(Paint.Style.FILL);
        headPaint.setTextSize(30);
        headPaint.setAntiAlias(true);

        //-----Path Paint-------//
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setColor(Color.parseColor("#e2e2e2"));

        //
        lineNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lineNumberPaint.setColor(Color.GREEN);
        lineNumberPaint.setTextSize(getResources().getDisplayMetrics().density * 18);
//        lineNumberTxtHeight = lineNumberPaint.measureText("1");
        lineNumberTxtHeight = defaultImgH;
        lineNumberPaintFontMetrics = lineNumberPaint.getFontMetrics();
        lineNumberPaint.setTextAlign(Paint.Align.CENTER);

        // Bitmaps
        seatBitmap = BitmapFactory.decodeResource(getResources(), seatAvailableResID);
        selectedSeatBitmap = BitmapFactory.decodeResource(getResources(), seatSelectedResID);
        soldSeatBitmap = BitmapFactory.decodeResource(getResources(), seatSoldResID);

        // Scale
        float scaleX = defaultImgW / seatBitmap.getWidth();
        float scaleY = defaultImgH / seatBitmap.getHeight();
        xScale1 = scaleX;
        yScale1 = scaleY;

        seatHeight = (int) (seatBitmap.getHeight() * yScale1);
        seatWidth = (int) (seatBitmap.getWidth() * xScale1);

        spacing = (int) dip2Px(7);
        verSpacing = (int) dip2Px(12);

        seatBitmapWidth = column * seatWidth + (column - 1) * spacing;
        seatBitmapHeight = row * seatHeight + (row - 1) * verSpacing;

        //
        for(int i=1;i<=row;i++) {
            lineNumbers.add(Integer.toString(i));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        if (headBitmap == null) {
            headBitmap= drawTopPart();
        }
        // draw the head top
        canvas.drawBitmap(headBitmap,0,0,null);
        drawSeat(canvas);
        drawNumber(canvas);
        drawScreen(canvas);


    }
    Paint pathPaint;
    private void drawScreen(Canvas canvas){
        //TODO: Look at Path
        float startY = headHeight;
        float centerX = seatBitmapWidth * getMatrixScaleX() / 2;

        Path path = new Path();
        path.moveTo(centerX,startY);
        float screenWidth = seatBitmapWidth*0.6f;
        path.lineTo(centerX - screenWidth / 2, startY);
        path.lineTo(centerX - screenWidth / 2 + screenHeight / 2, startY+ screenHeight);
        path.lineTo(centerX + screenWidth / 2 - screenHeight / 2, startY+ screenHeight);
        path.lineTo(centerX + screenWidth / 2, startY);

        canvas.drawPath(path,pathPaint);
    }

    Paint lineNumberPaint;
    ArrayList<String> lineNumbers = new ArrayList<>();
    private void drawNumber(Canvas canvas){
        // 没想好x怎么算
        float x = 50;
        float startY = headHeight + screenHeight + x + defaultImgH;
        for (String line:lineNumbers) {
            canvas.drawText(line,x,startY+ (verSpacing+ lineNumberTxtHeight)*lineNumbers.indexOf(line),lineNumberPaint);
        }
    }

    private void drawSeat(Canvas canvas){
        for (int i = 0; i < row; i++) {
            float top = i* seatHeight+ i * verSpacing + (headHeight + screenHeight + 50);
            float bottom = top + seatHeight;
            if (top > getHeight()) continue;

            for (int j = 0; j < column; j++) {
                float left = j*(seatWidth + spacing)+(50 + spacing*2);
                float right = left + seatWidth;
                if (left > getWidth()) continue;
                tempMatrix.setTranslate(left,top);
                tempMatrix.postScale(xScale1,yScale1,left,top);
                canvas.drawBitmap(seatBitmap,tempMatrix,seatPaint);
            }
        }
    }



    // Draw the top : info about pics stand for
    Matrix tempMatrix = new Matrix();
    Bitmap drawTopPart(){
        float txtY = getYForVertCenter(headPaint,0,headHeight);

        int txtWidthForTwo = (int) headPaint.measureText("12");
        float spacing = dip2Px(20);
        float spacing1 = dip2Px(5);

        int picNum = 3;
        float width  = (seatWidth+spacing+spacing1+txtWidthForTwo)*picNum;

        // Create a new Bitmap
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), (int) headHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas  = new Canvas(bitmap);

        headPaint.setColor(Color.WHITE);
//        headPaint.setColor(Color.parseColor("#7e000000"));
        canvas.drawRect(0, 0, getWidth(), headHeight, headPaint);
        headPaint.setColor(Color.BLACK);

        float startX = (getWidth() - width) / 2;
        float headY = (headHeight - seatHeight) / 2;
        tempMatrix.setScale(xScale1,yScale1);
        tempMatrix.postTranslate(startX,headY);
        canvas.drawBitmap(seatBitmap,tempMatrix,headPaint);
        float txtX1 = startX+seatWidth+spacing1;
        canvas.drawText("可选",txtX1,txtY,headPaint);

        tempMatrix.setScale(xScale1,yScale1);
        tempMatrix.postTranslate(txtX1+txtWidthForTwo+spacing,headY);
        canvas.drawBitmap(selectedSeatBitmap,tempMatrix,headPaint);
        float txtX2 = txtX1+txtWidthForTwo+spacing+ seatWidth+spacing1;
        canvas.drawText("已选",txtX2,txtY,headPaint);

        tempMatrix.setScale(xScale1,yScale1);
        tempMatrix.postTranslate(txtX2+txtWidthForTwo+spacing,headY);
        canvas.drawBitmap(soldSeatBitmap,tempMatrix,headPaint);
        float txtX3 = txtX2+txtWidthForTwo+spacing+ seatWidth+spacing1;
        canvas.drawText("已售",txtX3,txtY,headPaint);

        return bitmap;
    }

    private float dip2Px(float value){
        return getResources().getDisplayMetrics().density * value;
    }

    private float getYForVertCenter(Paint p, float top, float bottom) {
        Paint.FontMetrics fontMetrics = p.getFontMetrics();
        int baseline = (int) ((bottom + top - fontMetrics.ascent) / 2);
        return baseline;
    }

    float[] m = new float[9];
    Matrix matrix = new Matrix();

    private float getTranslateX() {
        matrix.getValues(m);
        return m[2];
    }

    private float getTranslateY() {
        matrix.getValues(m);
        return m[5];
    }

    private float getMatrixScaleY() {
        matrix.getValues(m);
        return m[4];
    }

    private float getMatrixScaleX() {
        matrix.getValues(m);
        return m[Matrix.MSCALE_X];
    }

}
