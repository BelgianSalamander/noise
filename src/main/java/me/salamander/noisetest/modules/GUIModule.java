package me.salamander.noisetest.modules;

//If this is implemented. There should be support for inputs being null
public interface GUIModule extends NoiseModule{
    int numInputs();
    void setInput(int index, NoiseModule module);

    void setParameter(int index, double value);
    double getParameter(int index);
}
