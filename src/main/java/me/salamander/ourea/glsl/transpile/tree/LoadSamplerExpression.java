package me.salamander.ourea.glsl.transpile.tree;

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
    public Object getConstantValue() {
        return sampler;
    }
}
