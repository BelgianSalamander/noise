package me.salamander.noisetest.modules.types;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

import java.util.function.DoubleBinaryOperator;

public class BinaryModule implements GUIModule {
    private final DoubleBinaryOperator operator;

    protected NoiseModule inputOne, inputTwo;

    public BinaryModule(DoubleBinaryOperator operator){
        this.operator = operator;
    }

    @Override
    public int numInputs() {
        return 2;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        if(index == 0){
            inputOne = module;
        }else if(index == 1){
            inputTwo = module;
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
        return operator.applyAsDouble(NoiseModule.safeSample(inputOne, x, y), NoiseModule.safeSample(inputTwo, x, y));
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
}
