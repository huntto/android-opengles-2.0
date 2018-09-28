precision mediump float;
uniform sampler2D uTextureUnit;

varying vec3 vColor;
varying float vElapsedTime;

void main() {
    gl_FragColor = vec4(vColor , texture2D(uTextureUnit, gl_PointCoord).x  / vElapsedTime);
}