package me.salamander.noisetest.modules.combiner;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.Parameter;
import org.lwjgl.system.CallbackI;

public class Select implements NoiseModule {
    private NoiseModule noiseMapOne, noiseMapTwo, selector;
    private double edgeFalloff = 0.0;
    private double threshold = 0;

    public Select(NoiseModule noiseMapOne, NoiseModule noiseMapTwo, NoiseModule selector){
        this.noiseMapOne = noiseMapOne;
        this.noiseMapTwo = noiseMapTwo;
        this.selector = selector;
    }

    public Select(NoiseModule noiseMapOne, NoiseModule noiseMapTwo, NoiseModule selector, double edgeFalloff, double threshold){
        this(noiseMapOne, noiseMapTwo, selector);
        this.edgeFalloff = edgeFalloff;
        this.threshold = threshold;
    }

    @Override
    public double sample(double x, double y) {
        final double selectorValue = selector.sample(x, y);

        if(edgeFalloff > 0.0){
            if(selectorValue < threshold - edgeFalloff){
                return noiseMapOne.sample(x, y);
            }else if(selectorValue < threshold + edgeFalloff){
                return cubicInterpolation(
                        noiseMapOne.sample(x, y),
                        noiseMapTwo.sample(x, y),
                        (selectorValue - threshold + edgeFalloff) / (2 * edgeFalloff)
                );
            }else{
                return noiseMapTwo.sample(x, y);
            }
        }else{
            if(selectorValue < threshold){
                return noiseMapOne.sample(x, y);
            }else{
                return noiseMapTwo.sample(x, y);
            }
        }
    }

    @Override
    public int getNumInputs() {
        return 3;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        switch (index){
            case 0:
                noiseMapOne = module;
                break;
            case 1:
                noiseMapTwo = module;
                break;
            case 2:
                selector = module;
                break;
        }
    }

    public void setNoiseMapOne(NoiseModule noiseMapOne) {
        this.noiseMapOne = noiseMapOne;
    }

    public void setNoiseMapTwo(NoiseModule noiseMapTwo) {
        this.noiseMapTwo = noiseMapTwo;
    }

    public void setSelector(NoiseModule selector) {
        this.selector = selector;
    }

    public void setEdgeFalloff(double edgeFalloff) {
        this.edgeFalloff = edgeFalloff;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    private static double cubicInterpolation(double v0, double v1, double t){
        t = t * t * (3 - 2 * t);
        return v0 * (1 - t) + v1 * t;
    }

    private static final Parameter[] parameters = new Parameter[]{
            new Parameter(0, "Threshold", -1.0, 1.0),
            new Parameter(1, "Edge Falloff", 0.0, 1.0)
    };
    private static final String[] inputNames = new String[]{"Module One", "Module Two", "Selector"};

    @Override
    public void setParameter(int index, double value) {
        switch (index){
            case 0:
                threshold = value;
            case 1:
                edgeFalloff = value;
            default:
                throw new IllegalArgumentException("Index is out of bounds!");
        }
    }

    @Override
    public double getParameter(int index) {
        switch (index){
            case 0:
                return threshold;
            case 1:
                return edgeFalloff;
            default:
                throw new IllegalArgumentException("Index is out of bounds!");
        }
    }

    @Override
    public String getName() {
        return "Select";
    }

    @Override
    public String[] inputNames() {
        return inputNames;
    }

    @Override
    public Parameter[] parameters() {
        return parameters;
    }
}
