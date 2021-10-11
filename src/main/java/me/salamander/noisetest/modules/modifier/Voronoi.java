package me.salamander.noisetest.modules.modifier;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.FunctionRegistry;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.glsl.NotCompilableException;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.types.ModifierModule;
import me.salamander.noisetest.noise.Vec2;
import me.salamander.noisetest.noise.VoronoiSampler;

import java.util.*;

public class Voronoi extends ModifierModule implements GLSLCompilable {
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
	public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
		super.readNBT(tag, sourceLookup);

		voronoiSeed = tag.getInt("seed");
	}

	@Override
	public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
		super.writeNBT(tag, indexLookup);

		tag.putInt("seed", voronoiSeed);
	}

	@Override
	public long getSeed() {
		return source != null ? source.getSeed() : 0;
	}

	@Override
	public String glslExpression(String vec2Name, String seedName) {
		if(source instanceof GLSLCompilable compilable){
			return compilable.glslExpression("(sampleVoronoi(" + vec2Name + " * " + (1 / parameters[0]) + ", " + seedName + ", " + parameters[1] + ") * " + parameters[0] + ")","(" + seedName + " - 42816623)");
		}else{
			throw new IllegalStateException("Voronoi source can not be compiled");
		}
	}

	@Override
	public Set<FunctionInfo> requiredFunctions() {
		if(source instanceof GLSLCompilable compilable) {
			Set<FunctionInfo> combined = new HashSet<>(compilable.requiredFunctions());
			combined.addAll(required);
			return combined;
		}else{
			throw new NotCompilableException("Can't compile voronoi source");
		}
	}

	private static final Set<FunctionInfo> required = new HashSet<>();
	static {
		required.add(FunctionRegistry.getFunction("sampleVoronoi"));
	}
}