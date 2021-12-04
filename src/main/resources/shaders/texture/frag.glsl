#version 450 core

uniform sampler2D tex;
uniform vec3 lightDirection = normalize(vec3(1.0, 1.0, 1.0));
uniform float ambient = 0.5;

in vec2 uv;
in vec3 fragNormal;

out vec4 outColor;

void main(){
    outColor = texture(tex, uv);

    vec3 norm = normalize(fragNormal);
    float diffuse = max(dot(norm, lightDirection), 0.0);
    outColor.rgb *= (diffuse + ambient);
}