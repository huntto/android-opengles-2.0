package me.huntto.gl.globe;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import me.huntto.gl.common.BaseActivity;
import me.huntto.gl.globe.shape.Globe;

public class MainActivity extends BaseActivity {
    private Globe mGlobe;

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
        ShapeRenderer shapeRenderer = new ShapeRenderer(new ShapeRenderer.InitGLCallback() {
            @Override
            public void onInitGL() {
                Globe.init(MainActivity.this);
            }
        });

        mGlobe = new Globe();
        shapeRenderer.addShape(mGlobe);
        return shapeRenderer;
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
                    mPrevX = event.getX() / width * 2 - 1;
                    mPrevY = -event.getY() / height * 2 + 1;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX() / width * 2 - 1;
                    float y = -event.getY() / height * 2 + 1;
                    float vx = x - mPrevX;
                    float vy = y - mPrevY;

                    float dist = (float) Math.sqrt(vx * vx + vy * vy);
                    mGlobe.rotate((float) Math.toDegrees(dist), vy, vx, 0);

                    mPrevX = x;
                    mPrevY = y;

                    mGLSurfaceView.requestRender();
                    break;
            }
            return true;
        }
    };
}
