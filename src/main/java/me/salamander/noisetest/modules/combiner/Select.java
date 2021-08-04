package me.salamander.noisetest.modules.combiner;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.source.Const;

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

    public Select getVisualizer(double lowValue, double highValue){
        return new Select(new Const(lowValue), new Const(highValue), selector, edgeFalloff, threshold);
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

    @Override
    public void setSeed(long s) {
        if(noiseMapOne != null) noiseMapOne.setSeed(s);
        if(noiseMapTwo != null) noiseMapTwo.setSeed(s);
        if(selector != null) selector.setSeed(s);
    }
}
