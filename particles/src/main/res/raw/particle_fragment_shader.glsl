precision mediump float;

varying vec3 vColor;
varying float vElapsedTime;

void main() {
    gl_FragColor = vec4(vColor , 1.0 / (vElapsedTime * 2.0));
}