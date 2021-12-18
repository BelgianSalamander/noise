package me.salamander.ourea.gui.modules;

import me.salamander.ourea.modules.NoiseSampler;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class Input<T extends NoiseSampler> {
    private final String name;
    private final Function<T, NoiseSampler> getter;
    private final BiConsumer<T, NoiseSampler> setter;

    public Input(String name, Function<T, NoiseSampler> getter, BiConsumer<T, NoiseSampler> setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    public String getName(){
        return name;
    }

    public NoiseSampler get(T module) {
        return getter.apply(module);
    }

    public void set(T module, NoiseSampler value) {
        setter.accept(module, value);
    }
}
