package me.salamander.noisetest.modules.types;

import io.github.antiquitymc.nbt.CompoundTag;
import io.github.antiquitymc.nbt.IntArrayTag;
import io.github.antiquitymc.nbt.LongArrayTag;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.SerializableNoiseModule;

import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;

public abstract class ArrayModule implements GUIModule{
    protected final SerializableNoiseModule[] inputs;
    protected final float[] parameters;

    protected final int numInputs, numParameters;

    //Creates the arrays that hold the inputs and parameters
    protected ArrayModule(int numInputs, int numParameters){
        inputs = new SerializableNoiseModule[numInputs];
        parameters = new float[numParameters];

        this.numInputs = numInputs;
        this.numParameters = numParameters;
    }

    @Override
    public int numInputs() {
        return numInputs;
    }

    @Override
    public void setInput(int index, SerializableNoiseModule module) {
        inputs[index] = module;
    }

    @Override
    public SerializableNoiseModule getInput(int index) {
        return inputs[index];
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
    public abstract float sample(float x, float y);

    @Override
    public abstract void setSeed(long s);

	@Override
	public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
		if (this.inputs.length > 0) {
			tag.put("sources", new IntArrayTag(Arrays.stream(inputs).mapToInt(indexLookup::get).toArray()));
		}

		int[] data = new int[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            data[i] = Float.floatToIntBits(parameters[i]);
        }

		tag.put("parameters", new IntArrayTag(data));
	}

	@Override
	public void readNBT(CompoundTag tag, List<SerializableNoiseModule> moduleLookup) {
		if (tag.containsKey("sources")) {
			IntArrayTag sources = (IntArrayTag) tag.get("sources");

			System.arraycopy(
                    Arrays.stream(sources.getValue()).mapToObj(moduleLookup::get),
					0,
					this.inputs,
					0,
					this.inputs.length
			);
		}

		if (tag.containsKey("parameters")) {
		    int[] data = ((IntArrayTag) tag.get("parameters")).getValue();
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = Float.intBitsToFloat(data[i]);
            }
		}
	}

    @Override
    public Collection<SerializableNoiseModule> getSources() {
        return Arrays.asList(inputs);
    }
}
