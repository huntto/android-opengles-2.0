package me.huntto.gl.sphere;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;

import me.huntto.gl.common.BaseActivity;
import me.huntto.gl.sphere.shape.Sphere;

public class MainActivity extends BaseActivity {

    @NonNull
    @Override
    protected GLSurfaceView.Renderer newRenderer() {
        ShapeRenderer shapeRenderer = new ShapeRenderer(new ShapeRenderer.InitGLCallback() {
            @Override
            public void onInitGL() {
                Sphere.init(MainActivity.this);
            }
        });

        shapeRenderer.addShape(new Sphere());
        return shapeRenderer;
    }
}
