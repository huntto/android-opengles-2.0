uniform mat4 uMatrix;

attribute vec4 aPosition;
attribute vec2 aPointSize;

void main() {
    gl_Position = uMatrix * aPosition;
    gl_PointSize = aPointSize.x;
}
