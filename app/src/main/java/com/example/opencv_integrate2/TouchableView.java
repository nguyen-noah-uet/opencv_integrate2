package com.example.opencv_integrate2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class TouchableView extends View {

    private Paint paint;
    private float touchX = 0, touchY = 0;
    private boolean isTouched = false;

    public TouchableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
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
//                Log.d("TocuhX", String.valueOf(touchX));
//                Log.d("TouchY", String.valueOf(touchY));
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

    public Rect getRoi(Mat mat) {
        float alpha = 0.4f;
        int squareSize = 200; // Kích thước vuông
        if (touchX == 0.0 && touchY == 0.0) {
            // Trả về hình vuông ở giữa màn hình
            int centerX = mat.width() / 2;
            int centerY = mat.height() / 2;
            return new Rect((int)(centerX - squareSize / 2), (int)(centerY - squareSize / 2), squareSize, squareSize);
        } else {
            int touchXInt = (int) touchX;
            int touchYInt = (int) touchY;
            int left = touchXInt - squareSize / 2;
            int top = touchYInt - squareSize / 2 ;

            left -= alpha * touchXInt;
            top -= alpha * touchYInt;



            return new Rect(left, top, 200, 200);
        }
    }
}

