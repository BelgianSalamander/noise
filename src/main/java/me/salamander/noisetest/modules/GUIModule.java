package me.salamander.noisetest.modules;

//If this is implemented. There should be support for inputs being null
public interface GUIModule extends SerializableNoiseModule {
    int numInputs();
    void setInput(int index, SerializableNoiseModule module);
    SerializableNoiseModule getInput(int index);

    void setParameter(int index, double value);
    double getParameter(int index);
}
