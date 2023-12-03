package com.example.opencv_integrate2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class TouchableView extends View {

    private Paint paint;
    private float touchX = 0, touchY = 0;
    private boolean isTouched = false;
    private int touchableWidth, touchableHeight;

    public TouchableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        touchableWidth = this.getWidth();
        touchableHeight = this.getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isTouched) {
            // Vẽ hình vuông xung quanh điểm chạm
            float squareSize = 200; // Kích thước vuông
            float left = touchX - squareSize / 2;
            float top = touchY - squareSize / 2;
            float right = touchX + squareSize / 2;
            float bottom = touchY + squareSize / 2;

            canvas.drawRect(left, top, right, bottom, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Khi người dùng chạm xuống màn hình
                touchX = event.getX();
                touchY = event.getY();

                touchableHeight = this.getHeight();
                touchableWidth = this.getWidth();

                Log.d("TocuhX", String.valueOf(touchX));
                Log.d("TouchY", String.valueOf(touchY));
                isTouched = true;
                invalidate(); // Yêu cầu vẽ lại View
                return true;
            case MotionEvent.ACTION_UP:
                // Khi người dùng nhấc tay lên
                isTouched = false;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public Rect getRoi(Mat mat, int width, int height) {
        int squareSize = 400; // Kích thước vuông
        if (touchX == 0.0 && touchY == 0.0) {
            int centerX = mat.width() / 2;
            int centerY = mat.height() / 2;
            return new Rect((int)(centerX - squareSize / 2), (int)(centerY - squareSize / 2), squareSize, squareSize);
        } else {

            int touchXInt = (int) ((touchX / touchableWidth) * width);
            int touchYInt = (int) ((touchY / touchableHeight) * height);
            int tlx = touchXInt - squareSize / 2;
            int tly = touchYInt - squareSize / 2 ;
            return new Rect(tlx, tly, squareSize, squareSize);
//        if (touchX == 0.0 && touchY == 0.0) {
//            // Trả về hình vuông ở giữa màn hình
//            int centerX = mat.width() / 2;
//            int centerY = mat.height() / 2;
//            return new Rect((int)(centerX - squareSize / 2), (int)(centerY - squareSize / 2), squareSize, squareSize);
//        } else {
//            if(touchY > (touchableHeight  / 2 - height) && touchY < (touchableHeight / 2 + height)){
//                int touchXInt = (int) ((touchX / touchableWidth) * width);
//                int touchYInt = (int) ((touchY - height) / 2);
//                int tlx = touchXInt - squareSize / 2;
//                int tly = touchYInt - squareSize / 2 ;
//                return new Rect(tlx, tly, squareSize, squareSize);
//
//            }else{
//                // Trả về hình vuông ở giữa màn hình
//                int centerX = mat.width() / 2;
//                int centerY = mat.height() / 2;
//                return new Rect((int)(centerX - squareSize / 2), (int)(centerY - squareSize / 2), squareSize, squareSize);
//            }
        }
    }
}

