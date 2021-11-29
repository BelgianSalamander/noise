package me.salamander.ourea.glsl.transpile.method;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;

public class StaticMethodResolver implements MethodResolver{
    private final String name;

    public StaticMethodResolver(String glslName){
        this.name = glslName;
    }

    @Override
    public String toGLSL(String owner, String name, String desc, TranspilationInfo info, Expression[] args) {
        StringBuilder sb = new StringBuilder(name + "(");
        for(int i = 0; i < args.length; i++){
            if(i > 0){
                sb.append(", ");
            }
            sb.append(args[i].toGLSL(info));
        }
        sb.append(")");
        return sb.toString();
    }
}
