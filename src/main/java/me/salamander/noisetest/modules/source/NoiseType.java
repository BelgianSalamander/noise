package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.FunctionRegistry;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.glsl.NotCompilableException;
import me.salamander.noisetest.noise.OpenSimplexNoise;
import me.salamander.noisetest.noise.PerlinNoise2D;
import me.salamander.noisetest.noise.SimplexNoise2D;
import me.salamander.noisetest.util.TriFunction;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongFunction;

public enum NoiseType implements LongFunction<DoubleBinaryOperator> {
	PERLIN("Perlin", l -> new PerlinNoise2D(l)::sample, "samplePerlin"),
	BILLOW("Billow", l -> new PerlinNoise2D.Billow(l)::sample, "sampleBillow"),
	SIMPLEX("Simplex", l -> new SimplexNoise2D(l)::sample, "sampleSimplex"),
	OPEN_SIMPLEX("OpenSimplex", l -> new OpenSimplexNoise(l)::noise2, "sampleOpenSimplex");

	private final String name;
	private final LongFunction<DoubleBinaryOperator> noiseFnConstructor;
	private final TriFunction<String, String, double[], String> callMaker;
	private final FunctionInfo[] functions;

	NoiseType(String name, LongFunction<DoubleBinaryOperator> noiseFnConstructor) {
		this.name = name;
		this.noiseFnConstructor = noiseFnConstructor;
		Data.NOISE_TYPE_MAP.put(this.name, this);
		callMaker = null;
		this.functions = new FunctionInfo[0];
	}

	NoiseType(String name, LongFunction<DoubleBinaryOperator> noiseFnConstructor, TriFunction<String, String, double[], String> callMaker, FunctionInfo[] functions) {
		this.name = name;
		this.noiseFnConstructor = noiseFnConstructor;
		Data.NOISE_TYPE_MAP.put(this.name, this);
		this.callMaker = callMaker;
		this.functions = functions;
	}

	NoiseType(String name, LongFunction<DoubleBinaryOperator> noiseFnConstructor, String function){
		this(name, noiseFnConstructor, (vec2Name, seedName, parameters) -> function + "(" + vec2Name + ", " + seedName + ", " + parameters[1] + ", " + parameters[2] + ", " + parameters[3] + ", " + ((int) parameters[0]) + ")", new FunctionInfo[]{FunctionRegistry.getFunction(function)});
	}

	@Override
	public DoubleBinaryOperator apply(long value) {
		return this.noiseFnConstructor.apply(value);
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static NoiseType fromString(String name) {
		return Data.NOISE_TYPE_MAP.get(name);
	}

	public String glslCall(String vec2Name, String seedName, double[] params){
		if(callMaker == null){
			throw new NotCompilableException();
		}else{
			return callMaker.apply(vec2Name, seedName, params);
		}
	}

	public Set<FunctionInfo> required(){
		return new HashSet<>(Arrays.asList(functions));
	}

	// Java compiler hax to get around enum restrictions
	private static class Data {
		private static final Map<String, NoiseType> NOISE_TYPE_MAP = new HashMap<>();
	}
}
