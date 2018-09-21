package me.huntto.gl.common;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public abstract class BaseActivity extends AppCompatActivity {
    private GLSurfaceView mGLSurfaceView;
    private boolean mRendererSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        setContentView(mGLSurfaceView);

        if (GLESChecker.checkSupportES2(this)) {
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(newRenderer());
            mRendererSet = true;
        } else {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRendererSet) {
            mGLSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRendererSet) {
            mGLSurfaceView.onResume();
        }
    }

    @NonNull
    protected abstract GLSurfaceView.Renderer newRenderer();
}
