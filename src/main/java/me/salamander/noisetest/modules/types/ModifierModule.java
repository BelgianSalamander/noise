package me.salamander.noisetest.modules.types;

import io.github.antiquitymc.nbt.CompoundTag;
import io.github.antiquitymc.nbt.LongArrayTag;
import me.salamander.noisetest.gui.Modules;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

import java.util.Arrays;

public abstract class ModifierModule implements GUIModule {
    protected NoiseModule source;
    protected final double[] parameters;

    protected ModifierModule(int numParameters){
        parameters = new double[numParameters];
    }

    @Override
    public int numInputs() {
        return 1;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        if(index == 0){
            source = module;
        }else{
            throw new IllegalArgumentException("Index out of bounds for modifier with a single input");
        }
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

    @Override //Should set the seed of the source
    public void setSeed(long s){source.setSeed(s * 46237 ^ 42534);};

	@Override
	public void writeNBT(CompoundTag tag) {
		if (this.source != null) {
			tag.put("source", Modules.nodeToTag(this.source));
		}

		long[] data = Arrays.stream(this.parameters).mapToLong(Double::doubleToLongBits).toArray();
		tag.put("parameters", new LongArrayTag(data));
	}

	@Override
	public void readNBT(CompoundTag tag) {
		if (tag.containsKey("source")) {
			this.source = Modules.tagToNode(tag.getSubTag("source"));
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
