package me.salamander.noisetest.modules.combiner;

import java.util.Map;
import java.util.function.DoubleBinaryOperator;

public enum BinaryFunctionType {
    ADD("Add", (a, b) -> a + b),
    MULTIPLY("Multiply", (a, b) -> a * b),
    MIN("Min", (a, b) -> Math.min(a, b)),
    MAX("Max", (a, b) -> Math.max(a, b));

    private DoubleBinaryOperator function;
    private String nbtIdentifier;
    private static Map<String, BinaryFunctionType> functionsMap;

    private BinaryFunctionType(String ID, DoubleBinaryOperator function){
        this.function = function;
        nbtIdentifier = ID;
    }

    public static BinaryFunctionType fromNBT(String identifier){
        return functionsMap.get(identifier);
    }

    public DoubleBinaryOperator getFunction() {
        return function;
    }

    public String getNbtIdentifier() {
        return nbtIdentifier;
    }

    static {
        for(BinaryFunctionType function : BinaryFunctionType.values()){
            functionsMap.put(function.nbtIdentifier, function);
        }
    }
}
