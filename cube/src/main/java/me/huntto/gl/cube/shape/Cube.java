package me.huntto.gl.cube.shape;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;
import me.huntto.gl.cube.R;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class Cube implements Shape {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;

    private static int sProgram;

    private static final String A_POSITION = "aPosition";
    private static final String A_COLOR = "aColor";
    private static final String U_MVP_MATRIX = "uMVPMatrix";

    private static int uMVPMatrixLocation;
    private static int aColorLocation;
    private static int aPositionLocation;

    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    FloatBuffer colorBuffer;

    public static void init(Context context) {
        final float vertices[] = {
                // front
                -0.5f, -0.5f, 0.5f,     // left bottom front
                -0.5f, 0.5f, 0.5f,      // left top front
                0.5f, 0.5f, 0.5f,       // right top front

                0.5f, 0.5f, 0.5f,       // right top front
                0.5f, -0.5f, 0.5f,      // right bottom front
                -0.5f, -0.5f, 0.5f,     // left bottom front

                // back
                -0.5f, -0.5f, -0.5f,    // left bottom back
                0.5f, 0.5f, -0.5f,      // right top back
                -0.5f, 0.5f, -0.5f,     // left top back

                0.5f, 0.5f, -0.5f,      // right top back
                0.5f, -0.5f, -0.5f,     // right bottom back
                -0.5f, -0.5f, -0.5f,    // left bottom back

                // top
                -0.5f, 0.5f, 0.5f,      // left top front
                -0.5f, 0.5f, -0.5f,     // left top back
                0.5f, 0.5f, -0.5f,      // right top back

                0.5f, 0.5f, -0.5f,      // right top back
                0.5f, 0.5f, 0.5f,       // right top front
                -0.5f, 0.5f, 0.5f,      // left top front

                // left
                -0.5f, -0.5f, -0.5f,    // left bottom back
                -0.5f, 0.5f, -0.5f,     // left top back
                -0.5f, 0.5f, 0.5f,      // left top front

                -0.5f, 0.5f, 0.5f,      // left top front
                -0.5f, -0.5f, 0.5f,     // left bottom front
                -0.5f, -0.5f, -0.5f,    // left bottom back

                // right
                0.5f, -0.5f, 0.5f,      // right bottom front
                0.5f, 0.5f, 0.5f,       // right top front
                0.5f, 0.5f, -0.5f,      // right top back

                0.5f, 0.5f, -0.5f,      // right top back
                0.5f, -0.5f, -0.5f,     // right bottom back
                0.5f, -0.5f, 0.5f,      // right bottom front

                // bottom
                -0.5f, -0.5f, -0.5f,    // left bottom back
                -0.5f, -0.5f, 0.5f,     // left bottom front
                0.5f, -0.5f, 0.5f,      // right bottom front

                0.5f, -0.5f, 0.5f,      // right bottom front
                0.5f, -0.5f, -0.5f,     // right bottom back
                -0.5f, -0.5f, -0.5f,    // left bottom back
        };

        final float colors[] = {
                // front
                1.0f, 0.0f, 0.0f,     // red
                1.0f, 0.0f, 0.0f,     // red
                1.0f, 0.0f, 0.0f,     // red
                1.0f, 0.0f, 0.0f,     // red
                1.0f, 0.0f, 0.0f,     // red
                1.0f, 0.0f, 0.0f,     // red

                // back
                0.0f, 1.0f, 0.0f,     // green
                0.0f, 1.0f, 0.0f,     // green
                0.0f, 1.0f, 0.0f,     // green
                0.0f, 1.0f, 0.0f,     // green
                0.0f, 1.0f, 0.0f,     // green
                0.0f, 1.0f, 0.0f,     // green

                // top
                0.0f, 0.0f, 1.0f,     // blue
                0.0f, 0.0f, 1.0f,     // blue
                0.0f, 0.0f, 1.0f,     // blue
                0.0f, 0.0f, 1.0f,     // blue
                0.0f, 0.0f, 1.0f,     // blue
                0.0f, 0.0f, 1.0f,     // blue

                // left
                1.0f, 1.0f, 0.0f,     // yellow
                1.0f, 1.0f, 0.0f,     // yellow
                1.0f, 1.0f, 0.0f,     // yellow
                1.0f, 1.0f, 0.0f,     // yellow
                1.0f, 1.0f, 0.0f,     // yellow
                1.0f, 1.0f, 0.0f,     // yellow

                // right
                1.0f, 0.0f, 1.0f,      // purple
                1.0f, 0.0f, 1.0f,      // purple
                1.0f, 0.0f, 1.0f,      // purple
                1.0f, 0.0f, 1.0f,      // purple
                1.0f, 0.0f, 1.0f,      // purple
                1.0f, 0.0f, 1.0f,      // purple

                // bottom
                0.0f, 1.0f, 1.0f,     // cyan
                0.0f, 1.0f, 1.0f,     // cyan
                0.0f, 1.0f, 1.0f,     // cyan
                0.0f, 1.0f, 1.0f,     // cyan
                0.0f, 1.0f, 1.0f,     // cyan
                0.0f, 1.0f, 1.0f,     // cyan
        };


        FloatBuffer vertexBuffer = ByteBuffer
                .allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        FloatBuffer colorBuffer = ByteBuffer.allocateDirect(colors.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fragment_shader);
        sProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        glUseProgram(sProgram);

        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
        aColorLocation = glGetAttribLocation(sProgram, A_COLOR);
        uMVPMatrixLocation = glGetUniformLocation(sProgram, U_MVP_MATRIX);

        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, 0, vertexBuffer);
        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT,
                GL_FLOAT, false, 0, colorBuffer);
    }

    public Cube() {
        setIdentityM(mModelMatrix, 0);
        translateM(mModelMatrix, 0, 0, 0, -0.5f);
    }

    @Override
    public void draw(float[] viewProjectionMatrix) {
        glUseProgram(sProgram);

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);

        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mMVPMatrix, 0);

        glEnableVertexAttribArray(aPositionLocation);
        glEnableVertexAttribArray(aColorLocation);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glDisableVertexAttribArray(aColorLocation);
        glDisableVertexAttribArray(aPositionLocation);
    }
}
