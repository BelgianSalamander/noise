package me.salamander.ourea.glsl.transpile.tree.expression;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.method.MethodResolver;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvokeVirtualExpression implements Expression{
    private Expression[] args;
    private String owner;
    private String name;
    private String desc;
    private boolean itf;

    private Method method;

    public InvokeVirtualExpression(String owner, String name, String desc, boolean itf, Expression... args){
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.args = args;
        this.itf = itf;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        MethodResolver resolver = null;

        if(args[0].isConstant()){
            resolver = info.getMethodResolver(args[0].getConstantValue().getClass().getName().replace('.', '/'), name, desc);
        }

        if(resolver == null){
            resolver = info.getMethodResolver(owner, name, desc);
        }

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
        Object[] args = new Object[this.args.length - 1];
        for(int i = 1; i < this.args.length; i++){
            args[i - 1] = this.args[i].getConstantValue();
        }
        try {
            return method.invoke(this.args[0].getConstantValue(), args);
        }catch (IllegalAccessException | InvocationTargetException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPrecedence() {
        return 2;
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        for(int i = 0; i < args.length; i++){
            args[i] = args[i].resolvePrecedingExpression(precedingExpression);
        }
        return this;
    }

    private void loadMethod(){
        if(method == null) {
            Class<?> clazz = args[0].getClass();
            Type[] argTypes = new Type[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                argTypes[i - 1] = args[i].getType();
            }

            try {
                Class<?> argClasses[] = new Class<?>[argTypes.length];
                for (int i = 0; i < argTypes.length; i++) {
                    argClasses[i] = Class.forName(argTypes[i].getClassName());
                }

                while (clazz != null && method == null) {
                    method = clazz.getDeclaredMethod(name, argClasses);
                }

                method.setAccessible(true);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
