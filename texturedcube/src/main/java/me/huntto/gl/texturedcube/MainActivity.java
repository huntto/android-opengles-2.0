package me.huntto.gl.texturedcube;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;

import me.huntto.gl.common.BaseActivity;
import me.huntto.gl.texturedcube.shape.TexturedCube;

public class MainActivity extends BaseActivity {

    @NonNull
    @Override
    protected GLSurfaceView.Renderer newRenderer() {
        ShapeRenderer shapeRenderer = new ShapeRenderer(new ShapeRenderer.InitGLCallback() {
            @Override
            public void onInitGL() {
                TexturedCube.init(MainActivity.this);
            }
        });

        shapeRenderer.addShape(new TexturedCube());
        return shapeRenderer;
    }
}
