package me.salamander.ourea.modules.modifier;

import me.salamander.ourea.modules.NoiseSampler;

public class BinaryModule implements NoiseSampler {
    private NoiseSampler first, second;
    private Operator op;

    public BinaryModule() {
        this.op = Operator.ADD;
    }

    public BinaryModule(NoiseSampler first, NoiseSampler second, Operator op) {
        this.first = first;
        this.second = second;
        this.op = op;
    }

    @Override
    public float sample(float x, float y, int seed) {
        return op.apply(first.sample(x, y, seed), second.sample(x, y, seed));
    }

    @Override
    public float sample(float x, float y, float z, int seed) {
        return op.apply(first.sample(x, y, z, seed), second.sample(x, y, z, seed));
    }

    public NoiseSampler getFirst() {
        return first;
    }

    public NoiseSampler getSecond() {
        return second;
    }

    public Operator getOperator() {
        return op;
    }

    public void setFirst(NoiseSampler first) {
        this.first = first;
    }

    public void setSecond(NoiseSampler second) {
        this.second = second;
    }

    public void setOperator(Operator op) {
        this.op = op;
    }

    public enum Operator{
        ADD{
            public float apply(float a, float b){
                return a + b;
            }
        },
        SUB{
            public float apply(float a, float b){
                return a - b;
            }
        },
        MUL{
            public float apply(float a, float b){
                return a * b;
            }
        },
        DIV{
            public float apply(float a, float b){
                return a / b;
            }
        },
        MAX{
            public float apply(float a, float b){
                return Math.max(a, b);
            }
        },
        MIN{
            public float apply(float a, float b){
                return Math.min(a, b);
            }
        }
        ;

        abstract float apply(float a, float b);
    }
}
