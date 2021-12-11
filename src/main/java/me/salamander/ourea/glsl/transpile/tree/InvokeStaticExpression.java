package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.method.MethodResolver;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class InvokeStaticExpression implements Expression {
    private Expression[] args;
    private String owner;
    private String name;
    private String desc;

    private Method method;

    public InvokeStaticExpression(String owner, String name, String desc, Expression... args) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.args = args;

        if(Type.getReturnType(desc).equals(Type.VOID_TYPE)) {
            throw new RuntimeException("Cannot invoke static method " + owner + "." + name + " with void return type (It must be a valid function, not a procedure)");
        }
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

    @Override
    public boolean isConstant() {
        for(Expression arg : args){
            if(!arg.isConstant()){
                return false;
            }
        }
        return true;
    }

    @Override
    public Object getConstantValue() {
        loadMethod();
        try{
            return method.invoke(null, Arrays.stream(args).map(Expression::getConstantValue).toArray());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadMethod(){
        if(method == null){
            try {
                Class<?>[] parameterTypes = Arrays.stream(Type.getArgumentTypes(desc)).map(t -> {
                    try {
                        return Class.forName(t.getClassName());
                    }catch (ClassNotFoundException e){
                        throw new RuntimeException(e);
                    }
                }).toArray(Class<?>[]::new);

                Class<?> clazz = Class.forName(owner);
                while(clazz != null && method == null){
                    method = clazz.getDeclaredMethod(name, parameterTypes);
                    clazz = clazz.getSuperclass();
                }

                method.setAccessible(true);
            }catch (ClassNotFoundException | NoSuchMethodException e){
                throw new RuntimeException(e);
            }
        }
    }
}
