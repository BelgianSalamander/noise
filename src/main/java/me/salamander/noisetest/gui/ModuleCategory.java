package me.salamander.noisetest.gui;

public enum ModuleCategory {
    SOURCE("Source"),
    MODIFIER("Modifier"),
    COMBINER("Combiner");

    private final String name;

    ModuleCategory(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
