package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.modules.NoiseSampler;
import org.objectweb.asm.Type;

public class SamplerSampleExpression implements Expression{
    private final NoiseSampler sampler;
    private final int dimensions;

    private final Expression x;
    private final Expression y;
    private final Expression z;
    private final Expression seed;

    public SamplerSampleExpression(NoiseSampler sampler, Expression x, Expression y, Expression seed) {
        this.sampler = sampler;
        this.x = x;
        this.y = y;
        this.z = null;
        this.seed = seed;
        this.dimensions = 2;
    }

    public SamplerSampleExpression(NoiseSampler sampler, Expression x, Expression y, Expression z, Expression seed) {
        this.sampler = sampler;
        this.x = x;
        this.y = y;
        this.z = z;
        this.seed = seed;
        this.dimensions = 3;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        if(dimensions == 2) {
            return "[PSEUD_EXPRESSION] " + sampler + ".sample(" + x.toGLSL(info, depth) + ", " + y.toGLSL(info, depth) + ")";
        }else{
            return "[PSEUD_EXPRESSION] " + sampler + ".sample(" + x.toGLSL(info, depth) + ", " + y.toGLSL(info, depth) + ", " + z.toGLSL(info, depth) + ")";
        }
    }

    @Override
    public Type getType() {
        return Type.FLOAT_TYPE;
    }

    @Override
    public boolean isConstant() {
        if(!seed.isConstant()) return false;
        if(!x.isConstant()) return false;
        if(!y.isConstant()) return false;
        if(dimensions == 3 && !z.isConstant()) return false;

        return true;
    }

    @Override
    public Object getConstantValue() {
        if(dimensions == 2){
            return sampler.sample((Float) x.getConstantValue(), (Float) y.getConstantValue(), (Integer) seed.getConstantValue());
        }else{
            return sampler.sample((Float) x.getConstantValue(), (Float) y.getConstantValue(), (Float) z.getConstantValue(), (Integer) seed.getConstantValue());
        }
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        if(dimensions == 2){
            return new SamplerSampleExpression(sampler, x.resolvePrecedingExpression(precedingExpression), y.resolvePrecedingExpression(precedingExpression), seed);
        }
        return new SamplerSampleExpression(sampler, x.resolvePrecedingExpression(precedingExpression), y.resolvePrecedingExpression(precedingExpression), z.resolvePrecedingExpression(precedingExpression), seed.resolvePrecedingExpression(precedingExpression));
    }
}
