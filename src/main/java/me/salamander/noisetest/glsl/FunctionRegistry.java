package me.salamander.noisetest.glsl;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FunctionRegistry {
    private static final Map<String, FunctionInfo> functions = new HashMap<>();

    public static FunctionInfo getFunction(String name){
        return functions.get(name);
    }

    //Load all functions
    static {
        URI functionsURI;
        try {
            functionsURI = FunctionRegistry.class.getResource("/glsl/functions/").toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not load functions", e);
        }

        final String[] parts = functionsURI.toString().split("!");

        try {
            FileSystem fs = FileSystems.newFileSystem(URI.create(parts[0]), new HashMap<>());
            Path functionsDir = fs.getPath(parts[1]);

            Files.list(functionsDir).forEach((path) -> {
                String fileName = path.getFileName().toString();
                if(fileName.endsWith(".func")){
                    InputStream in = FunctionRegistry.class.getResourceAsStream(path.toAbsolutePath().toString());
                    Scanner scanner = new Scanner(in);
                    FunctionInfo function = new BasicFunctionInfo(scanner);
                    scanner.close();

                    String name = fileName.substring(0, fileName.length() - 5);
                    functions.put(name, function);
                    System.out.println("Loaded method " + name);
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not load functions", e);
        }
    }
}
