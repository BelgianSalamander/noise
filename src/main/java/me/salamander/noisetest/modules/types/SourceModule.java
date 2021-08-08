package me.salamander.noisetest.modules.types;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

public abstract class SourceModule implements GUIModule {
    @Override
    public final int numInputs() {
        return 0;
    }

    @Override
    public final void setInput(int index, NoiseModule module) {
        throw new IllegalArgumentException("Index out of range for input (Source modules don't have any inputs)");
    }
}