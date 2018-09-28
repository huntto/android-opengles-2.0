uniform mat4 uMatrix;
uniform float uTime;

attribute vec3 aPosition;
attribute vec3 aColor;
attribute vec3 aDirectionVector;
attribute float aParticleStartTime;

varying vec3 vColor;
varying float vElapsedTime;

void main() {
    vColor = aColor;
    vElapsedTime = uTime - aParticleStartTime;
    vec3 currentPosition = aPosition + (aDirectionVector * vElapsedTime * vElapsedTime);
    gl_Position = uMatrix * vec4(currentPosition, 1.0);
    gl_PointSize = 10.0f;
}
