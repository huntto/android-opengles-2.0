package me.huntto.gl.touchpointer;

import android.opengl.GLSurfaceView;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.huntto.gl.touchpointer.shape.Shape;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.setIdentityM;

public class ShapeRenderer implements GLSurfaceView.Renderer {
    private float[] mProjectionMatrix = new float[16];

    private List<Shape> mShapes = new ArrayList<>();
    private InitGLCallback mInitGLCallback;


    public ShapeRenderer(InitGLCallback callback) {
        mInitGLCallback = callback;
    }

    public void addShape(Shape shape) {
        mShapes.add(shape);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (mInitGLCallback != null) {
            mInitGLCallback.onInitGL();
        }
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        setIdentityM(mProjectionMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        for (Shape shape : mShapes) {
            shape.draw(mProjectionMatrix);
        }
    }

    public interface InitGLCallback {
        void onInitGL();
    }
}
