package me.huntto.gl.sphere.shape;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;
import me.huntto.gl.sphere.R;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINE_STRIP;
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
import static android.opengl.Matrix.translateM;

public class Sphere implements Shape {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int ANGLE_SPAN = 10;
    private static final float RADIUS = 0.5f;

    private static int sProgram;

    private static final String A_POSITION = "aPosition";
    private static final String U_COLOR = "uColor";
    private static final String U_MATRIX = "uMatrix";

    private static int uMatrixLocation;
    private static int uColorLocation;
    private static int aPositionLocation;

    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private static FloatBuffer sPositionBuffer;

    public static void init(Context context) {
        final int vAngle180 = 180;
        final int hAngle360 = 360;

        int vCount = vAngle180 / ANGLE_SPAN + 1;
        int hCount = hAngle360 / ANGLE_SPAN + 1;

        float[] vertices = new float[vCount * hCount * POSITION_COMPONENT_COUNT];

        int index = 0;
        for (int vAngle = 0; vAngle <= vAngle180; vAngle += ANGLE_SPAN) {
            for (int hAngle = 0; hAngle < hAngle360; hAngle += ANGLE_SPAN) {

                float x = (float) (RADIUS * Math.sin(Math.toRadians(vAngle))
                        * Math.cos(Math.toRadians(hAngle)));
                float y = (float) (RADIUS * Math.sin(Math.toRadians(vAngle))
                        * Math.sin(Math.toRadians(hAngle)));
                float z = (float) (RADIUS * Math.cos(Math.toRadians(vAngle)));

                vertices[index] = x;
                vertices[index + 1] = y;
                vertices[index + 2] = z;

                index += POSITION_COMPONENT_COUNT;
            }
        }

        sPositionBuffer = ByteBuffer
                .allocateDirect(index * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        sPositionBuffer.put(vertices, 0, index);


        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fragment_shader);
        sProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        glUseProgram(sProgram);

        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
        uColorLocation = glGetUniformLocation(sProgram, U_COLOR);
        uMatrixLocation = glGetUniformLocation(sProgram, U_MATRIX);

    }

    public Sphere() {
        setIdentityM(mModelMatrix, 0);
        translateM(mModelMatrix, 0, 0, 0, 0.5f);
    }

    @Override
    public void draw(float[] viewProjectionMatrix) {
        glUseProgram(sProgram);

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);

        glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);

        sPositionBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, 0, sPositionBuffer);

        glEnableVertexAttribArray(aPositionLocation);

        glDrawArrays(GL_LINE_STRIP, 0, sPositionBuffer.limit() / POSITION_COMPONENT_COUNT);

        glDisableVertexAttribArray(aPositionLocation);
    }
}
