package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.noise.OpenSimplexNoise;

import java.util.Random;

public class OpenSimplex extends Simplex {
	public OpenSimplex() {
		this(6);
	}

	public OpenSimplex(int octaves){
		this(octaves, new Random().nextLong());
	}

	public OpenSimplex(int octaves, long seed){
		super(octaves, seed, l -> new OpenSimplexNoise(l)::noise2);
	}
}
