uniform mat4 uMatrix;

attribute vec4 aPosition;

varying vec2 vTextureCoordinates;

void main() {
    float degree2radian = 3.1415926 / 180.0;

    float x = aPosition[2] * sin(aPosition[1] * degree2radian) * cos(aPosition[0] * degree2radian);
    float y = aPosition[2] * sin(aPosition[1] * degree2radian) * sin(aPosition[0] * degree2radian);
    float z = aPosition[2] * cos(aPosition[1] * degree2radian);

    gl_Position = uMatrix * vec4(x, y, z, 1.0);

    vTextureCoordinates = vec2(aPosition.x / 360.0, 1.0 - aPosition.y / 180.0);
}