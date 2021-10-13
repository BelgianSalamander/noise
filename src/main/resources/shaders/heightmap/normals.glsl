#version 450 core

uniform uint tileWidth;
uniform uint tileHeight;
uniform uint amountPoints;
uniform float heightScale;

uniform ivec2 offset;

struct DataPoint{ //Base Alignment: 16
    vec3 normal; //Base Alignment: 16, Aligned Offset: 0
    vec3 color; //Base Alignment: 16, Aligned Offset: 16
    float height; //Base Alignment: 0, Aligned Offset: 28
};

struct ColorPoint{ //Base Alignment: 16
    vec3 color;  //Base Alignment: 16, Aligned Offset: 0
    float value; //Base Alignment: 0, Aligned Offset: 12
};

layout(std430, binding = 3) buffer HeightData {
    DataPoint data[];
} data;

uniform bool hasPositiveXData = false;
layout(std430, binding = 4) buffer HeightDataPX {
    DataPoint data[];
} positiveXData;

uniform bool hasNegativeXData = false;
layout(std430, binding = 5) buffer HeightDataNX {
    DataPoint data[];
} negativeXData;

uniform bool hasPositiveYData = false;
layout(std430, binding = 6) buffer HeightDataPY {
    DataPoint data[];
} positiveYData;

uniform bool hasNegativeYData = false;
layout(std430, binding = 7) buffer HeightDataNY {
    DataPoint data[];
} negativeYData;

layout(std430, binding = 2) buffer ColorData {
    ColorPoint points[];
} color;

layout(local_size_x = 32, local_size_y = 32) in;

void main(){
    //Compute Color First
    uint index = (gl_GlobalInvocationID.y + offset.y) * tileWidth + gl_GlobalInvocationID.x + offset.x;
    float height = data.data[index].height;

    //Do a binary search
    uint lowerBound = 0;
    uint upperBound = amountPoints - 1;

    uint k = 0;

    while(upperBound > lowerBound){
        k++;
        if(k > 20) break;
        uint testIndex = (lowerBound + upperBound) / 2u + 1;

        float value = color.points[testIndex].value;

        if(value == height){
            lowerBound = upperBound = testIndex;
        }else if(value < height){
            lowerBound = testIndex;
        }else{
            upperBound = testIndex - 1;
        }
    }

    uint colorIndex = lowerBound;

    vec3 pointColor;
    if(colorIndex == amountPoints - 1){
        pointColor = color.points[colorIndex].color;
    }else{
        ColorPoint lowColor = color.points[colorIndex];
        ColorPoint upperColor = color.points[colorIndex + 1];

        float t = (height - lowColor.value) / (upperColor.value - lowColor.value);

        pointColor = (1 - t) * lowColor.color + t * upperColor.color;
    }


    //data.data[index].color = vec3((height + 1) / 2, (height + 1) / 2, (height + 1) / 2);
    data.data[index].color = pointColor;

    //Compute normals
    vec3 normal = vec3(0, 0, 0);

    vec3 vertexNorth = vec3(0, 0, 0);
    vec3 vertexEast = vec3(0, 0, 0);
    vec3 vertexSouth = vec3(0, 0, 0);
    vec3 vertexWest = vec3(0, 0, 0);

    float heightAtPoint = height * heightScale;

    bool onEdge = false;

    if(gl_GlobalInvocationID.x + offset.x != 0){
        vertexWest = vec3(-1, data.data[index - 1].height * heightScale - heightAtPoint, 0);
    }else if(hasNegativeXData){
        onEdge = true;
        vertexWest = vec3(-1, negativeXData.data[index + tileWidth - 2].height * heightScale - heightAtPoint, 0);
    }

    if(gl_GlobalInvocationID.y + offset.y != 0){
        vertexSouth = vec3(0, data.data[index - tileWidth].height * heightScale - heightAtPoint, -1);
    }else if(hasNegativeYData){
        onEdge = true;
        vertexSouth = vec3(0, negativeYData.data[index + (tileHeight - 2) * tileWidth].height * heightScale - heightAtPoint, -1);
    }

    if(gl_GlobalInvocationID.x + offset.x != tileWidth - 1){
        vertexEast = vec3(1, data.data[index + 1].height * heightScale - heightAtPoint, 0);
    }else if(hasPositiveXData){
        onEdge = true;
        vertexEast = vec3(1, positiveXData.data[index - (tileWidth - 2)].height * heightScale - heightAtPoint, 0);
    }

    if(gl_GlobalInvocationID.y + offset.y != tileHeight - 1){
        vertexNorth = vec3(0, data.data[index + tileWidth].height * heightScale - heightAtPoint, 1);
    }else if(hasPositiveYData){
        onEdge = true;
        vertexNorth = vec3(0, positiveYData.data[index - (tileHeight - 2) * tileWidth].height * heightScale - heightAtPoint, 1);
    }

    normal += cross(vertexNorth, vertexEast);
    normal += cross(vertexEast, vertexSouth);
    normal += cross(vertexSouth, vertexWest);
    normal += cross(vertexWest, vertexNorth);

    vec3 normalizedNormal = normalize(normal);

    if(onEdge){
        int x = int(gl_GlobalInvocationID.x);
        int y = int(gl_GlobalInvocationID.y);

        //data.data[index].color = vec3(1, 0, 1);

        if(x == 0){
            if(hasNegativeXData){
                negativeXData.data[index + tileWidth - 1].normal = normalizedNormal;
                //negativeXData.data[index + tileWidth - 1].color = vec3(1, 0, 1);
            }
        } else if(x == tileWidth - 1){
            if(hasPositiveXData){
                positiveXData.data[index - (tileWidth - 1)].normal = normalizedNormal;
                //positiveXData.data[index - (tileWidth - 1)].color = vec3(1, 0, 1);
            }
        }else if(y == 0){
            if(hasNegativeYData){
                negativeYData.data[index + (tileHeight - 1) * tileWidth].normal = normalizedNormal;
                //negativeYData.data[index + (tileHeight - 1) * tileWidth].color = vec3(1, 0, 1);
            }
        } else if(y == tileHeight - 1){
            if(hasPositiveYData){
                positiveYData.data[index - (tileHeight - 1) * tileWidth].normal = normalizedNormal;
                //positiveYData.data[index - (tileHeight - 1) * tileWidth].color = vec3(1, 0, 1);
            }
        }
    }

    data.data[index].normal = normalizedNormal;
}