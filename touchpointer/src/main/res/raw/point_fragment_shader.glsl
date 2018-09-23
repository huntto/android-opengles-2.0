precision mediump float;

uniform vec4 uColor;

void main() {
    if (length(gl_PointCoord - vec2(0.5)) > 0.5) {
        discard;
    }
    gl_FragColor = uColor;
}