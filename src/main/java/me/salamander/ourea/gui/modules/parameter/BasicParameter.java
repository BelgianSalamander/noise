package me.salamander.ourea.gui.modules.parameter;

import me.salamander.ourea.modules.NoiseSampler;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class BasicParameter<T extends NoiseSampler, U> implements Parameter<T, U> {
    private final String name;
    private final Function<T, U> getter;
    private final BiConsumer<T, U> setter;

    public BasicParameter(String name, Function<T, U> getter, BiConsumer<T, U> setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public U getValue(T sampler) {
        return getter.apply(sampler);
    }

    @Override
    public void setValue(T sampler, U value) {
        setter.accept(sampler, value);
    }
}
