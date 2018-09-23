package me.huntto.gl.texturecube;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;

import me.huntto.gl.common.BaseActivity;
import me.huntto.gl.texturecube.shape.TextureCube;

public class MainActivity extends BaseActivity {

    @NonNull
    @Override
    protected GLSurfaceView.Renderer newRenderer() {
        ShapeRenderer shapeRenderer = new ShapeRenderer(new ShapeRenderer.InitGLCallback() {
            @Override
            public void onInitGL() {
                TextureCube.init(MainActivity.this);
            }
        });

        shapeRenderer.addShape(new TextureCube());
        return shapeRenderer;
    }
}
