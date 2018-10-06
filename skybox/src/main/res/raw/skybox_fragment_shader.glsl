precision mediump float;

uniform samplerCube uTextureUnit;
varying vec3 vPosition;

void main() {
    gl_FragColor = textureCube(uTextureUnit, vPosition);
}
