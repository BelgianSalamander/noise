package me.salamander.noisetest.terra.fractal;

import com.fasterxml.jackson.databind.JsonNode;

public class FBM extends FractalModule {
    public FBM(JsonNode data){
        super(data);
    }

    @Override
    public double sample(double x, double y) {
        float total = 0;

        float currentGain = gain;
        float currentFrequency = 1;

        for(int i = 0; i < octaves; i++){
            total += currentGain * function.sample(x * currentFrequency, y * currentFrequency);

            //Scramble x and y
            x += ((getSeed() ^ 4726474728L) * i) & 0xffff - 0x8000;
            y += ((getSeed() ^ 5467757689L) * i) & 0xffff - 0x8000;

            //Change gain and freq
            currentGain *= gain;
            currentFrequency *= lacunarity;
        }

        return total;
    }
}
