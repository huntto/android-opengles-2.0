package me.huntto.gl.skybox;

import android.opengl.GLSurfaceView;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.huntto.gl.skybox.shape.Shape;

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
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;

public class ShapeRenderer implements GLSurfaceView.Renderer {
    private static final boolean D = BuildConfig.DEBUG;
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private List<Shape> mShapes = new ArrayList<>();
    private InitGLCallback mInitGLCallback;

    private float mRotationX;
    private float mRotationY;

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

    public void handleTouchDrag(float deltaX, float deltaY) {
        mRotationX += deltaX / 16;
        mRotationY += deltaY / 16;

        if (mRotationY < -90) {
            mRotationY = -90;
        } else if (mRotationY > 90) {
            mRotationY = 90;
        }

        resetViewMatrix();
        rotateM(mViewMatrix, 0, -mRotationY, 1f, 0f, 0f);
        rotateM(mViewMatrix, 0, -mRotationX, 0f, 1f, 0f);
    }

    private void resetViewMatrix() {
        setLookAtM(mViewMatrix, 0,
                0f, 0f, 0f,
                0f, 0f, 1f,
                0f, 1f, 0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        perspectiveM(mProjectionMatrix, 0, 60, (float) width / (float) height, 1f, 10f);
        resetViewMatrix();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        for (Shape shape : mShapes) {
            shape.draw(mViewMatrix, mProjectionMatrix);
        }
    }

    public interface InitGLCallback {
        void onInitGL();
    }
}
