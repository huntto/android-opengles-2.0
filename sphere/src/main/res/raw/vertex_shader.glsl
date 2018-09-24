uniform mat4 uMatrix;

attribute vec4 aPosition;

void main() {
    gl_Position = uMatrix * aPosition;
}
