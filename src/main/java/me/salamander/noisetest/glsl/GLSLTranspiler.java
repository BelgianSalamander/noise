package me.salamander.noisetest.glsl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class GLSLTranspiler {
    public static String compileModule(GLSLCompilable compilable){
        Set<FunctionInfo> requiredFunctions = new HashSet<>();

        Set<FunctionInfo> newFunctions = new HashSet<>(compilable.requiredFunctions());
        Set<FunctionInfo> newerFunctions = new HashSet<>();

        while(newFunctions.size() > 0){
            for(FunctionInfo functionInfo: newFunctions){
                newerFunctions.addAll(functionInfo.requiredFunctions());
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
        code.append("\tfloat value = ").append(compilable.glslExpression("(gl_GlobalInvocationID.xy * step + startPos)", "baseSeed")).append(";\n");
        code.append(TAIL);
        code.append("}");

        return code.toString();
    }

    private static final String HEADER = """
            #version 450 core
                       
            uniform int baseSeed;
            uniform uint width;
            uniform vec2 startPos;
            uniform float step;
                       
            struct DataPoint{
                vec3 normal;
                vec3 color;
                float height;
            };
                       
            layout(std430, binding = 3) buffer HeightData {
              DataPoint data[];
            } data;
                       
            layout(local_size_x = 32, local_size_y = 32) in;
            """;

    private static final String TAIL = """
            \tuint index = gl_GlobalInvocationID.y * width + gl_GlobalInvocationID.x;
            \tdata.data[index].height = value;
            """;

    public static String compileModule(GLSLCompilable compilable, String path) {
        Path saveIn = Path.of("/").relativize(Path.of(path));

        String source = compileModule(compilable);

        try{
            if(!Files.exists(saveIn)){
                saveIn.getParent().toFile().mkdirs();
                Files.createFile(saveIn);
            }

            FileOutputStream fout = new FileOutputStream(saveIn.toAbsolutePath().toString());
            fout.write(source.getBytes(StandardCharsets.UTF_8));
            fout.close();

            System.out.println("Saved shader source to " + saveIn.toAbsolutePath());
        }catch (IOException e){
            System.out.println("Couldn't save shader source");
            e.printStackTrace();
        }

        return source;
    }
}
