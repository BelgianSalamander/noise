package me.salamander.ourea.glsl.transpile.tree.expression;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.modules.NoiseSampler;
import org.objectweb.asm.Type;

public class LoadSamplerExpression implements Expression{
    private final NoiseSampler sampler;

    public LoadSamplerExpression(NoiseSampler sampler){
        this.sampler = sampler;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "[PSEUDO_STATEMENT] Load Sampler Expression '" + sampler + "'";
    }

    @Override
    public Type getType() {
        return Type.getType(NoiseSampler.class);
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public NoiseSampler getConstantValue() {
        return sampler;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return this;
    }
}