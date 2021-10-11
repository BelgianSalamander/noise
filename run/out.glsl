#version 450 core

uniform int baseSeed;
uniform uint width;
uniform vec2 startPos;
uniform float step;

struct DataPoint{
    vec3 normal;
    vec3 color;
    float height;
};

layout(std430, binding = 3) buffer HeightData {
  DataPoint data[];
} data;

layout(local_size_x = 32, local_size_y = 32) in;
float lerp(float f0, float f1, float t);
float samplePerlin(vec2 pos, int seed, float frequency, float persistence, float lacunarity, int numOctaves);
vec2 getVec(vec2 pos, int seed);
float samplePerlinOctave(vec2 pos, int seed);
float lerp(float f00, float f01, float f10, float f11, float tx, float ty);
float smoothstep(float t);


float lerp(float f0, float f1, float t){
    return (1 - t) * f0 + t * f1;
}

float samplePerlin(vec2 pos, int seed, float frequency, float persistence, float lacunarity, int numOctaves){
    float total = 0;
    float actualPersistence = 1;
    float actualFrequency = frequency;

    for(int i = 0; i < numOctaves; i++){
        total += actualPersistence * samplePerlinOctave(pos * actualFrequency, seed);
        seed *= 122609317;
        actualPersistence *= persistence;
        actualFrequency *= lacunarity;
    }

    return total;
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

vec2 getVec(vec2 pos, int seed){
    int seedCopy = seed;
   seedCopy ^= floatBitsToInt(pos.x) * 1316110667;
   seedCopy ^= floatBitsToInt(pos.y) * 1213414981;

   return gradients[abs(seedCopy) % 24];
}

//Code Begin
float samplePerlinOctave(vec2 pos, int seed){
    int lowX = int(floor(pos.x));
    int lowY = int(floor(pos.y));

    int highX = lowX + 1;
    int highY = lowY + 1;

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

float lerp(float f00, float f01, float f10, float f11, float tx, float ty){
    return lerp(lerp(f00, f10, tx), lerp(f01, f11, tx), ty);
}

float smoothstep(float t){
    return t * t * t * (t * (t * 6 - 15) + 10);
}

void main(){
	float value = samplePerlin((gl_GlobalInvocationID.xy * step + startPos), baseSeed, 1.0, 0.5, 2.0, 6);
	uint index = gl_GlobalInvocationID.y * width + gl_GlobalInvocationID.x;
	data.data[index].height = value;
}