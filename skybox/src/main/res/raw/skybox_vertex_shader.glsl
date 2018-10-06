uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

attribute vec3 aPosition;

varying vec3 vPosition;

void main() {
    vPosition = aPosition;
    vPosition.z = - vPosition.z;

    gl_Position = uProjection * uView * uModel * vec4(aPosition, 1.0);
    gl_Position = gl_Position.xyww;
}