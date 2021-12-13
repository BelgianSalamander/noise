#version 450 core

uniform float ambient = 0.5f;
uniform vec3 lightDirection = normalize(vec3(1.0, 1.0, 1.0));

in vec4 fragColor;
in vec3 fragNormal;

out vec4 outColor;

void main(){
    outColor = fragColor;

    vec3 norm = normalize(fragNormal);
    float diffuse = max(dot(norm, lightDirection), 0.0);
    outColor.rgb *= (diffuse + ambient);
}