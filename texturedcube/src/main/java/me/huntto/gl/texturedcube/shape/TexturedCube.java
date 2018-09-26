package me.huntto.gl.texturedcube.shape;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;
import me.huntto.gl.common.util.TextureHelper;
import me.huntto.gl.texturedcube.R;

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

public class TexturedCube implements Shape {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static int sProgram;

    private static final String A_POSITION = "aPosition";
    private static final String A_TEXTURE_COORDINATES = "aTextureCoordinates";
    private static final String U_MVP_MATRIX = "uMatrix";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";

    private static int uMVPMatrixLocation;
    private static int aTextureCoordinatesLocation;
    private static int aPositionLocation;
    private static int uTextureUnitLocation;
    private static int sTextureId;

    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private static FloatBuffer sVertexBuffer;

    public static void init(Context context) {
        // right hand
        final float vertices[] = {
                // front
                -0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
                -0.5f, 0.5f, -0.5f, 1.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

                -0.5f, 0.5f, -0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

                // back
                0.5f, -0.5f, 0.5f, 1.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 1.0f,

                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 1.0f,

                // right
                -0.5f, -0.5f, 0.5f, 1.0f, 1.0f,
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

                // left
                0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 0.0f, 1.0f,

                0.5f, 0.5f, -0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 0.0f, 1.0f,

                // bottom
                -0.5f, -0.5f, 0.5f, 1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 0.0f, 1.0f,

                -0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 0.0f, 1.0f,

                // top
                -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 1.0f,

                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
        };

        sVertexBuffer = ByteBuffer
                .allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        sVertexBuffer.put(vertices);


        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.texture_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.texture_fragment_shader);
        sProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        glUseProgram(sProgram);

        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(sProgram, A_TEXTURE_COORDINATES);
        uMVPMatrixLocation = glGetUniformLocation(sProgram, U_MVP_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(sProgram, U_TEXTURE_UNIT);

        sTextureId = TextureHelper.loadTexture(context, R.drawable.lena);
    }

    public TexturedCube() {
        setIdentityM(mModelMatrix, 0);
    }

    @Override
    public void draw(float[] viewProjectionMatrix) {
        glUseProgram(sProgram);

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);

        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mMVPMatrix, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sTextureId);
        glUniform1i(uTextureUnitLocation, 0);

        sVertexBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, sVertexBuffer);

        sVertexBuffer.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(aTextureCoordinatesLocation, COORDINATES_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, sVertexBuffer);

        glEnableVertexAttribArray(aTextureCoordinatesLocation);
        glEnableVertexAttribArray(aPositionLocation);

        glDrawArrays(GL_TRIANGLES, 0, 36);

        glDisableVertexAttribArray(aTextureCoordinatesLocation);
        glDisableVertexAttribArray(aPositionLocation);
    }
}
