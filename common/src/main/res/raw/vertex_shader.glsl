uniform mat4 uMVPMatrix;

attribute vec4 aColor;
attribute vec4 aPosition;

varying vec4 vColor;

void main() {
    vColor = aColor;
    gl_Position = uMVPMatrix * aPosition;
}
