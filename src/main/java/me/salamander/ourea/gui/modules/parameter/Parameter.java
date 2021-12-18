package me.salamander.ourea.gui.modules.parameter;

import me.salamander.ourea.modules.NoiseSampler;

import javax.swing.*;

public interface Parameter<SAMPLER_TYPE extends NoiseSampler, PARAMETER_TYPE> {
    String getName();
    PARAMETER_TYPE getValue(SAMPLER_TYPE sampler);
    void setValue(SAMPLER_TYPE sampler, PARAMETER_TYPE value);

    //For use in the GUI
    JComponent getComponent(SAMPLER_TYPE sampler);

    @SuppressWarnings("unchecked")
    default JComponent getComponentRaw(NoiseSampler sampler){
        return getComponent((SAMPLER_TYPE) sampler);
    }
}
