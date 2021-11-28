package me.salamander.ourea.color;

import me.salamander.ourea.util.SortedList;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.nio.FloatBuffer;

public class ColorGradient {
    private SortedList<ColorEntry> entries = new SortedList<>(ColorEntry::compareTo);

    public ColorGradient() {

    }

    public void put(float position, Color color){
        ColorEntry entry = new ColorEntry(position, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
        entries.add(entry);
    }

    public void putInto(float value, FloatBuffer vb, boolean includeAlpha, boolean interpolate) {
        if(entries.size() == 0){
            vb.put(0.5f);
            vb.put(0.5f);
            vb.put(0.5f);
        }else if(entries.size() == 1){
            ColorEntry entry = entries.get(0);
            vb.put(entry.r);
            vb.put(entry.g);
            vb.put(entry.b);
        }else {
            int index = entries.getPlacementIndexOf((v) -> Float.compare(v.position, value));
            if(index == 0){
                ColorEntry entry = entries.get(0);
                vb.put(entry.r);
                vb.put(entry.g);
                vb.put(entry.b);
            }else if(index == entries.size()){
                ColorEntry entry = entries.get(entries.size() - 1);
                vb.put(entry.r);
                vb.put(entry.g);
                vb.put(entry.b);
            }else{
                ColorEntry entry = entries.get(index - 1);
                if(interpolate) {
                    ColorEntry entry2 = entries.get(index);
                    float ratio = (value - entry.position) / (entry2.position - entry.position);
                    vb.put(entry.r + (entry2.r - entry.r) * ratio);
                    vb.put(entry.g + (entry2.g - entry.g) * ratio);
                    vb.put(entry.b + (entry2.b - entry.b) * ratio);
                }else{
                    vb.put(entry.r);
                    vb.put(entry.g);
                    vb.put(entry.b);
                }
            }
        }

        if(includeAlpha) vb.put(1.0f);
    }

    private static class ColorEntry implements Comparable<ColorEntry> {
        final float position;
        final float r, g, b;

        ColorEntry(float position, float r, float g, float b) {
            this.position = position;
            this.r = r;
            this.g = g;
            this.b = b;
        }


        @Override
        public int compareTo(@NotNull ColorGradient.ColorEntry o) {
            return Float.compare(position, o.position);
        }
    }
}
