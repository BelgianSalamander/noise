#version 450 core

uniform int baseSeed;
uniform uint width;
uniform vec2 startPos;
uniform float step;

layout(std430, binding = 3) buffer HeightData {
  vec4 data[];
} heightData;

layout(local_size_x = 32, local_size_y = 32) in;
float sampleRidge(vec2 pos, int seed, float frequency, float persistence, float lacunarity, int numOctaves);
vec2 getVec(vec2 pos, int seed);
float lerp(float f0, float f1, float t);
float smoothstep(float t);
float samplePerlinOctave(vec2 pos, int seed);
vec2 sampleVoronoi(vec2 pos, int seed, float relaxation);
float lerp(float f00, float f01, float f10, float f11, float tx, float ty);


float sampleRidge(vec2 pos, int seed, float frequency, float persistence, float lacunarity, int numOctaves){
    float total = 0;
    float actualPersistence = 1;
    float actualFrequency = frequency;

    float weight = 1.0;

    for(int i = 0; i < numOctaves; i++){
        float signal = samplePerlinOctave(pos * actualFrequency, seed);

        signal = 1 - abs(signal);
        signal *= signal * weight;

        weight = signal * 2.0;

        if(weight > 1.0) weight = 1.0;
        else if(weight < 0.0) weight = 0.0;

        total += signal * actualPersistence;

        seed *= 122609317;
        actualPersistence *= persistence;
        actualFrequency *= lacunarity;
    }

    return total * 1.25 - 1.0;
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

float lerp(float f0, float f1, float t){
    return (1 - t) * f0 + t * f1;
}

float smoothstep(float t){
    return t * t * t * (t * (t * 6 - 15) + 10);
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

vec2 sampleVoronoi(vec2 pos, int seed, float relaxation){
    float unrelaxation = 1.0 - relaxation;

    vec2 base = floor(pos);

    vec2 closest;
    float minimumDistance = 100000.0;

    for(int xo = -1; xo <= 1; xo++){
        float gridX = base.x + xo;

        for(int yo = -1; yo <= 1; yo++){
            float gridY = base.y + yo;

            vec2 gridPos = vec2(gridX, gridY);

            vec2 jitteredPoint = gridPos + (relaxation * 0.5 + unrelaxation * getVec(gridPos, seed));
            float distance = dot(jitteredPoint - pos, jitteredPoint - pos);

            if(distance < minimumDistance){
                minimumDistance = distance;
                closest = jitteredPoint;
            }
        }
    }

    return closest;
}

float lerp(float f00, float f01, float f10, float f11, float tx, float ty){
    return lerp(lerp(f00, f10, tx), lerp(f01, f11, tx), ty);
}

void main(){
	float value = sampleRidge((sampleVoronoi((gl_GlobalInvocationID.xy * step + startPos) * 3.3333333333333335, baseSeed, 0.0) * 0.3), (baseSeed - 42816623), 1.0, 0.5, 2.0, 6);
	uint index = gl_GlobalInvocationID.y * width + gl_GlobalInvocationID.x;
	heightData.data[index] = vec4(value, 1, 1, 1);
}