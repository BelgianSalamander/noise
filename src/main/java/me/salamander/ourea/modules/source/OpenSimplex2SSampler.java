package me.salamander.ourea.modules.source;

import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.util.Grad2;
import me.salamander.ourea.util.Grad3;
import me.salamander.ourea.util.MathHelper;

//TODO: Give better credit
/**
 * A lot of this code is taken from K.jpg's OpenSimplex 2, smooth variant ("SuperSimplex")
 */
public class OpenSimplex2SSampler implements NoiseSampler {
    private int salt;
    private float frequency = 1;

    @Override
    public void setSalt(int salt) {
        this.salt = salt;
    }

    @Override
    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public float sample(float x, float y, int seed) {
        seed += salt;
        x *= frequency;
        y *= frequency;

        float s = 0.366025403784439f * (x + y);
        float xs = x + s, ys = y + s;

        float value = 0;

        // Get base points and offsets
        int xsb = MathHelper.floor(xs), ysb = MathHelper.floor(ys);
        float xsi = xs - xsb, ysi = ys - ysb;

        // Index to point list
        int a = (int)(xsi + ysi);
        int index =
                (a << 2) |
                        (int)(xsi - ysi / 2 + 1 - a / 2.0) << 3 |
                        (int)(ysi - xsi / 2 + 1 - a / 2.0) << 4;

        float ssi = (xsi + ysi) * -0.211324865405187f;
        float xi = xsi + ssi, yi = ysi + ssi;

        // Point contributions
        for (int i = 0; i < 4; i++) {
            LatticePoint2D c = LOOKUP_2D[index + i];

            float dx = xi + c.dx, dy = yi + c.dy;
            float attn = 2.0f / 3.0f - dx * dx - dy * dy;
            if (attn <= 0) continue;

            Grad2 grad = MathHelper.getGradient(xsb + c.xsv, ysb + c.ysv, seed);
            float extrapolation = grad.x() * dx * 18.24196194486065f + grad.y() * dy * 18.24196194486065f;

            attn *= attn;
            value += attn * attn * extrapolation;
        }

        return value;
    }

    @Override
    public float sample(float x, float y, float z, int seed) {
        seed += salt;
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float r = (2.0f / 3.0f) * (x + y + z);
        float xr = r - x, yr = r - y, zr = r - z;

        // Get base and offsets inside cube of first lattice.
        int xrb = MathHelper.floor(xr), yrb = MathHelper.floor(yr), zrb = MathHelper.floor(zr);
        float xri = xr - xrb, yri = yr - yrb, zri = zr - zrb;

        // Identify which octant of the cube we're in. This determines which cell
        // in the other cubic lattice we're in, and also narrows down one point on each.
        int xht = (int)(xri + 0.5), yht = (int)(yri + 0.5), zht = (int)(zri + 0.5);
        int index = (xht << 0) | (yht << 1) | (zht << 2);

        // Point contributions
        float value = 0;
        LatticePoint3D c = LOOKUP_3D[index];
        while (c != null) {
            float dxr = xri + c.dxr, dyr = yri + c.dyr, dzr = zri + c.dzr;
            float attn = 0.75f - dxr * dxr - dyr * dyr - dzr * dzr;
            if (attn < 0) {
                c = c.nextOnFailure;
            } else {
                Grad3 grad = MathHelper.getGradient(xrb + c.xrv, yrb + c.yrv, zrb + c.zrv, seed);
                float extrapolation = grad.x() * dxr * 11.8671744262711f + grad.y() * dyr * 11.8671744262711f + grad.z() * dzr + 11.8671744262711f;

                attn *= attn;
                value += attn * attn * extrapolation;
                c = c.nextOnSuccess;
            }
        }
        return value;
    }

    private static class LatticePoint2D {
        int xsv, ysv;
        float dx, dy;
        public LatticePoint2D(int xsv, int ysv) {
            this.xsv = xsv; this.ysv = ysv;
            float ssv = (xsv + ysv) * -0.211324865405187f;
            this.dx = -xsv - ssv;
            this.dy = -ysv - ssv;
        }
    }

    private static class LatticePoint3D {
        public float dxr, dyr, dzr;
        public int xrv, yrv, zrv;
        LatticePoint3D nextOnFailure, nextOnSuccess;
        public LatticePoint3D(int xrv, int yrv, int zrv, int lattice) {
            this.dxr = -xrv + lattice * 0.5f; this.dyr = -yrv + lattice * 0.5f; this.dzr = -zrv + lattice * 0.5f;
            this.xrv = xrv + lattice * 1024; this.yrv = yrv + lattice * 1024; this.zrv = zrv + lattice * 1024;
        }
    }

