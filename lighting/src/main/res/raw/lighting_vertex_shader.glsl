uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

attribute vec4 aPosition;

varying vec2 vTextureCoordinates;
varying vec3 vNormal;
varying vec3 vFragPos;

void main() {
    float degree2radian = 3.1415926 / 180.0;

    float x = aPosition[2] * sin(aPosition[1] * degree2radian) * cos(aPosition[0] * degree2radian);
    float y = aPosition[2] * sin(aPosition[1] * degree2radian) * sin(aPosition[0] * degree2radian);
    float z = aPosition[2] * cos(aPosition[1] * degree2radian);

    gl_Position = uProjection * uView * uModel * vec4(x, y, z, 1.0);

    vNormal = vec3(uModel * vec4(x, y, z, 1.0));
    vFragPos = vec3(uModel * vec4(x, y, z, 1.0));

    vTextureCoordinates = vec2(aPosition.x / 360.0, 1.0 - aPosition.y / 180.0);
}