package me.salamander.noisetest.modules.types;

import io.github.antiquitymc.nbt.CompoundTag;
import io.github.antiquitymc.nbt.LongArrayTag;

import java.util.Arrays;

public abstract class ArraySourceModule extends SourceModule{
    protected final double[] parameters;

    public ArraySourceModule(int numParameters){
        parameters = new double[numParameters];
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
	public void writeNBT(CompoundTag tag) {
    	long[] data = Arrays.stream(this.parameters).mapToLong(Double::doubleToLongBits).toArray();
		tag.put("parameters", new LongArrayTag(data));
	}

	@Override
	public void readNBT(CompoundTag tag) {
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
