package me.huntto.gl.particles;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

import me.huntto.gl.common.BaseActivity;
import me.huntto.gl.particles.shape.ParticleSystem;


public class MainActivity extends BaseActivity implements View.OnTouchListener {
    private ParticleSystem mParticleSystem;
    private Random mRandom = new Random(47);

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mGLSurfaceView.setOnTouchListener(this);
    }

    @NonNull
    @Override
    protected GLSurfaceView.Renderer newRenderer() {
        ShapeRenderer shapeRenderer = new ShapeRenderer(new ShapeRenderer.InitGLCallback() {
            @Override
            public void onInitGL() {
                ParticleSystem.init(MainActivity.this);
            }
        });

        mParticleSystem = new ParticleSystem(100);
        shapeRenderer.addShape(mParticleSystem);
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
                long startTime = System.currentTimeMillis();
                int pointerCount = event.getPointerCount();
                for (int i = 0; i < pointerCount; i++) {
                    float x = event.getX(i) / width * 2 - 1;
                    float y = -event.getY(i) / height * 2 + 1;
                    int color = mRandom.nextInt();
                    mParticleSystem.addParticle(x, y, color, startTime);
                }
                break;
            default:
                break;
        }
        return true;
    }
}
