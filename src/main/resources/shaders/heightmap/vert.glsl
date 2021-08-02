#version 450 core

layout(location = 0) in vec2 xzPosition;
layout(location = 1) in float height;
layout(location = 2) in vec3 color;

layout(binding = 0, std140) buffer Debug{
    float data[10];
} debug;

uniform mat4 mvpMatrix;

out vec4 fragColor;

void main(){
    vec4 basePosition = vec4(xzPosition.x, height, xzPosition.y /*is actually z*/, 1.0);
    debug.data[0] = basePosition.x;
    debug.data[1] = basePosition.y;
    debug.data[2] = basePosition.z;
    debug.data[3] = basePosition.w;

    debug.data[4] = 42.0;
    gl_Position = mvpMatrix * basePosition;
    fragColor = vec4(color, 1.0);
}