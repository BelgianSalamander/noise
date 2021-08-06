package me.salamander.noisetest.modules.modifier;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.noise.Vec2;
import me.salamander.noisetest.noise.VoronoiSampler;

public class Voronoi implements GUIModule {
	private NoiseModule source;
	private double size = 0.3;
	private int voronoiSeed = 69420;
	private double relaxation = 0.0;

	@Override
	public int numInputs() {
		return 1;
	}

	@Override
	public void setInput(int index, NoiseModule module) {
		if (index > 0) {
			throw new IllegalArgumentException("Index out of bounds: " + index); // yes java automatically converts the int to a string
		}

		this.source = module;
	}

	@Override
	public void setParameter(int index, double value) {
		switch (index){
		case 0:
			this.size = value;
			break;
		case 1:
			this.relaxation = value;
			break;
		default:
			throw new IllegalArgumentException("Index out of bounds: " + index);
		}
	}

	@Override
	public double getParameter(int index) {
		switch (index){
		case 0:
			return this.size;
		case 1:
			return this.relaxation;
		default:
			throw new IllegalArgumentException("Index out of bounds: " + index);
		}
	}

	@Override
	public double sample(double x, double y) {
		Vec2 location = VoronoiSampler.sampleVoronoi(x / this.size, y / this.size, this.voronoiSeed, this.relaxation);
		return this.source == null ? 0 : this.source.sample(location.getX() * this.size, location.getY() * this.size);
	}

	@Override
	public void setSeed(long s) {
		this.voronoiSeed = VoronoiSampler.seedFromLong(s);
	}
}
