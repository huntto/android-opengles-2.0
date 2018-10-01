package me.huntto.gl.lighting.shape;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;
import me.huntto.gl.common.util.TextureHelper;
import me.huntto.gl.lighting.R;

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
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setRotateM;

public class Sphere implements Shape {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 3;

    private static final int ANGLE_STEP = 5;
    private static final float RADIUS = 0.5f;

    private static int sProgram;

    private static final String A_POSITION = "aPosition";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";
    private static final String U_MODEL = "uModel";
    private static final String U_VIEW = "uView";
    private static final String U_PROJECTION = "uProjection";
    private static final String U_VIEW_POS = "uViewPos";

    private static final String U_MATERIAL_AMBIENT = "uMaterial.ambient";
    private static final String U_MATERIAL_DIFFUSE = "uMaterial.diffuse";
    private static final String U_MATERIAL_SPECULAR = "uMaterial.specular";
    private static final String U_MATERIAL_SHININESS = "uMaterial.shininess";

    private static final String U_LIGHT_AMBIENT = "uLight.ambient";
    private static final String U_LIGHT_DIFFUSE = "uLight.diffuse";
    private static final String U_LIGHT_SPECULAR = "uLight.specular";
    private static final String U_LIGHT_POSITION = "uLight.position";

    private static int uModelLocation;
    private static int uViewLocation;
    private static int uProjectionLocation;
    private static int uTextureUnitLocation;
    private static int aPositionLocation;
    private static int uViewPosLocation;

    private static int uMaterialAmbientLocation;
    private static int uMaterialDiffuseLocation;
    private static int uMaterialSpecularLocation;
    private static int uMaterialShininessLocation;

    private static int uLightAmbientLocation;
    private static int uLightDiffuseLocation;
    private static int uLightSpecularLocation;
    private static int uLightPositionLocation;

    private final float[] mModelMatrix = new float[16];
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


        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.lighting_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.lighting_fragment_shader);
        sProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        glUseProgram(sProgram);

        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
        uTextureUnitLocation = glGetUniformLocation(sProgram, U_TEXTURE_UNIT);
        uModelLocation = glGetUniformLocation(sProgram, U_MODEL);
        uViewLocation = glGetUniformLocation(sProgram, U_VIEW);
        uProjectionLocation = glGetUniformLocation(sProgram, U_PROJECTION);
        uViewPosLocation = glGetUniformLocation(sProgram, U_VIEW_POS);

        uMaterialAmbientLocation = glGetUniformLocation(sProgram, U_MATERIAL_AMBIENT);
        uMaterialDiffuseLocation = glGetUniformLocation(sProgram, U_MATERIAL_DIFFUSE);
        uMaterialShininessLocation = glGetUniformLocation(sProgram, U_MATERIAL_SHININESS);
        uMaterialSpecularLocation = glGetUniformLocation(sProgram, U_MATERIAL_SPECULAR);

        uLightAmbientLocation = glGetUniformLocation(sProgram, U_LIGHT_AMBIENT);
        uLightDiffuseLocation = glGetUniformLocation(sProgram, U_LIGHT_DIFFUSE);
        uLightSpecularLocation = glGetUniformLocation(sProgram, U_LIGHT_SPECULAR);
        uLightPositionLocation = glGetUniformLocation(sProgram, U_LIGHT_POSITION);

        sTextureId = TextureHelper.loadTexture(context, R.drawable.board);
    }

    public Sphere() {
        setIdentityM(mModelMatrix, 0);
        scaleM(mModelMatrix, 0, 0.8f, 0.8f, 0.8f);
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

        glUniform3f(uMaterialAmbientLocation, 1.0f, 0.5f, 0.31f);
        glUniform3f(uMaterialDiffuseLocation, 1.0f, 0.5f, 0.31f);
        glUniform3f(uMaterialSpecularLocation, 0.5f, 0.5f, 0.5f);
        glUniform1f(uMaterialShininessLocation, 32.0f);

        glUniform3f(uLightAmbientLocation, 0.2f, 0.2f, 0.2f);
        glUniform3f(uLightDiffuseLocation, 0.5f, 0.5f, 0.5f);
        glUniform3f(uLightSpecularLocation, 1.0f, 1.0f, 1.0f);

        glUniform3f(uLightPositionLocation, 1.0f, 1.0f, -1.0f);

        glUniform3f(uViewPosLocation, 0f, 0f, -2f);

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
