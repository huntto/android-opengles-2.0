package me.huntto.gl.skybox;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import me.huntto.gl.common.BaseActivity;
import me.huntto.gl.skybox.shape.Skybox;

public class MainActivity extends BaseActivity {
    private ShapeRenderer mShapeRenderer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView.setOnTouchListener(mTouchListener);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @NonNull
    @Override
    protected GLSurfaceView.Renderer newRenderer() {
        mShapeRenderer = new ShapeRenderer(new ShapeRenderer.InitGLCallback() {
            @Override
            public void onInitGL() {
                Skybox.init(MainActivity.this);
            }
        });

        mShapeRenderer.addShape(new Skybox());
        return mShapeRenderer;
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        private float mPrevX;
        private float mPrevY;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int width = mGLSurfaceView.getWidth();
            int height = mGLSurfaceView.getHeight();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mPrevX = event.getX();
                    mPrevY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX();
                    float y = event.getY();
                    float deltaX = x - mPrevX;
                    float deltaY = y - mPrevY;
                    mShapeRenderer.handleTouchDrag(deltaX, deltaY);

                    mPrevX = x;
                    mPrevY = y;

                    mGLSurfaceView.requestRender();
                    break;
            }
            return true;
        }
    };
}
