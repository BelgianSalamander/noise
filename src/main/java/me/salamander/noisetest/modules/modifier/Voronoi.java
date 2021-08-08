package me.salamander.noisetest.modules.modifier;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.types.ModifierModule;
import me.salamander.noisetest.noise.Vec2;
import me.salamander.noisetest.noise.VoronoiSampler;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;

public class Voronoi extends ModifierModule {
	private int voronoiSeed;

	public Voronoi(){
		super(2);
		voronoiSeed = (new Random()).nextInt();
		initParameters();
	}

	public Voronoi(long seed){
		super(2);
		voronoiSeed = VoronoiSampler.seedFromLong(seed);
		initParameters();
	}

	private void initParameters(){
		parameters[0] = 0.3;
		parameters[1] = 0.0;
	}

	@Override
	public double sample(double x, double y) {
		Vec2 location = VoronoiSampler.sampleVoronoi(x / parameters[0], y / parameters[0], this.voronoiSeed, parameters[1]);
		return this.source == null ? 0 : this.source.sample(location.getX() * parameters[0], location.getY() * parameters[0]);
	}

	@Override
	public void setSeed(long s) {
		this.voronoiSeed = VoronoiSampler.seedFromLong(s);
		source.setSeed(s * 7 ^ 42545);
	}

	@Override
	public String getNodeRegistryName() {
		return null;
	}

	@Override
	public void readNBT(CompoundTag tag, List<NoiseModule> sourceLookup) {
		super.readNBT(tag, sourceLookup);

		voronoiSeed = tag.getInt("seed");
	}

	@Override
	public void writeNBT(CompoundTag tag, IdentityHashMap<NoiseModule, Integer> indexLookup) {
		super.writeNBT(tag, indexLookup);

		tag.putInt("seed", voronoiSeed);
	}
}