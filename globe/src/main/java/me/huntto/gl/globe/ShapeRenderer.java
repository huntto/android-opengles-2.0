package me.huntto.gl.globe;

import android.opengl.GLSurfaceView;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import me.huntto.gl.globe.shape.Shape;

import static android.opengl.GLES20.GL_BACK;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_CCW;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCullFace;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glFrontFace;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.setLookAtM;

public class ShapeRenderer implements GLSurfaceView.Renderer {
    private static final boolean D = BuildConfig.DEBUG;
    private float[] mViewMatrix = new float[16];
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
        glEnable(GL_DEPTH_TEST);

        glFrontFace(GL_CCW);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        glEnable(GL_BLEND);

        if (mInitGLCallback != null) {
            mInitGLCallback.onInitGL();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        perspectiveM(mProjectionMatrix, 0, 45, (float) width / (float) height, 1f, 10f);
        setLookAtM(mViewMatrix, 0, 0f, 0f, -2f, 0f, 0f, 0f, 0f, 1f, 0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final float[] viewProjectionMatrix = new float[16];
        multiplyMM(viewProjectionMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        for (Shape shape : mShapes) {
            shape.draw(viewProjectionMatrix);
        }
    }

    public interface InitGLCallback {
        void onInitGL();
    }
}
