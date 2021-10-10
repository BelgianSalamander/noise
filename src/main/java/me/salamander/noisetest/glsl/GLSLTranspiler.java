package me.salamander.noisetest.glsl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GLSLTranspiler {
    public static String compileModule(GLSLCompilable compilable){
        Set<FunctionInfo> requiredFunctions = new HashSet<>();

        Set<FunctionInfo> newFunctions = new HashSet<>(Arrays.asList(compilable.requiredMethods()));
        Set<FunctionInfo> newerFunctions = new HashSet<>();

        while(newFunctions.size() > 0){
            for(FunctionInfo functionInfo: newFunctions){
                newerFunctions.addAll(Arrays.asList(functionInfo.requiredFunctions()));
            }

            requiredFunctions.addAll(newFunctions);
            newFunctions.clear();
            newFunctions.addAll(newerFunctions);
            newFunctions.removeAll(requiredFunctions);
            newerFunctions.clear();
        }

        for(FunctionInfo functionInfo: requiredFunctions){
            System.out.println("Requires: " + functionInfo.forwardDeclaration());
        }

        StringBuilder code = new StringBuilder(HEADER);

        //For now, forward declare all functions
        for(FunctionInfo functionInfo: requiredFunctions){
            code.append(functionInfo.forwardDeclaration()).append(";\n");
        }
        code.append("\n\n");

        //Create definitions
        for(FunctionInfo function: requiredFunctions){
            code.append(function.generateCode()).append("\n");
        }

        code.append("void main(){\n");
        code.append("\timageStore(heightmapOut, ivec2(gl_GlobalInvocationID.xy), vec4(").append(compilable.glslExpression("gl_GlobalInvocationID.xy * step + startPos", "baseSeed")).append("));\n}");

        return code.toString();
    }

    private static final String HEADER = """
           #version 450 core
           
           uniform uint baseSeed;
           uniform vec2 startPos;
           uniform float step;
           
           uniform layout(binding = 0, r32f) image2D heightmapOut;
           
           layout(local_size_x = 32, local_size_y = 32) in;
           """;
}
