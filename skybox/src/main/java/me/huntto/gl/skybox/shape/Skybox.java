package me.huntto.gl.skybox.shape;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;
import me.huntto.gl.common.util.TextureHelper;
import me.huntto.gl.skybox.R;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
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

public class Skybox implements Shape {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 3;

    private static final String U_MODEL = "uModel";
    private static final String U_VIEW = "uView";
    private static final String U_PROJECTION = "uProjection";
    private static final String A_POSITION = "aPosition";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";

    private static int sProgram;

    private static int uModelLocation;
    private static int uViewLocation;
    private static int uProjectionLocation;
    private static int aPositionLocation;
    private static int uTextureUnitLocation;

    private final float[] mModelMatrix = new float[16];
    private final float[] mTemp = new float[32];

    private static FloatBuffer sVertexBuffer;
    private static ByteBuffer sIndexBuffer;

    private static int sTextureId;

    public static void init(Context context) {
        // Right hand
        final float[] vertices = new float[]{
                -1, 1, 1,   // Left-top-near
                1, 1, 1,    // Right-top-near
                -1, -1, 1,  // Left-bottom-near
                1, -1, 1,   // Right-bottom-near

                -1, 1, -1,   // Left-top-far
                1, 1, -1,    // Right-top-far
                -1, -1, -1,  // Left-bottom-far
                1, -1, -1,   // Right-bottom-far
        };

        final byte[] indices = new byte[]{
                // Front
                1, 3, 0,
                0, 3, 2,

                // Back
                4, 6, 5,
                5, 6, 7,

                // Left
                0, 2, 4,
                4, 2, 6,

                // Right
                5, 7, 1,
                1, 7, 3,

                // Top
                5, 1, 4,
                4, 1, 0,

                // Bottom
                6, 2, 7,
                7, 2, 3
        };

        sVertexBuffer = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        sVertexBuffer.put(vertices, 0, vertices.length);

        sIndexBuffer = ByteBuffer.allocate(indices.length)
                .order(ByteOrder.nativeOrder());
        sIndexBuffer.put(indices, 0, indices.length);

        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.skybox_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.skybox_fragment_shader);
        sProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        glUseProgram(sProgram);

        uModelLocation = glGetUniformLocation(sProgram, U_MODEL);
        uViewLocation = glGetUniformLocation(sProgram, U_VIEW);
        uProjectionLocation = glGetUniformLocation(sProgram, U_PROJECTION);
        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
        uTextureUnitLocation = glGetUniformLocation(sProgram, U_TEXTURE_UNIT);

        sTextureId = TextureHelper.loadCubeMap(context, new int[]{
                R.drawable.left, R.drawable.right,
                R.drawable.bottom, R.drawable.top,
                R.drawable.front, R.drawable.back
        });
    }

    public Skybox() {
        setIdentityM(mModelMatrix, 0);
    }

    public void rotate(float alpha, float x, float y, float z) {
        if (x == 0 && y == 0 && z == 0) {
            return;
        }
        setRotateM(mTemp, 0, alpha, x, y, z);
        multiplyMM(mTemp, 16, mTemp, 0, mModelMatrix, 0);
        System.arraycopy(mTemp, 16, mModelMatrix, 0, 16);
    }

    @Override
    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        glUseProgram(sProgram);

        glUniformMatrix4fv(uModelLocation, 1, false, mModelMatrix, 0);
        glUniformMatrix4fv(uViewLocation, 1, false, viewMatrix, 0);
        glUniformMatrix4fv(uProjectionLocation, 1, false, projectionMatrix, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, sTextureId);
        glUniform1i(uTextureUnitLocation, 0);

        sVertexBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, 0, sVertexBuffer);


        glEnableVertexAttribArray(aPositionLocation);

        sIndexBuffer.position(0);
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, sIndexBuffer);

        glDisableVertexAttribArray(aPositionLocation);
    }
}
