package me.salamander.ourea.glsl;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;
import org.objectweb.asm.Type;

public record MethodInfo(Object ownerObj, String ownerName, String name, String desc, boolean isStatic){
    public MethodInfo(Object ownerObj, String ownerName, String name, String desc, boolean isStatic) {
        this.ownerObj = ownerObj;
        this.ownerName = ownerName.replace('.', '/');
        this.name = name;
        this.desc = desc;
        this.isStatic = isStatic;
    }

    public String generateDeclaration(TranspilationInfo info) {
        Type returnType = Type.getReturnType(desc);
        Type[] argTypes = Type.getArgumentTypes(desc);

        StringBuilder sb = new StringBuilder();

        sb.append(info.resolveType(returnType));
        sb.append(" ");
        sb.append(generateName());
        sb.append("(");

        Type[] actualArgs;
        if(ownerObj == null && !isStatic) {
            actualArgs = new Type[argTypes.length + 1];
            actualArgs[0] = Type.getObjectType(ownerName);
            System.arraycopy(argTypes, 0, actualArgs, 1, argTypes.length);
        }else{
            actualArgs = argTypes;
        }

        for(int i = 0; i < actualArgs.length; i++) {
            if(i != 0) {
                sb.append(", ");
            }
            sb.append(info.resolveType(actualArgs[i]));
            sb.append(" ");
            sb.append("arg_").append(i);
        }

        return sb.append(")").toString();
    }

    public String generateName() {
        String baseName = name + "_" + ownerName.replace('/', '_').replace('.', '_').replace('$', '_');
        if(ownerObj == null) {
            return baseName;
        }

        //Scramble!
        String scramble = Math.abs(this.hashCode()) + "" + Math.abs(ownerObj.hashCode());
        return baseName + "_" + scramble;
    }

    public String call(TranspilationInfo info, Expression[] args) {
        StringBuilder sb = new StringBuilder();

        sb.append(generateName());
        sb.append("(");

        int start = ownerObj == null ? 0 : 1;
        for(int i = start; i < args.length; i++) {
            if(i != start) {
                sb.append(", ");
            }
            sb.append(args[i].toGLSL(info, 0));
        }

        return sb.append(")").toString();
    }
}
