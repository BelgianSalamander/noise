package me.salamander.noisetest.modules.types;

import io.github.antiquitymc.nbt.CompoundTag;
import io.github.antiquitymc.nbt.ListTag;
import io.github.antiquitymc.nbt.LongArrayTag;
import me.salamander.noisetest.gui.Modules;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class ArrayModule implements GUIModule {
    protected final NoiseModule[] inputs;
    protected final double[] parameters;

    protected final int numInputs, numParameters;

    //Creates the arrays that hold the inputs and parameters
    protected ArrayModule(int numInputs, int numParameters){
        inputs = new NoiseModule[numInputs];
        parameters = new double[numParameters];

        this.numInputs = numInputs;
        this.numParameters = numParameters;
    }

    @Override
    public int numInputs() {
        return numInputs;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        inputs[index] = module;
    }

    @Override
    public void setParameter(int index, double value) {
        parameters[index] = value;
    }

    @Override
    public double getParameter(int index) {
        return parameters[index];
    }

    @Override
    public abstract double sample(double x, double y);

    @Override
    public abstract void setSeed(long s);

	@Override
	public void writeNBT(CompoundTag tag) {
		if (this.inputs.length > 0) {
			tag.put("sources", new ListTag(Arrays.stream(this.inputs).map(o -> Modules.nodeToTag(o)).collect(Collectors.toList())));
		}

		long[] data = Arrays.stream(this.parameters).mapToLong(Double::doubleToLongBits).toArray();
		tag.put("parameters", new LongArrayTag(data));
	}

	@Override
	public void readNBT(CompoundTag tag) {
		if (tag.containsKey("sources")) {
			ListTag sources = (ListTag) tag.get("sources");

			System.arraycopy(
					sources.stream().map(o -> Modules.tagToNode((CompoundTag) o)).toArray(),
					0,
					this.inputs,
					0,
					this.inputs.length
			);
		}

		if (tag.containsKey("parameters")) {
			System.arraycopy(
					Arrays.stream(((LongArrayTag)tag.get("parameters")).getValue()).mapToDouble(Double::longBitsToDouble).toArray(),
					0,
					this.parameters,
					0,
					this.parameters.length
			);
		}
	}
}
