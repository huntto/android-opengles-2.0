package me.huntto.gl.touchpointer;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import me.huntto.gl.common.BaseActivity;
import me.huntto.gl.touchpointer.shape.TouchPointer;

public class MainActivity extends BaseActivity implements View.OnTouchListener {
    private TouchPointer mTouchPointer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLSurfaceView.setOnTouchListener(this);
    }

    @NonNull
    @Override
    protected GLSurfaceView.Renderer newRenderer() {
        ShapeRenderer shapeRenderer = new ShapeRenderer(new ShapeRenderer.InitGLCallback() {
            @Override
            public void onInitGL() {
                TouchPointer.init(MainActivity.this);
            }
        });

        mTouchPointer = new TouchPointer();
        shapeRenderer.addShape(mTouchPointer);
        return shapeRenderer;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int width = mGLSurfaceView.getWidth();
        int height = mGLSurfaceView.getHeight();
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_POINTER_UP:
                int pointerCount = event.getPointerCount();
                mTouchPointer.resetPutPointer(pointerCount);
                for (int i = 0; i < pointerCount; i++) {
                    float x = event.getX(i) / width * 2 - 1;
                    float y = -event.getY(i) / height * 2 + 1;
                    float size = event.getTouchMajor(i);
                    mTouchPointer.putPointer(x, y, size);
                }
                break;
            default:
                mTouchPointer.resetPutPointer(0);
                break;
        }
        mTouchPointer.endPutPointer();
        mGLSurfaceView.requestRender();
        return true;
    }
}
