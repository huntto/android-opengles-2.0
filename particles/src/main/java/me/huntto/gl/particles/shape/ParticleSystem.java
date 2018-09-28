package me.huntto.gl.particles.shape;

import android.content.Context;
import android.graphics.Color;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.huntto.gl.common.util.ShaderHelper;
import me.huntto.gl.common.util.TextResourceReader;
import me.huntto.gl.particles.R;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;

public class ParticleSystem implements Shape {
    private static final int BYTES_PER_FLOAT = 4;

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int DIRECTION_VECTOR_COMPONENT_COUNT = 3;
    private static final int PARTICLE_START_TIME_COMPONENT_COUNT = 1;

    private static final int TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT
            + COLOR_COMPONENT_COUNT
            + DIRECTION_VECTOR_COMPONENT_COUNT
            + PARTICLE_START_TIME_COMPONENT_COUNT;
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;

    private static int sProgram;

    private static final String A_POSITION = "aPosition";
    private static final String A_COLOR = "aColor";
    private static final String A_DIRECTION_VECTOR = "aDirectionVector";
    private static final String A_PARTICLE_START_TIME = "aParticleStartTime";
    private static final String U_MATRIX = "uMatrix";
    private static final String U_TIME = "uTime";

    private static int aPositionLocation;
    private static int aColorLocation;
    private static int aDirectionVectorLocation;
    private static int aParticleStartTimeLocation;
    private static int uMatrixLocation;
    private static int uTimeLocation;


    private final float[] mModelMatrix = new float[16];
    private final float[] mMatrix = new float[16];

    private final Object PARTICLES_SYNC = new Object();

    private final float[] mParticles;

    private int mCurrentParticleCount;
    private int mNextParticle;
    private final int mMaxParticleCount;
    private FloatBuffer mVertexBuffer;
    private static long sGlobeStartTime;

    public static void init(Context context) {

        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.particle_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.particle_fragment_shader);
        sProgram = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        glUseProgram(sProgram);

        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
        aColorLocation = glGetAttribLocation(sProgram, A_COLOR);
        aDirectionVectorLocation = glGetAttribLocation(sProgram, A_DIRECTION_VECTOR);
        aParticleStartTimeLocation = glGetAttribLocation(sProgram, A_PARTICLE_START_TIME);
        uMatrixLocation = glGetUniformLocation(sProgram, U_MATRIX);
        uTimeLocation = glGetUniformLocation(sProgram, U_TIME);


        sGlobeStartTime = System.currentTimeMillis();
    }

    public ParticleSystem(int maxParticleCount) {
        mMaxParticleCount = maxParticleCount;
        mParticles = new float[maxParticleCount * TOTAL_COMPONENT_COUNT];
        mNextParticle = 0;
        setIdentityM(mModelMatrix, 0);

        mVertexBuffer = ByteBuffer.allocateDirect(mParticles.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.position(0);
    }

    @Override
    public void draw(float[] viewProjectionMatrix) {
        glUseProgram(sProgram);

        multiplyMM(mMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);

        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);

        glUniform1f(uTimeLocation, (System.currentTimeMillis() - sGlobeStartTime) / 1000.0f);

        synchronized (PARTICLES_SYNC) {
            int dataOffset = 0;
            mVertexBuffer.position(dataOffset);
            glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                    GL_FLOAT, false, STRIDE, mVertexBuffer);

            dataOffset += POSITION_COMPONENT_COUNT;
            mVertexBuffer.position(dataOffset);
            glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT,
                    GL_FLOAT, false, STRIDE, mVertexBuffer);

            dataOffset += COLOR_COMPONENT_COUNT;
            mVertexBuffer.position(dataOffset);
            glVertexAttribPointer(aDirectionVectorLocation, DIRECTION_VECTOR_COMPONENT_COUNT,
                    GL_FLOAT, false, STRIDE, mVertexBuffer);

            dataOffset += DIRECTION_VECTOR_COMPONENT_COUNT;
            mVertexBuffer.position(dataOffset);
            glVertexAttribPointer(aParticleStartTimeLocation, PARTICLE_START_TIME_COMPONENT_COUNT,
                    GL_FLOAT, false, STRIDE, mVertexBuffer);

            glEnableVertexAttribArray(aPositionLocation);
            glEnableVertexAttribArray(aColorLocation);
            glEnableVertexAttribArray(aDirectionVectorLocation);
            glEnableVertexAttribArray(aParticleStartTimeLocation);

            glDrawArrays(GL_POINTS, 0, mCurrentParticleCount);
        }
    }

    public void addParticle(float x, float y, int color, long particleStartTime) {
        float startTime = (particleStartTime - sGlobeStartTime) / 1000.0f;
        final int particleOffset = mNextParticle * TOTAL_COMPONENT_COUNT;

        int currentOffset = particleOffset;
        mNextParticle++;

        synchronized (PARTICLES_SYNC) {
            if (mCurrentParticleCount < mMaxParticleCount) {
                mCurrentParticleCount++;
            }
        }

        if (mNextParticle == mMaxParticleCount) {
            mNextParticle = 0;
        }

        mParticles[currentOffset++] = x;
        mParticles[currentOffset++] = y;
        mParticles[currentOffset++] = 0;

        mParticles[currentOffset++] = Color.red(color) / 255f;
        mParticles[currentOffset++] = Color.green(color) / 255f;
        mParticles[currentOffset++] = Color.blue(color) / 255f;

        mParticles[currentOffset++] = 0;
        mParticles[currentOffset++] = -1f;
        mParticles[currentOffset++] = 0;

        mParticles[currentOffset] = startTime;

        synchronized (PARTICLES_SYNC) {
            mVertexBuffer.position(particleOffset);
            mVertexBuffer.put(mParticles, particleOffset, TOTAL_COMPONENT_COUNT);
            mVertexBuffer.position(0);
        }
    }

}
