package me.huntto.gl.touchpointer.shape;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;
import me.huntto.gl.touchpointer.R;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_POINTS;
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
    private static final int POINT_SIZE_COMPONENT_COUNT = 1;
    private static final int MAX_POINTER_COUNT = 25;

    private static int sProgram;

    private static final String A_POSITION = "aPosition";
    private static final String A_POINT_SIZE = "aPointSize";
    private static final String U_MATRIX = "uMatrix";
    private static final String U_COLOR = "uColor";

    private static int aPositionLocation;
    private static int aPointSizeLocation;
    private static int uMatrixLocation;
    private static int uColorLocation;

    private final float[] mModelMatrix = new float[16];
    private final float[] mMatrix = new float[16];

    private static FloatBuffer sPositionBuffer;
    private static FloatBuffer sPointSizeBuffer;

    private final Object BUFFER_SYNC = new Object();
    private volatile int mPointerCount;

    private float[] mPositions;
    private float[] mPointSizes;

    private int mPointerIndex;

    public static void init(Context context) {
        sPositionBuffer = ByteBuffer
                .allocateDirect(MAX_POINTER_COUNT * POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        sPointSizeBuffer = ByteBuffer
                .allocateDirect(MAX_POINTER_COUNT * POINT_SIZE_COMPONENT_COUNT * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();


        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.point_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.point_fragment_shader);
        sProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        glUseProgram(sProgram);

        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
        aPointSizeLocation = glGetAttribLocation(sProgram, A_POINT_SIZE);
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

        synchronized (BUFFER_SYNC) {
            sPositionBuffer.position(0);
            glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                    GL_FLOAT, false, 0, sPositionBuffer);

            sPointSizeBuffer.position(0);
            glVertexAttribPointer(aPointSizeLocation, POINT_SIZE_COMPONENT_COUNT,
                    GL_FLOAT, false, 0, sPointSizeBuffer);

            glEnableVertexAttribArray(aPositionLocation);
            glEnableVertexAttribArray(aPointSizeLocation);

            glDrawArrays(GL_POINTS, 0, mPointerCount);
        }
    }


    public void resetPutPointer(int maxPointerCount) {
        int positionCount = POSITION_COMPONENT_COUNT * maxPointerCount;
        if (mPositions == null || mPositions.length < positionCount) {
            mPositions = new float[positionCount];
        }

        int pointSizeCount = POINT_SIZE_COMPONENT_COUNT * maxPointerCount;
        if (mPointSizes == null || mPointSizes.length < pointSizeCount) {
            mPointSizes = new float[pointSizeCount];
        }
        mPointerIndex = 0;
    }

    public void putPointer(float x, float y, float size) {
        int pointSizeIndex = mPointerIndex * POINT_SIZE_COMPONENT_COUNT;
        mPointSizes[pointSizeIndex] = size;

        int positionIndex = mPointerIndex * POSITION_COMPONENT_COUNT;
        mPositions[positionIndex] = x;
        mPositions[positionIndex + 1] = y;

        mPointerIndex++;
    }

    public void endPutPointer() {
        if (mPositions == null) {
            return;
        }
        synchronized (BUFFER_SYNC) {
            sPointSizeBuffer.position(0);
            sPointSizeBuffer.put(mPointSizes, 0, mPointerIndex * POINT_SIZE_COMPONENT_COUNT);

            sPositionBuffer.position(0);
            sPositionBuffer.put(mPositions, 0, mPointerIndex * POSITION_COMPONENT_COUNT);

            mPointerCount = mPointerIndex;
        }
    }

}
