package me.huntto.gl.globe.shape;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;
import me.huntto.gl.common.util.TextureHelper;
import me.huntto.gl.globe.R;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setRotateM;

public class Globe implements Shape {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 3;

    private static final int ANGLE_STEP = 5;
    private static final float RADIUS = 0.5f;

    private static int sProgram;

    private static final String A_POSITION = "aPosition";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";
    private static final String U_MATRIX = "uMatrix";

    private static int uMatrixLocation;
    private static int uTextureUnitLocation;
    private static int aPositionLocation;

    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private final float[] mTemp = new float[32];

    private static FloatBuffer sPositionBuffer;

    private static int sTextureId;

    public static void init(Context context) {
        final int angle180 = 180;
        final int angle360 = 360;

        float[] vertices = new float[angle180 / ANGLE_STEP
                * angle360 / ANGLE_STEP
                * 6
                * POSITION_COMPONENT_COUNT];

        int count = 0;
        for (int longitude = 0; longitude < angle360; longitude += ANGLE_STEP) {
            for (int latitude = 0; latitude < angle180; latitude += ANGLE_STEP) {
                vertices[count++] = longitude;
                vertices[count++] = latitude;
                vertices[count++] = RADIUS;

                vertices[count++] = longitude;
                vertices[count++] = latitude + ANGLE_STEP;
                vertices[count++] = RADIUS;

                vertices[count++] = longitude + ANGLE_STEP;
                vertices[count++] = latitude + ANGLE_STEP;
                vertices[count++] = RADIUS;

                vertices[count++] = longitude + ANGLE_STEP;
                vertices[count++] = latitude + ANGLE_STEP;
                vertices[count++] = RADIUS;

                vertices[count++] = longitude + ANGLE_STEP;
                vertices[count++] = latitude;
                vertices[count++] = RADIUS;

                vertices[count++] = longitude;
                vertices[count++] = latitude;
                vertices[count++] = RADIUS;
            }
        }

        sPositionBuffer = ByteBuffer
                .allocateDirect(count * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        sPositionBuffer.put(vertices, 0, count);


        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.sphere_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.texture_fragment_shader);
        sProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        glUseProgram(sProgram);

        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
        uTextureUnitLocation = glGetUniformLocation(sProgram, U_TEXTURE_UNIT);
        uMatrixLocation = glGetUniformLocation(sProgram, U_MATRIX);

        sTextureId = TextureHelper.loadTexture(context, R.drawable.earth);
    }

    public Globe() {
        setIdentityM(mModelMatrix, 0);
    }

    public void rotate(float alpha, float x, float y, float z) {
        setRotateM(mTemp, 0, alpha, x, y, z);
        multiplyMM(mTemp, 16, mTemp, 0, mModelMatrix, 0);
        System.arraycopy(mTemp, 16, mModelMatrix, 0, 16);
    }

    @Override
    public void draw(float[] viewProjectionMatrix) {
        glUseProgram(sProgram);

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);

        glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sTextureId);
        glUniform1i(uTextureUnitLocation, 0);

        sPositionBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, 0, sPositionBuffer);


        glEnableVertexAttribArray(aPositionLocation);

        glDrawArrays(GL_TRIANGLES, 0, sPositionBuffer.limit() / POSITION_COMPONENT_COUNT);

        glDisableVertexAttribArray(aPositionLocation);
    }
}
