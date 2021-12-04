package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.method.MethodResolver;
import org.objectweb.asm.Type;

public class InvokeVirtualExpression implements Expression{
    private Expression[] args;
    private String owner;
    private String name;
    private String desc;

    public InvokeVirtualExpression(String owner, String name, String desc, Expression... args){
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.args = args;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        MethodResolver resolver = info.getMethodResolver(owner, name, desc);
        if(resolver != null) {
            return resolver.toGLSL(owner, name, desc, info, args);
        }
        String methodName = "[Unrecognised Method " + owner + "." + name + "]";
        StringBuilder sb = new StringBuilder(methodName + "(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(args[i].toGLSL(info, 0));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Type getType() {
        return Type.getReturnType(desc);
    }
}
