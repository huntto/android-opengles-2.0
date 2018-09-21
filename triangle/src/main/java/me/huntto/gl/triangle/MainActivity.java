package me.huntto.gl.triangle;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;

import me.huntto.gl.common.BaseActivity;

public class MainActivity extends BaseActivity {

    @NonNull
    @Override
    protected GLSurfaceView.Renderer newRenderer() {
        return new TriangleRenderer(this);
    }

}
