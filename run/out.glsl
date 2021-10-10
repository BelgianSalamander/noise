#version 450 core

uniform uint baseSeed;
uniform vec2 startPos;
uniform float step;

uniform layout(binding = 0, r32f) image2D heightmapOut;

layout(local_size_x = 32, local_size_y = 32) in;
float lerp(float f0, float f1, float t);
float samplePerlin(vec2 pos, uint seed, float frequency, float persistence, float lacunarity, int numOctaves);
float smoothstep(float t);
float lerp(float f00, float f01, float f10, float f11, float tx, float ty);
vec2 getVec(vec2 pos, uint seed);
float samplePerlinOctave(vec2 pos, uint seed);


float lerp(float f0, float f1, float t){
    return (1 - t) * f0 + t * f1;
}

float samplePerlin(vec2 pos, uint seed, float frequency, float persistence, float lacunarity, int numOctaves){
    float total = 0;
    float actualPersistence = 1;
    float actualFrequency = frequency;

    for(int i = 0; i < numOctaves; i++){
        total += actualPersistence * samplePerlinOctave(pos * actualFrequency, seed);
        seed *= 4287264211;
        actualPersistence *= persistence;
        actualFrequency *= lacunarity;
    }

    return total;
}

float smoothstep(float t){
    return t * t * t * (t * (t * 6 - 15) + 10);
}

float lerp(float f00, float f01, float f10, float f11, float tx, float ty){
    return lerp(lerp(f00, f10, tx), lerp(f01, f11, tx), ty);
}

//Gradient list
vec2 gradients[24] = vec2[24](
  vec2( 0.130526192220052,  0.99144486137381),
  vec2( 0.38268343236509,   0.923879532511287),
  vec2( 0.608761429008721,  0.793353340291235),
  vec2( 0.793353340291235,  0.608761429008721),
  vec2( 0.923879532511287,  0.38268343236509),
  vec2( 0.99144486137381,   0.130526192220051),
  vec2( 0.99144486137381,  -0.130526192220051),
  vec2( 0.923879532511287, -0.38268343236509),
  vec2( 0.793353340291235, -0.60876142900872),
  vec2( 0.608761429008721, -0.793353340291235),
  vec2( 0.38268343236509,  -0.923879532511287),
  vec2( 0.130526192220052, -0.99144486137381),
  vec2(-0.130526192220052, -0.99144486137381),
  vec2(-0.38268343236509,  -0.923879532511287),
  vec2(-0.608761429008721, -0.793353340291235),
  vec2(-0.793353340291235, -0.608761429008721),
  vec2(-0.923879532511287, -0.38268343236509),
  vec2(-0.99144486137381,  -0.130526192220052),
  vec2(-0.99144486137381,   0.130526192220051),
  vec2(-0.923879532511287,  0.38268343236509),
  vec2(-0.793353340291235,  0.608761429008721),
  vec2(-0.608761429008721,  0.793353340291235),
  vec2(-0.38268343236509,   0.923879532511287),
  vec2(-0.130526192220052,  0.99144486137381)
);

vec2 getVec(vec2 pos, uint seed){
    seed ^= floatBitsToUint(pos.x) * 3456093529;
    seed ^= floatBitsToUint(pos.y) * 2190470101;

    return gradients[seed % 24];
}

//Code Begin
float samplePerlinOctave(vec2 pos, uint seed){
    float lowX = floor(pos.x);
    float lowY = floor(pos.y);

    float highX = lowX + 1;
    float highY = lowY + 1;

    vec2 cornerOneOffset = vec2(lowX - pos.x, lowY - pos.y);
    vec2 cornerTwoOffset = vec2(lowX - pos.x, highY - pos.y);
    vec2 cornerThreeOffset = vec2(highX - pos.x, lowY - pos.y);
    vec2 cornerFourOffset = vec2(highX - pos.x, highY - pos.y);

    return lerp(
        dot(getVec(vec2(lowX, lowY), seed), cornerOneOffset),
        dot(getVec(vec2(lowX, highY), seed), cornerTwoOffset),
        dot(getVec(vec2(highX, lowY), seed), cornerThreeOffset),
        dot(getVec(vec2(highX, highY), seed), cornerFourOffset),
        smoothstep(pos.x - lowX),
        smoothstep(pos.y - lowY)
    );
}

void main(){
	imageStore(heightmapOut, ivec2(gl_GlobalInvocationID.xy), vec4(samplePerlin(gl_GlobalInvocationID.xy * step + startPos, baseSeed, 1.0, 0.5, 2.0, 6)));
}