package com.example.schoolproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLBlurView extends FrameLayout {

    private static final float BLUR_RADIUS = 15f; // Change blur intensity
    private Bitmap bitmap;
    private Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

    public GLBlurView(Context context) {
        super(context);
        init();
    }

    public GLBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // Draw child views into a bitmap
        if (bitmap == null || bitmap.getWidth() != getWidth() || bitmap.getHeight() != getHeight()) {
            if (getWidth() > 0 && getHeight() > 0) {
                bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            }
        }
        if (bitmap != null) {
            Canvas tempCanvas = new Canvas(bitmap);
            super.dispatchDraw(tempCanvas);
            Bitmap blurred = blurBitmap(bitmap);
            canvas.drawBitmap(blurred, 0, 0, paint);
        } else {
            super.dispatchDraw(canvas);
        }
    }

    private Bitmap blurBitmap(Bitmap bitmap) {
        // Simple manual fast blur (very basic stack blur effect)

        Bitmap output = Bitmap.createBitmap(bitmap);
        int radius = (int) BLUR_RADIUS;

        for (int i = radius; i < bitmap.getWidth() - radius; i++) {
            for (int j = radius; j < bitmap.getHeight() - radius; j++) {
                int r = 0, g = 0, b = 0;
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        int pixel = bitmap.getPixel(i + dx, j + dy);
                        r += (pixel >> 16) & 0xFF;
                        g += (pixel >> 8) & 0xFF;
                        b += pixel & 0xFF;
                    }
                }
                int div = (radius * 2 + 1) * (radius * 2 + 1);
                r /= div;
                g /= div;
                b /= div;
                output.setPixel(i, j, (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return output;
    }
}
