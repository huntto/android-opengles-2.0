uniform mat4 uMatrix;

attribute vec4 aPosition;
attribute vec3 aTextureCoordinates;

varying vec3 vTextureCoordinates;

void main() {
    vTextureCoordinates = aTextureCoordinates;
    gl_Position = uMatrix * aPosition;
}
