package me.salamander.noisetest.modules.types;

import io.github.antiquitymc.nbt.CompoundTag;
import io.github.antiquitymc.nbt.IntArrayTag;
import io.github.antiquitymc.nbt.LongArrayTag;
import me.salamander.noisetest.modules.SerializableNoiseModule;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;

public abstract class ArraySourceModule extends SourceModule{
    protected final float[] parameters;

    public ArraySourceModule(int numParameters){
        parameters = new float[numParameters];
    }

    @Override
    public void setParameter(int index, float value) {
        parameters[index] = value;
    }

    @Override
    public float getParameter(int index) {
        return parameters[index];
    }


	@Override
	public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
    	int[] data = ((IntArrayTag) tag.get("parameters")).getValue();

		for (int i = 0; i < data.length; i++) {
			parameters[i] = Float.intBitsToFloat(data[i]);
		}
	}

	@Override
	public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
    	int[] data = new int[parameters.length];

		for (int i = 0; i < parameters.length; i++) {
			data[i] = Float.floatToIntBits(parameters[i]);
		}

		tag.put("parameters", new IntArrayTag(data));
	}
}
