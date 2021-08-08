package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.noise.OpenSimplexNoise;
import me.salamander.noisetest.noise.PerlinNoise2D;
import me.salamander.noisetest.noise.SimplexNoise2D;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongFunction;

public enum NoiseType implements LongFunction<DoubleBinaryOperator> {
	PERLIN("Perlin", l -> new PerlinNoise2D(l)::sample),
	BILLOW("Billow", l -> new PerlinNoise2D.Billow(l)::sample),
	SIMPLEX("Simplex", l -> new SimplexNoise2D(l)::sample),
	OPEN_SIMPLEX("OpenSimplex", l -> new OpenSimplexNoise(l)::noise2);

	private final String name;
	private final LongFunction<DoubleBinaryOperator> noiseFnConstructor;

	NoiseType(String name, LongFunction<DoubleBinaryOperator> noiseFnConstructor) {
		this.name = name;
		this.noiseFnConstructor = noiseFnConstructor;
		Data.NOISE_TYPE_MAP.put(this.name, this);
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

	// Java compiler hax to get around enum restrictions
	private static class Data {
		private static final Map<String, NoiseType> NOISE_TYPE_MAP = new HashMap<>();
	}
}
