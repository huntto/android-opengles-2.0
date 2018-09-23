package me.huntto.gl.touchpointer.shape;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;
import me.huntto.gl.touchpointer.R;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;

public class TouchPointer implements Shape {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final int VERTEX_DEFAULT_CAPACITY = POSITION_COMPONENT_COUNT * 25;

    private static int sProgram;

    private static final String A_POSITION = "aPosition";
    private static final String U_MATRIX = "uMatrix";
    private static final String U_COLOR = "uColor";

    private static int aPositionLocation;
    private static int uMatrixLocation;
    private static int uColorLocation;

    private final float[] mModelMatrix = new float[16];
    private final float[] mMatrix = new float[16];

    private static FloatBuffer sVertexBuffer;

    private final Object VERTEX_SYNC = new Object();
    private int mPointerCount;
    private float[] mPointers;
    private int mPointerIndex;

    public static void init(Context context) {
        sVertexBuffer = ByteBuffer
                .allocateDirect(VERTEX_DEFAULT_CAPACITY * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();


        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.point_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.point_fragment_shader);
        sProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        glUseProgram(sProgram);

        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
        uMatrixLocation = glGetUniformLocation(sProgram, U_MATRIX);
        uColorLocation = glGetUniformLocation(sProgram, U_COLOR);
    }

    public TouchPointer() {
        setIdentityM(mModelMatrix, 0);
    }

    @Override
    public void draw(float[] viewProjectionMatrix) {
        glUseProgram(sProgram);

        multiplyMM(mMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);

        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);

        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);

        synchronized (VERTEX_SYNC) {
            sVertexBuffer.position(0);
            glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                    GL_FLOAT, false, 0, sVertexBuffer);

            glEnableVertexAttribArray(aPositionLocation);

            glDrawArrays(GL_POINTS, 0, mPointerCount);
        }
    }


    public void resetPutPointer(int maxPointerCount) {
        if (mPointers == null || mPointers.length < POSITION_COMPONENT_COUNT * maxPointerCount) {
            mPointers = new float[POSITION_COMPONENT_COUNT * maxPointerCount];
        }
        mPointerIndex = 0;
    }

    public void putPointer(float x, float y, float size) {
        mPointers[mPointerIndex] = x;
        mPointers[mPointerIndex+1] = y;
        mPointerIndex += 2;
    }

    public void endPutPointer() {
        if (mPointers == null) {
            return;
        }
        synchronized (VERTEX_SYNC) {
            sVertexBuffer.position(0);
            sVertexBuffer.put(mPointers, 0, mPointerIndex);
            mPointerCount = mPointerIndex / POSITION_COMPONENT_COUNT;
        }
    }

}
