#version 450 core

layout(location = 0) in vec2 xzPosition;
layout(location = 1) in float height;
layout(location = 2) in vec3 color;
layout(location = 3) in vec3 normal;

uniform mat4 mvpMatrix;

out vec4 fragColor;
out vec3 fragNormal;

void main(){
    gl_Position = mvpMatrix * vec4(xzPosition.x, height, xzPosition.y, 1.0);
    fragColor = vec4(color, 1.0);
    fragNormal = normal;
}