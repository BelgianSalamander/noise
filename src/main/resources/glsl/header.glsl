#version 450

uniform int baseSeed;
uniform uint width = 256;
uniform vec2 startPos;
uniform ivec2 offset;
uniform float step;

struct DataPoint{
    vec4 color;
    vec3 normal;
    float height;
};

layout(std430, binding = 3) buffer HeightData {
    DataPoint data[];
} data;

layout(local_size_x = 32, local_size_y = 32) in;

vec2 grad2[] = {
    vec2(0.6087614, 0.7933533),
    vec2(0.13052619, 0.9914449),
    vec2(0.6087614, -0.7933533),
    vec2(0.13052619, -0.9914449),
    vec2(-0.13052619, -0.9914449),
    vec2(-0.6087614, -0.7933533),
    vec2(-0.6087614, 0.7933533),
    vec2(-0.13052619, 0.9914449),
    vec2(0.7933533, 0.6087614),
    vec2(0.7933533, -0.6087614),
    vec2(-0.7933533, -0.6087614),
    vec2(-0.7933533, 0.6087614),
    vec2(0.38268346, 0.92387956),
    vec2(0.38268346, -0.92387956),
    vec2(-0.38268346, -0.92387956),
    vec2(-0.38268346, 0.92387956),
    vec2(0.9914449, 0.13052619),
    vec2(0.9914449, -0.13052619),
    vec2(-0.9914449, -0.13052619),
    vec2(-0.9914449, 0.13052619),
    vec2(0.92387956, 0.38268346),
    vec2(0.92387956, -0.38268346),
    vec2(-0.92387956, -0.38268346),
    vec2(-0.92387956, 0.38268346)
};

vec3 grad3[] = {
    vec3(-0.6738873, -0.6738873, -0.30290547),
    vec3(-0.6738873, -0.6738873, 0.30290547),
    vec3(-0.6738873, 0.6738873, -0.30290547),
    vec3(-0.6738873, 0.6738873, 0.30290547),
    vec3(0.6738873, -0.6738873, -0.30290547),
    vec3(0.6738873, -0.6738873, 0.30290547),
    vec3(0.6738873, 0.6738873, -0.30290547),
    vec3(0.6738873, 0.6738873, 0.30290547),
    vec3(0.0, -0.934847, -0.35505104),
    vec3(0.0, -0.934847, 0.35505104),
    vec3(0.0, 0.934847, -0.35505104),
    vec3(0.0, 0.934847, 0.35505104),
    vec3(-0.934847, 0.0, -0.35505104),
    vec3(-0.934847, 0.0, 0.35505104),
    vec3(0.934847, 0.0, -0.35505104),
    vec3(0.934847, 0.0, 0.35505104),
    vec3(-0.934847, -0.35505104, 0.0),
    vec3(-0.934847, 0.35505104, 0.0),
    vec3(0.934847, -0.35505104, 0.0),
    vec3(0.934847, 0.35505104, 0.0),
    vec3(0.0, -0.35505104, -0.934847),
    vec3(0.0, -0.35505104, 0.934847),
    vec3(0.0, 0.35505104, -0.934847),
    vec3(0.0, 0.35505104, 0.934847),
    vec3(-0.35505104, 0.0, -0.934847),
    vec3(-0.35505104, 0.0, 0.934847),
    vec3(0.35505104, 0.0, -0.934847),
    vec3(0.35505104, 0.0, 0.934847),
    vec3(-0.6738873, -0.30290547, -0.6738873),
    vec3(-0.6738873, 0.30290547, -0.6738873),
    vec3(-0.6738873, -0.30290547, 0.6738873),
    vec3(-0.6738873, 0.30290547, 0.6738873),
    vec3(0.6738873, -0.30290547, -0.6738873),
    vec3(0.6738873, 0.30290547, -0.6738873),
    vec3(0.6738873, -0.30290547, 0.6738873),
    vec3(0.6738873, 0.30290547, 0.6738873),
    vec3(-0.35505104, -0.934847, 0.0),
    vec3(-0.35505104, 0.934847, 0.0),
    vec3(0.35505104, -0.934847, 0.0),
    vec3(0.35505104, 0.934847, 0.0),
    vec3(0.30290547, -0.6738873, -0.6738873),
    vec3(-0.30290547, -0.6738873, -0.6738873),
    vec3(-0.30290547, -0.6738873, 0.6738873),
    vec3(0.30290547, -0.6738873, 0.6738873),
    vec3(-0.30290547, 0.6738873, -0.6738873),
    vec3(0.30290547, 0.6738873, -0.6738873),
    vec3(-0.30290547, 0.6738873, 0.6738873),
    vec3(0.30290547, 0.6738873, 0.6738873)
};

int modi(int a, int b){
    return (a % b + b) % b;
}

int floori(float f){
    int res = int(f);
    return res <= f ? res : res - 1;
}

int hash(int x, int y, int seed){
    seed ^= x * 1748247483;
    seed ^= y * 848274837;
    seed ^= seed >> 13;
    seed *= 16807;
    seed ^= seed >> 7;
    seed *= 16807;
    seed ^= seed >> 11;
    seed *= 16807;
    seed ^= seed >> 15;
    return seed;
}

int hash(int x, int y, int z, int seed){
    int h = hash(x, y, seed);
    return hash(h, z, seed + 3);
}

vec2 getGradient(int x, int y, int seed){
    return grad2[modi(hash(x, y, seed), 24)];
}

vec3 getGradient(int x, int y, int z, int seed){
    return grad3[modi(hash(x, y, seed), 48)];
}

float lerp(float v00, float v01, float v10, float v11, float tx, float ty) {
    return mix(mix(v00, v01, tx), mix(v10, v11, tx), ty);
}

float lerp(float v000, float v001, float v010, float v011, float v100, float v101, float v110, float v111, float tx, float ty, float tz) {
    return lerp(mix(v000, v001, tx), mix(v010, v011, tx), mix(v100, v101, tx), mix(v110, v111, tx), ty, tz);
}

float smoothstep(float t){
    return t * t * t * (t * (t * 6 - 15) + 10);
}
