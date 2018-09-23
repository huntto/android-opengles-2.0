package me.huntto.gl.cube;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;

import me.huntto.gl.common.BaseActivity;
import me.huntto.gl.cube.shape.Cube;
import me.huntto.gl.cube.shape.Triangle;

public class MainActivity extends BaseActivity {

    @NonNull
    @Override
    protected GLSurfaceView.Renderer newRenderer() {
        ShapeRenderer shapeRenderer = new ShapeRenderer(new ShapeRenderer.InitGLCallback() {
            @Override
            public void onInitGL() {
                Cube.init(MainActivity.this);
                Triangle.init(MainActivity.this);
            }
        });

        shapeRenderer.addShape(new Cube());
        shapeRenderer.addShape(new Triangle());
        return shapeRenderer;
    }
}
