#version 450 core

layout(location = 0) in vec2 xzPosition;
layout(location = 1) in float height;
layout(location = 2) in vec3 color;
layout(location = 3) in vec3 normal;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

out vec4 fragColor;
out vec3 fragNormal;

out vec3 pos;

out float diffuseStrength;

void main(){
    vec4 pos = modelViewMatrix * vec4(xzPosition.x, height, xzPosition.y, 1.0);
    gl_Position = projectionMatrix * pos;

    float distance = length(pos);

    if(distance > 300){
        if(distance < 500){
            diffuseStrength = 1 - (distance - 300) / 200;
        }else{
            diffuseStrength = 0;
        }
    }else{
        diffuseStrength = 1;
    }

    fragColor = vec4(color, 1.0);
    fragNormal = normal;

    /*if(distance > 500){
        fragColor *= 1 - ((distance - 500) / 300);
    }*/
}