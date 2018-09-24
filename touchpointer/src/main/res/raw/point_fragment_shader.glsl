precision mediump float;

uniform vec4 uColor;

void main() {
    float len = length(gl_PointCoord - vec2(0.5f));
    if (len > 0.5) {
        discard;
    }
    gl_FragColor = vec4(uColor.xyz, 1.0f - pow(len * 2.0f, 5.0f));
}