package me.salamander.ourea.gui.modules;

import me.salamander.ourea.gui.modules.parameter.DropDownParameter;
import me.salamander.ourea.gui.modules.parameter.Parameter;
import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.modules.modifier.BinaryModule;

import java.util.function.Supplier;

public class GUINoiseModuleType<T extends NoiseSampler> {
    private final Supplier<T> samplerCreator;
    private final Input<? super T>[] inputs;
    private final Parameter<? super T, ?>[] parameters;

    private final String name;

    @SafeVarargs
    public GUINoiseModuleType(String name, Supplier<T> samplerCreator, Input<? super T>[] inputs, Parameter<? super T, ?>... parameters) {
        this.name = name;
        this.samplerCreator = samplerCreator;
        this.inputs = inputs;
        this.parameters = parameters;
    }

    public GUINoiseModule createInstance(){
        return new GUINoiseModule(this, samplerCreator.get());
    }

    public Input<? super T>[] getInputs() {
        return inputs;
    }

    public String getName() {
        return name;
    }

    public Parameter<? super T, ?>[] getParameters() {
        return parameters;
    }
}
