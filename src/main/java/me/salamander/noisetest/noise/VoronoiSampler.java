/*
 * Naturverbunden
 * Copyright (C) 2021 Valoeghese
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package me.salamander.noisetest.noise;

/**
 * Voronoi calculations taken from older code I've written. Changed to use doubles instead of floats.
 */
public final class VoronoiSampler {
	public static Vec2 sampleVoronoiGrid(int x, int y, int seed) {
		double vx = x + randomdouble(x, y, seed);
		double vy = y + randomdouble(x, y, seed + 1);
		return new Vec2(vx, vy);
	}

	public static int seedFromLong(long seed) {
		return (int) (seed & 0xFFFFFFFF);
	}

	private static Vec2 sampleRelaxedVoronoi(double x, double y, int seed) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		double rx = 0;
		double ry = 0;
		double rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				// ensure more evenly distributed
				double vx = gridX + (randomdouble(gridX, gridY, seed) + 0.5) * 0.5;
				double vy = gridY + (randomdouble(gridX, gridY, seed + 1) + 0.5) * 0.5;
				double vdist = squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Vec2(rx, ry);
	}

	public static Vec2 sampleVoronoi(double x, double y, int seed, boolean relaxed) {
		if (relaxed) {
			return sampleRelaxedVoronoi(x, y, seed);
		} else {
			final int baseX = (int) Math.floor(x);
			final int baseY = (int) Math.floor(y);
			double rx = 0;
			double ry = 0;
			double rdist = 1000;

			for (int xo = -1; xo <= 1; ++xo) {
				int gridX = baseX + xo;

				for (int yo = -1; yo <= 1; ++yo) {
					int gridY = baseY + yo;

					double vx = gridX + randomdouble(gridX, gridY, seed);
					double vy = gridY + randomdouble(gridX, gridY, seed + 1);
					double vdist = squaredDist(x, y, vx, vy);

					if (vdist < rdist) {
						rx = vx;
						ry = vy;
						rdist = vdist;
					}
				}
			}

			return new Vec2(rx, ry);
		}
	}

	public static double sampleD1D2SquaredWorley(double x, double y, int seed) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		double rdist2 = 1000;
		double rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				double vx = gridX + randomdouble(gridX, gridY, seed);
				double vy = gridY + randomdouble(gridX, gridY, seed + 1);
				double vdist = squaredDist(x, y, vx, vy);

				if (vdist < rdist) {
					rdist2 = rdist;
					rdist = vdist;
				} else if (vdist < rdist2) {
					rdist2 = vdist;
				}
			}
		}

		return rdist2 - rdist;
	}

	//public static void main(String[] args) {
	//	System.out.println(sampleD1D2Worley(0, 1, 5));
	//}

	public static double sampleEvenD1SquaredWorley(double x, double y, int seed) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		double dist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				// ensure more evenly distributed
				double vx = gridX + (randomdouble(gridX, gridY, seed) + 0.5) * 0.5;
				double vy = gridY + (randomdouble(gridX, gridY, seed + 1) + 0.5) * 0.5;
				double vdist = squaredDist(x, y, vx, vy);

				if (vdist < dist) {
					dist = vdist;
				}
			}
		}

		return dist;
	}

	public static double sampleD1SquaredWorley(double x, double y, int seed) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		double dist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				double vx = gridX + randomdouble(gridX, gridY, seed);
				double vy = gridY + randomdouble(gridX, gridY, seed + 1);
				double vdist = squaredDist(x, y, vx, vy);

				if (vdist < dist) {
					dist = vdist;
				}
			}
		}

		return dist;
	}

	public static Vec2 sampleManhattanVoronoi(double x, double y, int seed) {
		final int baseX = (int) Math.floor(x);
		final int baseY = (int) Math.floor(y);
		double rx = 0;
		double ry = 0;
		double rdist = 1000;

		for (int xo = -1; xo <= 1; ++xo) {
			int gridX = baseX + xo;

			for (int yo = -1; yo <= 1; ++yo) {
				int gridY = baseY + yo;

				double vx = gridX + randomdouble(gridX, gridY, seed);
				double vy = gridY + randomdouble(gridX, gridY, seed + 1);
				double vdist = manhattanDist(x, y, vx, vy);

				if (vdist < rdist) {
					rx = vx;
					ry = vy;
					rdist = vdist;
				}
			}
		}

		return new Vec2(rx, ry);
	}

	public static int random(int x, int y, int seed, int mask) {
		seed *= 375462423 * seed + 672456235;
		seed += x;
		seed *= 375462423 * seed + 672456235;
		seed += y;

		return seed & mask;
	}

	private static double squaredDist(double x0, double y0, double x1, double y1) {
		double dx = x1 - x0;
		double dy = y1 - y0;
		return dx * dx + dy * dy;
	}

	private static double manhattanDist(double x0, double y0, double x1, double y1) {
		double dx = Math.abs(x1 - x0);
		double dy = Math.abs(y1 - y0);
		return dx + dy;
	}

	public static double randomdouble(int x, int y, int seed) {
		return (double) random(x, y, seed, 0xFFFF) / (double) 0xFFFF;
	}
}