package me.huntto.gl.texture;

import android.content.Context;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;
import me.huntto.gl.common.util.TextureHelper;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;

public class TextureRenderer implements GLSurfaceView.Renderer {
    private static final boolean D = BuildConfig.DEBUG;

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COORDINATES_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;

    private static final String U_MATRIX = "uMatrix";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";

    private static final String A_POSITION = "aPosition";
    private static final String A_TEXTURE_COORDINATES = "aTextureCoordinates";

    private final FloatBuffer mVertexData;
    private final FloatBuffer mTextureCoordinatesData;
    private Context mContext;

    private int mProgram;

    private int uMatrixLocation;
    private int uTextureUnitLocation;

    private int aPositionLocation;
    private int aTextureCoordinatesLocation;

    private int mTextureId;

    private final float[] mProjectionMatrix = new float[16];

    public TextureRenderer(Context context) {
        final float[] vertices = {
                -1.0f, -1.0f,   // left bottom
                -1.0f, 1.0f,    // left top
                1.0f, -1.0f,    // right bottom
                1.0f, 1.0f      // right top
        };

        final float[] textureCoordinates = {
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        mVertexData = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(vertices);

        mTextureCoordinatesData = ByteBuffer.allocateDirect(textureCoordinates.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureCoordinatesData.put(textureCoordinates);

        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        String vertexShaderSource = TextResourceReader.readTextFromResource(mContext, R.raw.texture_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(mContext, R.raw.texture_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (D) {
            ShaderHelper.validateProgram(mProgram);
        }
        glUseProgram(mProgram);

        uMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(mProgram, U_TEXTURE_UNIT);

        aPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(mProgram, A_TEXTURE_COORDINATES);

        mVertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(aPositionLocation);

        mTextureCoordinatesData.position(0);
        glVertexAttribPointer(aTextureCoordinatesLocation, COORDINATES_COMPONENT_COUNT, GL_FLOAT,
                false, 0, mTextureCoordinatesData);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);

        mTextureId = TextureHelper.loadTexture(mContext, R.drawable.lena);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);

        final float aspectRatio = width > height
                ? (float) width / (float) height
                : (float) height / (float) width;

        if (width > height) {
            orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            orthoM(mProjectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        glUniformMatrix4fv(uMatrixLocation, 1, false, mProjectionMatrix, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        glUniform1i(uTextureUnitLocation, 0);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }
}
