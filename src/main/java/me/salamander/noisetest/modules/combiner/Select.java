package me.salamander.noisetest.modules.combiner;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.source.Const;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;

public class Select implements GUIModule {
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
        final double selectorValue = NoiseModule.safeSample(selector, x, y);

        if(edgeFalloff > 0.0){
            if(selectorValue < threshold - edgeFalloff){
                return NoiseModule.safeSample(noiseMapOne, x, y);
            }else if(selectorValue < threshold + edgeFalloff){
                return cubicInterpolation(
                        NoiseModule.safeSample(noiseMapOne, x, y),
                        NoiseModule.safeSample(noiseMapTwo, x, y),
                        (selectorValue - threshold + edgeFalloff) / (2 * edgeFalloff)
                );
            }else{
                return NoiseModule.safeSample(noiseMapTwo, x, y);
            }
        }else{
            if(selectorValue < threshold){
                return NoiseModule.safeSample(noiseMapOne, x, y);
            }else{
                return NoiseModule.safeSample(noiseMapTwo, x, y);
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


    @Override
    public int numInputs() {
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
            default:
                throw new IllegalArgumentException("Index out of bounds!");
        }
    }

    @Override
    public NoiseModule getInput(int index) {
        switch (index){
            case 0:
                return noiseMapOne;
            case 1:
                return noiseMapTwo;
            case 2:
                return selector;
            default:
                throw new IllegalArgumentException("Index out of bounds!");
        }
    }

    @Override
    public void setParameter(int index, double value) {
        if(index == 0){
            threshold = value;
        }else if(index == 1){
            edgeFalloff = value;
        }else{
            throw new IllegalArgumentException("Index out of bounds!");
        }
    }

    @Override
    public double getParameter(int index) {
        if(index == 0){
            return threshold;
        }else if(index == 1){
            return edgeFalloff;
        }else{
            throw new IllegalArgumentException("Index out of bounds!");
        }
    }

    @Override
    public Collection<NoiseModule> getSources() {
        List<NoiseModule> sources =  new ArrayList<>();
        sources.add(selector);
        sources.add(noiseMapOne);
        sources.add(noiseMapTwo);
        return sources;
    }

    @Override
    public void readNBT(CompoundTag tag, List<NoiseModule> sourceLookup) {
        edgeFalloff = tag.getDouble("edgeFalloff");
        threshold = tag.getDouble("threshold");

        if(tag.containsKey("inputOne")){
            noiseMapOne = sourceLookup.get(tag.getInt("inputOne"));
        }

        if(tag.containsKey("inputTwo")){
            noiseMapTwo = sourceLookup.get(tag.getInt("inputTwo"));
        }

        if(tag.containsKey("selector")){
            selector = sourceLookup.get(tag.getInt("selector"));
        }
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<NoiseModule, Integer> indexLookup) {
        tag.putDouble("edgeFalloff", edgeFalloff);
        tag.putDouble("threshold", threshold);

        if(noiseMapOne != null){
            tag.putInt("inputOne", indexLookup.get(noiseMapOne));
        }

        if(noiseMapTwo != null){
            tag.putInt("inputTwo", indexLookup.get(noiseMapTwo));
        }

        if(selector != null){
            tag.putInt("selector", indexLookup.get(selector));
        }
    }

    @Override
    public String getNodeRegistryName() {
        return "Select";
    }


}
