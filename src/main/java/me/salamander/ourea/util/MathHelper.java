package me.salamander.ourea.util;

import java.util.Random;

public class MathHelper {
    public static float lerp(float v0, float v1, float t) {
        return v0 * (1 - t) + v1 * t;
    }

    public static float lerp(float v00, float v01, float v10, float v11, float tx, float ty) {
        return lerp(lerp(v00, v01, tx), lerp(v10, v11, tx), ty);
    }

    public static float lerp(float v000, float v001, float v010, float v011, float v100, float v101, float v110, float v111, float tx, float ty, float tz) {
        return lerp(lerp(v000, v001, tx), lerp(v010, v011, tx), lerp(v100, v101, tx), lerp(v110, v111, tx), ty, tz);
    }

    public static float smoothstep(float t){
        //Quintic smoothing
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public static int floor(float n){
        int result = (int) n;
        return  result <= n ? result : result - 1;
    }

    public static int mod(int a, int b) {
        return (a % b + b) % b;
    }

    public static int hash(int x, int y, int seed) {
        seed ^= x * 1748247483;
        seed ^= y * 848274837;
        seed ^= seed >> 13;
        seed *= 16807;
        seed ^= seed >> 7;
        seed *= 16807;
        seed ^= seed >> 11;
        seed *= 16807;
        seed ^= seed >> 15;
        return (int) seed;
    }

    public static int hash(int x, int y, int z, int seed) {
        int hash = hash(x, y, seed);
        hash = hash(hash, z, seed);
        return hash;
    }

    public static float random(int x, int y, int seed){
        int hash = hash(x, y, seed);
        return (hash & 0xFFFF) / 65536f;
    }

    public static float random(int x, int y, int z, int seed){
        int hash = hash(x, y, z, seed);
        return (hash & 0xFFFF) / 65536f;
    }

    public static Grad2 getGradient(int x, int y, int seed){
        int hash = hash(x, y, seed);
        //random.setSeed(seed + x ^ 16363462 + y);
        //int hash = random.nextInt();
        return grad2[mod(hash, grad2.length)];
    }

    public static Grad3 getGradient(int x, int y, int z, int seed){
        int hash = hash(x, y, z, seed);
        return grad3[mod(hash, grad3.length)];
    }

    public static float sin(float n){
        return (float) Math.sin(n);
    }

    public static float cos(float n){
        return (float) Math.cos(n);
    }

    private static final Grad2[] grad2 = {
            new Grad2( 0.130526192220052f,  0.99144486137381f),
            new Grad2( 0.38268343236509f,   0.923879532511287f),
            new Grad2( 0.608761429008721f,  0.793353340291235f),
            new Grad2( 0.793353340291235f,  0.608761429008721f),
            new Grad2( 0.923879532511287f,  0.38268343236509f),
            new Grad2( 0.99144486137381f,   0.130526192220051f),
            new Grad2( 0.99144486137381f,  -0.130526192220051f),
            new Grad2( 0.923879532511287f, -0.38268343236509f),
            new Grad2( 0.793353340291235f, -0.60876142900872f),
            new Grad2( 0.608761429008721f, -0.793353340291235f),
            new Grad2( 0.38268343236509f,  -0.923879532511287f),
            new Grad2( 0.130526192220052f, -0.99144486137381f),
            new Grad2(-0.130526192220052f, -0.99144486137381f),
            new Grad2(-0.38268343236509f,  -0.923879532511287f),
            new Grad2(-0.608761429008721f, -0.793353340291235f),
            new Grad2(-0.793353340291235f, -0.608761429008721f),
            new Grad2(-0.923879532511287f, -0.38268343236509f),
            new Grad2(-0.99144486137381f,  -0.130526192220052f),
            new Grad2(-0.99144486137381f,   0.130526192220051f),
            new Grad2(-0.923879532511287f,  0.38268343236509f),
            new Grad2(-0.793353340291235f,  0.608761429008721f),
            new Grad2(-0.608761429008721f,  0.793353340291235f),
            new Grad2(-0.38268343236509f,   0.923879532511287f),
            new Grad2(-0.130526192220052f,  0.99144486137381f)
    };

    private static final Grad3[] grad3 = {
            new Grad3(-2.22474487139f,      -2.22474487139f,      -1.0f),
            new Grad3(-2.22474487139f,      -2.22474487139f,       1.0f),
            new Grad3(-3.0862664687972017f, -1.1721513422464978f,  0.0f),
            new Grad3(-1.1721513422464978f, -3.0862664687972017f,  0.0f),
            new Grad3(-2.22474487139f,      -1.0f,                -2.22474487139f),
            new Grad3(-2.22474487139f,       1.0f,                -2.22474487139f),
            new Grad3(-1.1721513422464978f,  0.0f,                -3.0862664687972017f),
            new Grad3(-3.0862664687972017f,  0.0f,                -1.1721513422464978f),
            new Grad3(-2.22474487139f,      -1.0f,                 2.22474487139f),
            new Grad3(-2.22474487139f,       1.0f,                 2.22474487139f),
            new Grad3(-3.0862664687972017f,  0.0f,                 1.1721513422464978f),
            new Grad3(-1.1721513422464978f,  0.0f,                 3.0862664687972017f),
            new Grad3(-2.22474487139f,       2.22474487139f,      -1.0f),
            new Grad3(-2.22474487139f,       2.22474487139f,       1.0f),
            new Grad3(-1.1721513422464978f,  3.0862664687972017f,  0.0f),
            new Grad3(-3.0862664687972017f,  1.1721513422464978f,  0.0f),
            new Grad3(-1.0f,                -2.22474487139f,      -2.22474487139f),
            new Grad3( 1.0f,                -2.22474487139f,      -2.22474487139f),
            new Grad3( 0.0f,                -3.0862664687972017f, -1.1721513422464978f),
            new Grad3( 0.0f,                -1.1721513422464978f, -3.0862664687972017f),
            new Grad3(-1.0f,                -2.22474487139f,       2.22474487139f),
            new Grad3( 1.0f,                -2.22474487139f,       2.22474487139f),
            new Grad3( 0.0f,                -1.1721513422464978f,  3.0862664687972017f),
            new Grad3( 0.0f,                -3.0862664687972017f,  1.1721513422464978f),
            new Grad3(-1.0f,                 2.22474487139f,      -2.22474487139f),
            new Grad3( 1.0f,                 2.22474487139f,      -2.22474487139f),
            new Grad3( 0.0f,                 1.1721513422464978f, -3.0862664687972017f),
            new Grad3( 0.0f,                 3.0862664687972017f, -1.1721513422464978f),
            new Grad3(-1.0f,                 2.22474487139f,       2.22474487139f),
            new Grad3( 1.0f,                 2.22474487139f,       2.22474487139f),
            new Grad3( 0.0f,                 3.0862664687972017f,  1.1721513422464978f),
            new Grad3( 0.0f,                 1.1721513422464978f,  3.0862664687972017f),
            new Grad3( 2.22474487139f,      -2.22474487139f,      -1.0f),
            new Grad3( 2.22474487139f,      -2.22474487139f,       1.0f),
            new Grad3( 1.1721513422464978f, -3.0862664687972017f,  0.0f),
            new Grad3( 3.0862664687972017f, -1.1721513422464978f,  0.0f),
            new Grad3( 2.22474487139f,      -1.0f,                -2.22474487139f),
            new Grad3( 2.22474487139f,       1.0f,                -2.22474487139f),
            new Grad3( 3.0862664687972017f,  0.0f,                -1.1721513422464978f),
            new Grad3( 1.1721513422464978f,  0.0f,                -3.0862664687972017f),
            new Grad3( 2.22474487139f,      -1.0f,                 2.22474487139f),
            new Grad3( 2.22474487139f,       1.0f,                 2.22474487139f),
            new Grad3( 1.1721513422464978f,  0.0f,                 3.0862664687972017f),
            new Grad3( 3.0862664687972017f,  0.0f,                 1.1721513422464978f),
            new Grad3( 2.22474487139f,       2.22474487139f,      -1.0f),
            new Grad3( 2.22474487139f,       2.22474487139f,       1.0f),
            new Grad3( 3.0862664687972017f,  1.1721513422464978f,  0.0f),
            new Grad3( 1.1721513422464978f,  3.0862664687972017f,  0.0f)
    };

    static {
        for (int i = 0; i < grad2.length; i++) {
            grad2[i] = grad2[i].normalized();
        }

        for (int i = 0; i < grad3.length; i++) {
            grad3[i] = grad3[i].normalized();
        }
    }
}
