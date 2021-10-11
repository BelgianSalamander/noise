package me.salamander.noisetest.modules.combiner;

import me.salamander.noisetest.glsl.NotCompilableException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;

public enum BinaryFunctionType {
    ADD("Add", (a, b) -> a + b, (first, second) -> first + " + " + second),
    MULTIPLY("Multiply", (a, b) -> a * b, (first, second) -> first + " * " + second),
    MIN("Min", (a, b) -> Math.min(a, b), (first, second) -> "min(" + first + ", " + second + ")"),
    MAX("Max", (a, b) -> Math.max(a, b), (first, second) -> "max(" + first + ", " + second + ")");

    private DoubleBinaryOperator function;
    private String nbtIdentifier;
    private GLSLExpressionGetter expressionGetter;

    private static Map<String, BinaryFunctionType> functionsMap = new HashMap<>();

    BinaryFunctionType(String ID, DoubleBinaryOperator function){
        this.function = function;
        nbtIdentifier = ID;
        expressionGetter = null;
    }

    BinaryFunctionType(String ID, DoubleBinaryOperator function, GLSLExpressionGetter expressionGetter){
        this.function = function;
        this.nbtIdentifier = ID;
        this.expressionGetter = expressionGetter;
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

    public String getGLSLExpression(String first, String second){
        if(expressionGetter == null){
            throw new NotCompilableException(toString() + " is not compilable!");
        }

        return expressionGetter.getExpression(first, second);
    }

    static {
        for(BinaryFunctionType function : BinaryFunctionType.values()){
            functionsMap.put(function.nbtIdentifier, function);
        }
    }

    @FunctionalInterface
    private interface GLSLExpressionGetter{
        String getExpression(String first, String second);
    }
}
