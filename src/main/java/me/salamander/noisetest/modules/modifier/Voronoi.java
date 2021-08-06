package me.salamander.noisetest.modules.modifier;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.types.ModifierModule;
import me.salamander.noisetest.noise.Vec2;
import me.salamander.noisetest.noise.VoronoiSampler;

import java.util.Random;

public class Voronoi extends ModifierModule {
	private NoiseModule source;
	private double size = 0.3;
	private int voronoiSeed;
	private double relaxation = 0.0;

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
		Vec2 location = VoronoiSampler.sampleVoronoi(x / this.size, y / this.size, this.voronoiSeed, this.relaxation);
		return this.source == null ? 0 : this.source.sample(location.getX() * this.size, location.getY() * this.size);
	}

	@Override
	public void setSeed(long s) {
		this.voronoiSeed = VoronoiSampler.seedFromLong(s);
		source.setSeed(s * 7 ^ 42545);
	}
}