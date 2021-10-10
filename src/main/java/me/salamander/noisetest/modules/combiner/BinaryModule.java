package me.salamander.noisetest.modules.combiner;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.SerializableNoiseModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;

public class BinaryModule implements GUIModule {
    private BinaryFunctionType functionType;

    protected SerializableNoiseModule inputOne, inputTwo;

    public BinaryModule(BinaryFunctionType type){
        this.functionType = type;
    }

    @Override
    public int numInputs() {
        return 2;
    }

    @Override
    public void setInput(int index, SerializableNoiseModule module) {
        if(index == 0){
            inputOne = module;
        }else if(index == 1){
            inputTwo = module;
        }else{
            throw new IllegalArgumentException("Index '" + index + "'out of bounds for module with two modules");
        }
    }

    @Override
    public SerializableNoiseModule getInput(int index) {
        if(index == 0){
            return inputOne;
        }else if(index == 1){
            return inputTwo;
        }else{
            throw new IllegalArgumentException("Index '" + index + "'out of bounds for module with two modules");
        }
    }

    @Override
    public void setParameter(int index, double value) {
        throw new IllegalArgumentException("Index out of bounds for module with zero parameters");
    }

    @Override
    public double getParameter(int index) {
        throw new IllegalArgumentException("Index out of bounds for module with zero parameters");
    }

    @Override
    public double sample(double x, double y) {
        return functionType.getFunction().applyAsDouble(SerializableNoiseModule.safeSample(inputOne, x, y), SerializableNoiseModule.safeSample(inputTwo, x, y));
    }

    @Override
    public void setSeed(long s) {
        if(inputOne != null){
            inputOne.setSeed(s + 86);
        }

        if(inputTwo != null){
            inputTwo.setSeed(s * 7 - 47264);
        }
    }

    @Override
    public Collection<SerializableNoiseModule> getSources() {
        List<SerializableNoiseModule> sources = new ArrayList<>();
        sources.add(inputOne);
        sources.add(inputTwo);

        return sources;
    }

    @Override
    public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
        functionType = BinaryFunctionType.fromNBT(tag.getString("function"));

        if(tag.containsKey("inputOne")){
            inputOne = sourceLookup.get(tag.getInt("inputOne"));
        }

        if(tag.containsKey("inputTwo")){
            inputTwo = sourceLookup.get(tag.getInt("inputTwo"));
        }
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
        tag.putString("function", functionType.getNbtIdentifier());

        if(inputOne != null){
            tag.putInt("inputOne", indexLookup.get(inputOne));
        }

        if(inputTwo != null){
            tag.putInt("inputTwo", indexLookup.get(inputTwo));
        }
    }

    @Override
    public String getNodeRegistryName() {
        return "BinaryModule";
    }

    public BinaryFunctionType getFunctionType() {
        return functionType;
    }
}
