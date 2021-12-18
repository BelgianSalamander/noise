#version 450 core

uniform uint tileWidth;
uniform uint tileHeight;
uniform uint amountPoints;
uniform ivec2 offset;

uniform bool debug = true;

struct DataPoint{ //Base Alignment: 16
    vec4 color;
    vec3 normal;
    float height;
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
    data.data[index].color = vec4(pointColor, 1.0);

    uint x_coord = gl_GlobalInvocationID.x + offset.x;
    uint y_coord = gl_GlobalInvocationID.y + offset.y;

    float nx, px, ny, py; //Negative X, Positive X, Negative Y, Positive Y
    float mult_x = 0.5;
    float mult_y = 0.5;

    bool onEdge = false;

    if(x_coord > 0){
        nx = data.data[index - 1].height;
    }else if(hasNegativeXData){
        nx = negativeXData.data[index + tileHeight - 2].height;
        onEdge = true;
    }else{
        nx = data.data[index].height;
        mult_x = 1;
    }

    if(x_coord < tileWidth - 1){
        px = data.data[index + 1].height;
    }else if(hasPositiveXData){
        px = positiveXData.data[index - tileHeight + 2].height;
        onEdge = true;
    }else{
        px = data.data[index].height;
        mult_y = 1;
    }

    if(y_coord > 0){
        ny = data.data[index - tileWidth].height;
    }else if(hasNegativeYData){
        ny = negativeYData.data[index + tileWidth * (tileHeight - 2)].height;
        onEdge = true;
    }else{
        ny = data.data[index].height;
        mult_x = 1;
    }

    if(y_coord < tileHeight - 1){
        py = data.data[index + tileWidth].height;
    }else if(hasPositiveYData){
        py = positiveYData.data[index - tileWidth * (tileHeight - 2)].height;
        onEdge = true;
    }else{
        py = data.data[index].height;
        mult_y = 1;
    }

    float dydx = (px - nx) * mult_x;
    float dzdx = (py - ny) * mult_y;

    vec3 normal = normalize(vec3(dydx, 1, dzdx));

    if(onEdge){
        if(x_coord == 0){
            if(hasNegativeXData){
                negativeXData.data[index + tileWidth - 1].normal = normal;
                if(debug)
                    negativeXData.data[index + tileWidth - 1].color = vec4(1.0, 0.0, 0.0, 1.0);
            }
        } else if(x_coord == tileWidth - 1){
            if(hasPositiveXData){
                positiveXData.data[index - (tileWidth - 1)].normal = normal;
                if(debug)
                    positiveXData.data[index - (tileWidth - 1)].color = vec4(1.0, 0.0, 0.0, 1.0);;
            }
        }else if(y_coord == 0){
            if(hasNegativeYData){
                negativeYData.data[index + (tileHeight - 1) * tileWidth].normal = normal;
                if(debug)
                    negativeYData.data[index + (tileHeight - 1) * tileWidth].color = vec4(1.0, 0.0, 0.0, 1.0);
            }
        } else if(y_coord == tileHeight - 1){
            if(hasPositiveYData){
                positiveYData.data[index - (tileHeight - 1) * tileWidth].normal = normal;
                if(debug)
                    positiveYData.data[index - (tileHeight - 1) * tileWidth].color = vec4(1.0, 0.0, 0.0, 1.0);
            }
        }

        if(debug)
            data.data[index].color = vec4(1.0, 0.0, 0.0, 1.0);
    }

    data.data[index].normal = normal;
}