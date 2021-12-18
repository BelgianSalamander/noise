package me.salamander.ourea.gui.modules;

import me.salamander.ourea.gui.modules.parameter.*;
import me.salamander.ourea.modules.FrequencyNoise;
import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.modules.SaltedNoise;
import me.salamander.ourea.modules.modifier.BinaryModule;
import me.salamander.ourea.modules.source.Const;
import me.salamander.ourea.modules.source.PerlinSampler;

//Ignore unchecked assignment warnings
@SuppressWarnings({"unchecked", "rawtypes"})
public class Modules {
    private static final Parameter<FrequencyNoise, Float> FREQUENCY_PARAMETER = new FloatSliderParameter<>("Frequency", FrequencyNoise::getFrequency, FrequencyNoise::setFrequency, 0.1f, 5.0f, 49);
    private static final Parameter<SaltedNoise, Integer> SALT_PARAMETER = new IntegerSelectorParameter<>("Salt", SaltedNoise::getSalt, SaltedNoise::setSalt, Integer.MIN_VALUE, Integer.MAX_VALUE);

    public static GUINoiseModuleType<PerlinSampler> PERLIN = new GUINoiseModuleType<>("Perlin", PerlinSampler::new, new Input[0], FREQUENCY_PARAMETER, SALT_PARAMETER);
    public static GUINoiseModuleType<Const> CONST = new GUINoiseModuleType<>("Const", Const::new, new Input[0], new FloatSliderParameter<>("Value", Const::getValue, Const::setValue, -1.f, 1.f, 200));

    public static GUINoiseModuleType<BinaryModule> BINARY = new GUINoiseModuleType<BinaryModule>(
            "Binary",
            BinaryModule::new,
            new Input[]{
                    new Input<>("A", BinaryModule::getFirst, BinaryModule::setFirst),
                    new Input<>("B", BinaryModule::getSecond, BinaryModule::setSecond)
            },
            new DropDownParameter<>("Operator", BinaryModule::getOperator, BinaryModule::setOperator, BinaryModule.Operator.values())
    );
}
