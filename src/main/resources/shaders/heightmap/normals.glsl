#version 450 core

uniform uint tileWidth;
uniform uint tileHeight;

uniform uint amountPoints;

uniform float heightScale;

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

layout(std430, binding = 2) buffer ColorData {
    ColorPoint points[];
} color;

layout(local_size_x = 32, local_size_y = 32) in;

void main(){
    //Compute Color First
    uint index = gl_GlobalInvocationID.y * tileWidth + gl_GlobalInvocationID.x;
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

    if(gl_GlobalInvocationID.x != 0){
        vertexWest = vec3(-1, data.data[index - 1].height * heightScale - heightAtPoint, 0);
    }

    if(gl_GlobalInvocationID.y != 0){
        vertexSouth = vec3(0, data.data[index - tileWidth].height * heightScale - heightAtPoint, -1);
    }

    if(gl_GlobalInvocationID.x != tileWidth - 1){
        vertexEast = vec3(1, data.data[index + 1].height * heightScale - heightAtPoint, 0);
    }

    if(gl_GlobalInvocationID.y != tileHeight - 1){
        vertexNorth = vec3(0, data.data[index + tileWidth].height * heightScale - heightAtPoint, 1);
    }

    normal += cross(vertexNorth, vertexEast);
    normal += cross(vertexEast, vertexSouth);
    normal += cross(vertexSouth, vertexWest);
    normal += cross(vertexWest, vertexNorth);

    data.data[index].normal = normalize(normal);
}