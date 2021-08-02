#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec4 vcolor;

uniform mat4 u_mvpMatrix;

out vec4 color;

void main(){
    gl_Position = u_mvpMatrix * vec4(position, 1.0);
    color = vcolor;
}