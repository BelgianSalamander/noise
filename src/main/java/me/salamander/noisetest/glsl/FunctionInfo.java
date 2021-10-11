package me.salamander.noisetest.glsl;

import java.util.Set;

public interface FunctionInfo {
    /**
     * Get the name of this method
     * @return The name of this method
     */
    String name();

    /**
     * Generates a GLSL definition for this method as a String
     * @return The GLSL definition
     */
    String generateCode();

    /**
     * Similar to {@code generateCode} but returns a <b>declaration</b> instead
     * @return The declaration
     */
    String forwardDeclaration();

    /**
     * The methods that this methods needs to have declared before it for it to compile. This can include this method itself.
     * If two methods are the same, it is required their that they are the SAME object (i.e that methodOne == methodTwo)
     * @return An array of MethodInfo
     */
    Set<FunctionInfo> requiredFunctions();
}
