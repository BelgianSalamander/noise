package me.salamander.noisetest.modules.types;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.SerializableNoiseModule;

import java.util.ArrayList;
import java.util.Collection;

public abstract class SourceModule implements GUIModule {
    @Override
    public final int numInputs() {
        return 0;
    }

    @Override
    public final void setInput(int index, SerializableNoiseModule module) {
        throw new IllegalArgumentException("Index out of range for input (Source modules don't have any inputs)");
    }

    @Override
    public SerializableNoiseModule getInput(int index) {
        throw new IllegalArgumentException("Index out of range for input (Source modules don't have any inputs)");
    }

    @Override
    public final Collection<SerializableNoiseModule> getSources() {
        return new ArrayList<>();
    }
}
