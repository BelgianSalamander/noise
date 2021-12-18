package me.salamander.ourea.modules.modifier;

import me.salamander.ourea.modules.NoiseSampler;

public class UnaryModule implements NoiseSampler {
    private NoiseSampler source;
    private Operator op;

    public UnaryModule(NoiseSampler source, Operator op){
        this.source = source;
        this.op = op;
    }

    @Override
    public float sample(float x, float y, int seed) {
        return op.apply(source.sample(x, y, seed));
    }

    @Override
    public float sample(float x, float y, float z, int seed) {
        return op.apply(source.sample(x, y, z, seed));
    }

    public enum Operator {
        NEGATE{
            public float apply(float x){
                return -x;
            }
        },
        SIN{
            public float apply(float x){
                return (float)Math.sin(x);
            }
        },
        COS{
            public float apply(float x){
                return (float)Math.cos(x);
            }
        },
        TANH{
            public float apply(float x){
                return (float)Math.tanh(x);
            }
        }
        ;
        abstract float apply(float x);
    }
}
