package me.salamander.ourea.glsl.transpile;

import me.salamander.ourea.glsl.CompiledMethod;
import me.salamander.ourea.glsl.transpile.method.MethodResolver;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TranspilationInfo {
    private final Map<String, MethodResolver> methodResolvers = new HashMap<>();
    private final CompiledMethod method;
    private final int argMax;
    private final int[] argIndices;
    private Map<Object, String> constants;
    private Set<String> referenceFields = new HashSet<>();
    private Map<Type, ConstantType> constantTypes = new HashMap<>();

    public TranspilationInfo() {
        this.method = null;
        this.argMax = -1;
        this.argIndices = null;
    }

    private TranspilationInfo(Map<String, MethodResolver> methodResolvers, CompiledMethod method, Map<Object, String> constants, Set<String> referenceFields) {
        this.method = method;
        this.methodResolvers.putAll(methodResolvers);
        this.constants = constants;
        this.referenceFields = referenceFields;

        int argMax = 0;
        if(!method.getInfo().isStatic()){
            argMax++;
        }

        for(Type type: Type.getArgumentTypes(method.getInfo().desc())){
            argMax += type.getSize();
        }

        this.argMax = argMax;

        this.argIndices = new int[argMax];
        int varIndex = 0;
        int argIndex = 0;
        if(!method.getInfo().isStatic()){
            argIndices[varIndex++] = argIndex++;
        }

        for(Type type: Type.getArgumentTypes(method.getInfo().desc())){
            argIndices[varIndex] = argIndex++;
            varIndex += type.getSize();
        }
    }

    public TranspilationInfo forMethod(CompiledMethod method) {
        return new TranspilationInfo(methodResolvers, method, constants, referenceFields);
    }

    public void addMethodResolver(String owner, String name, String desc, MethodResolver resolver) {
        methodResolvers.put(owner + "#" + name + " " + desc, resolver);
    }

    public MethodResolver getMethodResolver(String owner, String name, String desc) {
        return methodResolvers.get(owner + "#" + name + " " + desc);
    }

    public String getVarName(int index, Type type){
        if(method != null) {
            if (index < argMax) {
                int argIndex = argIndices[index];
                if(method.getInfo().ownerObj() != null){
                    argIndex--;
                }
                return "arg_" + argIndex;
            } else {
                return CompiledMethod.generateVarName(index, type);
            }
        }else{
            return "unresolved_var_" + index;
        }
    }

    public String getConstant(Object constant) {
        if(constants != null) {
            return constants.get(constant);
        }else{
            return "unresolved_constant_" + constant;
        }
    }

    public void setConstants(Map<Object, String> constants) {
        this.constants = constants;
    }

    public void addReferenceField(Type type, String name){
        referenceFields.add(type.getClassName() + "#" + name);
    }

    public boolean isReferenceField(Type type, String name){
        return referenceFields.contains(type.getClassName() + "#" + name);
    }

    public void addConstantType(Type type, ConstantType constantType) {
        constantTypes.put(type, constantType);
    }

    public String resolveType(Type type){
        if(constantTypes.containsKey(type)){
            return constantTypes.get(type).getName();
        }else{
            return type.getClassName().replace('.', '_').replace('$', '_');
        }
    }
}