    private static final LatticePoint2D[] LOOKUP_2D;
    private static final LatticePoint3D[] LOOKUP_3D;
    static {
        LOOKUP_2D = new LatticePoint2D[32];
        LOOKUP_3D = new LatticePoint3D[8];

        for (int i = 0; i < 8; i++) {
            int i1, j1, i2, j2;
            if ((i & 1) == 0) {
                if ((i & 2) == 0) { i1 = -1; j1 = 0; } else { i1 = 1; j1 = 0; }
                if ((i & 4) == 0) { i2 = 0; j2 = -1; } else { i2 = 0; j2 = 1; }
            } else {
                if ((i & 2) != 0) { i1 = 2; j1 = 1; } else { i1 = 0; j1 = 1; }
                if ((i & 4) != 0) { i2 = 1; j2 = 2; } else { i2 = 1; j2 = 0; }
            }
            LOOKUP_2D[i * 4 + 0] = new LatticePoint2D(0, 0);
            LOOKUP_2D[i * 4 + 1] = new LatticePoint2D(1, 1);
            LOOKUP_2D[i * 4 + 2] = new LatticePoint2D(i1, j1);
            LOOKUP_2D[i * 4 + 3] = new LatticePoint2D(i2, j2);
        }

        for (int i = 0; i < 8; i++) {
            int i1, j1, k1, i2, j2, k2;
            i1 = (i >> 0) & 1; j1 = (i >> 1) & 1; k1 = (i >> 2) & 1;
            i2 = i1 ^ 1; j2 = j1 ^ 1; k2 = k1 ^ 1;

            // The two points within this octant, one from each of the two cubic half-lattices.
            LatticePoint3D c0 = new LatticePoint3D(i1, j1, k1, 0);
            LatticePoint3D c1 = new LatticePoint3D(i1 + i2, j1 + j2, k1 + k2, 1);

            // (1, 0, 0) vs (0, 1, 1) away from octant.
            LatticePoint3D c2 = new LatticePoint3D(i1 ^ 1, j1, k1, 0);
            LatticePoint3D c3 = new LatticePoint3D(i1, j1 ^ 1, k1 ^ 1, 0);

            // (1, 0, 0) vs (0, 1, 1) away from octant, on second half-lattice.
            LatticePoint3D c4 = new LatticePoint3D(i1 + (i2 ^ 1), j1 + j2, k1 + k2, 1);
            LatticePoint3D c5 = new LatticePoint3D(i1 + i2, j1 + (j2 ^ 1), k1 + (k2 ^ 1), 1);

            // (0, 1, 0) vs (1, 0, 1) away from octant.
            LatticePoint3D c6 = new LatticePoint3D(i1, j1 ^ 1, k1, 0);
            LatticePoint3D c7 = new LatticePoint3D(i1 ^ 1, j1, k1 ^ 1, 0);

            // (0, 1, 0) vs (1, 0, 1) away from octant, on second half-lattice.
            LatticePoint3D c8 = new LatticePoint3D(i1 + i2, j1 + (j2 ^ 1), k1 + k2, 1);
            LatticePoint3D c9 = new LatticePoint3D(i1 + (i2 ^ 1), j1 + j2, k1 + (k2 ^ 1), 1);

            // (0, 0, 1) vs (1, 1, 0) away from octant.
            LatticePoint3D cA = new LatticePoint3D(i1, j1, k1 ^ 1, 0);
            LatticePoint3D cB = new LatticePoint3D(i1 ^ 1, j1 ^ 1, k1, 0);

            // (0, 0, 1) vs (1, 1, 0) away from octant, on second half-lattice.
            LatticePoint3D cC = new LatticePoint3D(i1 + i2, j1 + j2, k1 + (k2 ^ 1), 1);
            LatticePoint3D cD = new LatticePoint3D(i1 + (i2 ^ 1), j1 + (j2 ^ 1), k1 + k2, 1);

            // First two points are guaranteed.
            c0.nextOnFailure = c0.nextOnSuccess = c1;
            c1.nextOnFailure = c1.nextOnSuccess = c2;

            // If c2 is in range, then we know c3 and c4 are not.
            c2.nextOnFailure = c3; c2.nextOnSuccess = c5;
            c3.nextOnFailure = c4; c3.nextOnSuccess = c4;

            // If c4 is in range, then we know c5 is not.
            c4.nextOnFailure = c5; c4.nextOnSuccess = c6;
            c5.nextOnFailure = c5.nextOnSuccess = c6;

            // If c6 is in range, then we know c7 and c8 are not.
            c6.nextOnFailure = c7; c6.nextOnSuccess = c9;
            c7.nextOnFailure = c8; c7.nextOnSuccess = c8;

            // If c8 is in range, then we know c9 is not.
            c8.nextOnFailure = c9; c8.nextOnSuccess = cA;
            c9.nextOnFailure = c9.nextOnSuccess = cA;

            // If cA is in range, then we know cB and cC are not.
            cA.nextOnFailure = cB; cA.nextOnSuccess = cD;
            cB.nextOnFailure = cC; cB.nextOnSuccess = cC;

            // If cC is in range, then we know cD is not.
            cC.nextOnFailure = cD; cC.nextOnSuccess = null;
            cD.nextOnFailure = cD.nextOnSuccess = null;

            LOOKUP_3D[i] = c0;
        }
    }

}
