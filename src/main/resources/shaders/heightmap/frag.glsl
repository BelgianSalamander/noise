#version 450 core

in vec4 fragColor;
in vec3 fragNormal;
in vec3 pos;

layout(location = 0) out vec4 color;

uniform vec3 lightDirection = normalize(vec3(1.0, 2.0, 1.0));
float ambient = 0.1;

uniform bool doDiffuse = false;

void main(){
    if(doDiffuse){
        float diffuse = max(0.0, dot(normalize(fragNormal), lightDirection)) + ambient;
        color = vec4(diffuse * fragColor.rgb, fragColor.a);
    }else{
        color = fragColor;
    }
}