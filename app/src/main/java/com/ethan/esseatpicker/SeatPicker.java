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
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SeatPicker extends View {

    // background color for the main seating map
    int bgColor = Color.parseColor("#E5E8E8");

    public SeatPicker(Context context) {
        super(context);
    }

    public SeatPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public SeatPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SeatPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * Head Part
     */
    Paint headPaint;
    Bitmap headBitmap;
    float headHeight;

    /**
     * Seat Paint
     */
    Paint seatPaint;
    float screenHeight = 100;

    /**
     * 座位已售
     */
    private static final int SEAT_TYPE_SOLD = 1;

    /**
     * 座位已经选中
     */
    private static final int SEAT_TYPE_SELECTED = 2;

    /**
     * 座位可选
     */
    private static final int SEAT_TYPE_AVAILABLE = 3;

    /**
     * 座位不可用
     */
    private static final int SEAT_TYPE_NOT_AVAILABLE = 4;

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
     * Bitmap for thumbnail bg
     */
    Bitmap thumbnailBitmap;
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
    /**
     * 概览图白色方块高度
     */
    float rectHeight;

    /**
     * 概览图白色方块的宽度
     */
    float rectWidth;

    /**
     * 概览图上方块的水平间距
     */
    float overviewSpacing;

    /**
     * 概览图上方块的垂直间距
     */
    float overviewVerSpacing;

    /**
     * 概览图的比例
     */
    float overviewScale = 5f;

    /**
     * 整个概览图的宽度
     */
    float rectW;

    /**
     * 整个概览图的高度
     */
    float rectH;

    /**
     * maximum seats that can be selected
     */
    int maxSelected = Integer.MAX_VALUE;

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
    float[] seatBaseM = {0,0,0,0};
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

        //-----Line Paint -------//
        lineNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lineNumberPaint.setColor(Color.GREEN);
        lineNumberPaint.setTextSize(dip2Px(18));
//        lineNumberTxtHeight = lineNumberPaint.measureText("1");
        lineNumberTxtHeight = defaultImgH;
        lineNumberPaintFontMetrics = lineNumberPaint.getFontMetrics();
        lineNumberPaint.setTextAlign(Paint.Align.CENTER);

        //----OverView Paints----//
        focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        focusPaint.setColor(Color.YELLOW);
        focusPaint.setStyle(Paint.Style.STROKE);
        focusPaint.setStrokeWidth(dip2Px(1));

        thumbnailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbnailPaint.setStyle(Paint.Style.FILL);


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

        seatBaseM = new float[]{(headHeight + screenHeight + 50), (headHeight + screenHeight + 50) + seatHeight,
                (50 + spacing * 2), (50 + spacing * 2) + seatWidth};

        seatBitmapWidth = column * seatWidth + (column - 1) * spacing;
        seatBitmapHeight = row * seatHeight + (row - 1) * verSpacing;
        rectH = 1.1f * seatBitmapHeight / overviewScale;
        rectW = 1.1f * seatBitmapWidth / overviewScale;
        thumbnailBitmap = Bitmap.createBitmap((int) rectW, (int) rectH, Bitmap.Config.ARGB_4444);

        //
        for(int i=1;i<=row;i++) {
            lineNumbers.add(Integer.toString(i));
        }
    }

    public void initData(int row, int column){
        this.row = row;
        this.column = column;
        init();
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
        drawThumbNail(canvas);

    }
    Paint pathPaint;
    String screenName;

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    private void drawScreen(Canvas canvas){
        //TODO: Look at Path
        float startY = headHeight;
        float centerX = leftMargin + (seatBitmapWidth)/ 2f;

        Path path = new Path();
        path.moveTo(centerX,startY);
        float screenWidth = seatBitmapWidth*0.6f;
        path.lineTo(centerX - screenWidth / 2, startY);
        path.lineTo(centerX - screenWidth / 2 + screenHeight / 2, startY+ screenHeight);
        path.lineTo(centerX + screenWidth / 2 - screenHeight / 2, startY+ screenHeight);
        path.lineTo(centerX + screenWidth / 2, startY);

        pathPaint.setColor(Color.parseColor("#e2e2e2"));
        canvas.drawPath(path,pathPaint);

        if (!screenName.isEmpty()){
            pathPaint.setColor(Color.BLACK);
            pathPaint.setTextSize(30);
            float strWidth = pathPaint.measureText(screenName);
            float strHeight = getYForVertCenter(pathPaint, startY, startY + screenHeight);
            canvas.drawText(screenName,centerX - strWidth/2, strHeight,pathPaint);
        }
    }

    Paint focusPaint;
    Paint thumbnailPaint;
    private void drawThumbNail(Canvas canvas){
        thumbnailPaint.setColor(Color.parseColor("#7e000000"));
        thumbnailBitmap.eraseColor(Color.TRANSPARENT);
        canvas.drawRect(0,0,rectW,rectH,thumbnailPaint);
        rectHeight = seatHeight / overviewScale;
        rectWidth = seatWidth / overviewScale;
        for (int i = 0; i < row; i++) {
            float top = i* seatHeight+ i * verSpacing + 30;
            top /= overviewScale;
            if (top > getHeight()) continue;

            for (int j = 0; j < column; j++) {
                float left = j*(seatWidth + spacing) + 30;
                left /= overviewScale;
                if (left > getWidth()) continue;

                int seatType = getSeatType(i,j);
                switch (seatType){
                    case SEAT_TYPE_AVAILABLE:
                        thumbnailPaint.setColor(Color.WHITE);
                        break;
                    case SEAT_TYPE_SELECTED:
                        thumbnailPaint.setColor(Color.GREEN);
                        break;
                    case SEAT_TYPE_SOLD:
                        thumbnailPaint.setColor(Color.RED);
                        break;
                    case SEAT_TYPE_NOT_AVAILABLE:
                        continue;
                }
                canvas.drawRect(left,top,left + rectWidth, top + rectHeight, thumbnailPaint);
            }
        }
    }


    Paint lineNumberPaint;
    /**
     * left margin for line numbers
     */
    float leftMargin = 50;
    ArrayList<String> lineNumbers = new ArrayList<>();
    private void drawNumber(Canvas canvas){
        //TODO: 没想好x怎么算
        float x = leftMargin;
        float startY = headHeight + screenHeight + x + defaultImgH; // defaultImgH is necessary
        for (String line:lineNumbers) {
            // arg x and y here are relative to text's baseline
            canvas.drawText(line,x,startY+ (verSpacing+ lineNumberTxtHeight)*lineNumbers.indexOf(line),lineNumberPaint);
        }
    }

    private void drawSeat(Canvas canvas){
        for (int i = 0; i < row; i++) {
            float top = i* seatHeight+ i * verSpacing + (headHeight + screenHeight + 50);
            if (top > getHeight()) continue;

            for (int j = 0; j < column; j++) {
                float left = j*(seatWidth + spacing)+(50 + spacing*2);
                if (left > getWidth()) continue;
                tempMatrix.setTranslate(left,top);
                tempMatrix.postScale(xScale1,yScale1,left,top);

                int seatType = getSeatType(i,j);
                switch (seatType){
                    case SEAT_TYPE_AVAILABLE:
                        canvas.drawBitmap(seatBitmap,tempMatrix,seatPaint);
                        break;
                    case SEAT_TYPE_SELECTED:
                        canvas.drawBitmap(selectedSeatBitmap,tempMatrix,seatPaint);
                        break;
                    case SEAT_TYPE_SOLD:
                        canvas.drawBitmap(soldSeatBitmap,tempMatrix,seatPaint);
                        break;
                    case SEAT_TYPE_NOT_AVAILABLE:
                        break;
                }

            }
        }
    }

    private int getSeatType(int row, int column){
        // logic of selected
        if (selectedSeats.contains(getID(row,column))){
            return SEAT_TYPE_SELECTED;
        }

        if (seatClassifier != null) {
            if (!seatClassifier.isValid(row,column)){
                return SEAT_TYPE_NOT_AVAILABLE;
            }else if (seatClassifier.isSold(row, column)){
                return SEAT_TYPE_SOLD;
            }
        }
        return SEAT_TYPE_AVAILABLE;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get the x,y corresponding to the content view
        int y = (int) event.getY();
        int x = (int) event.getX();
        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        return true;
    }

    ArrayList<Integer> selectedSeats = new ArrayList<Integer>();

    GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            int y = (int) e.getY();
            int x = (int) e.getX();
            int[] seat = getSeatID(x,y);

            if (seat == null) return false;
            if (seatClassifier != null && !seatClassifier.isSold(seat[0],seat[1]) &&
                    seatClassifier.isValid(seat[0],seat[1])){
                int uid = getID(seat[0],seat[1]);
                if (selectedSeats.contains(uid)){
                    // index vs. object
                    selectedSeats.remove(Integer.valueOf(uid));
                    seatClassifier.unSelected(seat[0],seat[1]);
                }else{
                    if (selectedSeats.size() >= maxSelected) {
                        Toast.makeText(getContext(), "Can only select " + maxSelected + " seats",
                                Toast.LENGTH_SHORT).show();}
                    else{
                        selectedSeats.add(uid);
                        seatClassifier.selected(seat[0],seat[1]);
                    }
                }

                invalidate();
            }


            return super.onSingleTapConfirmed(e);
        }
    });

    // contor pairing function
    private int getID(int row,int column){
        return ((row + column)*(row + column + 1)/2) + column;
    }


    private int[] getSeatID(int x, int y){
        float top = seatBaseM[0];
        float left = seatBaseM[2];
        int tempColumn = (int)(x - left) / (seatWidth+spacing);
        int tempRow = (int)(y - top) / (seatHeight+verSpacing);

        float xMax = left + (++tempColumn) * (seatWidth+spacing) - spacing;
        float yMax = top +  (++tempRow)* (seatHeight+verSpacing) - verSpacing;
        if (x > xMax || y > yMax || tempRow > this.row
                || tempColumn > this.column || x < left || y < top) {
            return null;
        }
        return new int[]{--tempRow, --tempColumn};
    }

    // -------------- Utility Functions ------------------ //

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


    private SeatClassifier seatClassifier;
    public interface SeatClassifier {
        boolean isValid (int row, int column);
        boolean isSold (int row, int column);
        void selected(int row, int colum);
        void unSelected(int row, int column);
    }

    public void setSeatClassifier(SeatClassifier seatClassifier){
        this.seatClassifier = seatClassifier;
    }

    public void setMaxSelected(int maxSelected) {
        this.maxSelected = maxSelected;
    }
}
