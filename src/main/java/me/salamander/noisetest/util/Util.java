package me.salamander.noisetest.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Util {

    public static String loadResource(String path) throws IOException {
        return new String(Util.class.getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
