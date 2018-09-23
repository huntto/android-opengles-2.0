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
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class Triangle implements Shape {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;

    private static int sProgram;

    private static final String U_COLOR = "uColor";
    private static int uColorLocation;

    private static final String A_POSITION = "aPosition";
    private static int aPositionLocation;
    private static FloatBuffer sVertexData;

    public static void init(Context context) {
        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.simple_fragment_shader);

        sProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        float[] vertices = {
                -0.5f, -0.5f,
                0.5f, -0.5f,
                0f, 0.5f
        };

        sVertexData = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        sVertexData.put(vertices);

        glUseProgram(sProgram);

        uColorLocation = glGetUniformLocation(sProgram, U_COLOR);
        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);

    }

    @Override
    public void draw(float[] viewProjectionMatrix) {
        glUseProgram(sProgram);
        glUniform4f(uColorLocation, 1f, 0.75f, 0.0f, 1.0f);

        sVertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, sVertexData);
        glEnableVertexAttribArray(aPositionLocation);

        glDrawArrays(GL_TRIANGLES, 0, 3);

        glDisableVertexAttribArray(aPositionLocation);
    }
}
