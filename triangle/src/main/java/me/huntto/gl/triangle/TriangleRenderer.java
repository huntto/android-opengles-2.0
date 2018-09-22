package me.huntto.gl.triangle;

import android.content.Context;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class TriangleRenderer implements GLSurfaceView.Renderer {
    private static final boolean D = BuildConfig.DEBUG;

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;

    private final FloatBuffer mVertexData;
    private Context mContext;

    private int mProgram;

    private static final String U_COLOR = "uColor";
    private int uColorLocation;

    private static final String A_POSITION = "aPosition";
    private int aPositionLocation;

    public TriangleRenderer(Context context) {
        float[] vertices = {
                -0.5f, -0.5f,
                0f, 0.5f,
                0.5f, -0.5f
        };

        mVertexData = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(vertices);

        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        String vertexShaderSource = TextResourceReader.readTextFromResource(mContext, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(mContext, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (D) {
            ShaderHelper.validateProgram(mProgram);
        }
        glUseProgram(mProgram);

        uColorLocation = glGetUniformLocation(mProgram, U_COLOR);
        aPositionLocation = glGetAttribLocation(mProgram, A_POSITION);

        mVertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(aPositionLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }
}
