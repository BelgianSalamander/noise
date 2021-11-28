#version 450 core

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform float heightScale = 1;
uniform float invHeightScale = 1;

layout(location = 0) in vec2 xzPosition;
layout(location = 1) in float height;
layout(location = 2) in vec2 vertexUV;
layout(location = 3) in vec3 normal;

out vec2 uv;
out vec3 fragNormal;

void main(){
    vec4 scaledPosition = vec4(xzPosition.x, height * heightScale, xzPosition.y, 1);
    gl_Position = projectionMatrix * modelViewMatrix * scaledPosition;
    uv = vertexUV;

    fragNormal = normalize(vec3(normal.x, normal.y * invHeightScale, normal.z));
}
