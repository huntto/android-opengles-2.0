precision mediump float;

uniform sampler2D uTextureUnit;
varying vec2 vTextureCoordinates;
varying vec3 vNormal;
varying vec3 vFragPos;

struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float shininess;
};

struct Light {
    vec3 position;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

uniform Material uMaterial;
uniform Light uLight;

uniform vec3 uViewPos;

void main() {
    vec4 objectColor = texture2D(uTextureUnit, vTextureCoordinates);

    // 环境光
    vec3 ambient  = uLight.ambient * uMaterial.ambient;

    // 漫反射
    vec3 norm = normalize(vNormal);
    vec3 lightDir = normalize(uLight.position - vFragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse  = uLight.diffuse * (diff * uMaterial.diffuse);

    // 镜面反射
    float specularStrength = 0.5;
    vec3 viewDir = normalize(uViewPos - vFragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), uMaterial.shininess);
    vec3 specular = uLight.specular * (spec * uMaterial.specular);

    vec3 result = (specular + ambient + diffuse) * objectColor.rgb;
    gl_FragColor = vec4(result, 1.0);
}
