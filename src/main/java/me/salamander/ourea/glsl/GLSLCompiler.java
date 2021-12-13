package me.salamander.ourea.glsl;

import me.salamander.ourea.glsl.transpile.*;
import me.salamander.ourea.glsl.transpile.method.MethodResolver;
import me.salamander.ourea.glsl.transpile.method.StaticMethodResolver;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;
import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.util.Grad2;
import me.salamander.ourea.util.Grad3;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class GLSLCompiler {
    private static final Map<Class<?>, ConstantType> BUILTIN_TYPES = new HashMap<>();
    private static final CompiledMethod MARKER = new CompiledMethod(null, null, null, null, null);

    private final Map<String, ClassNode> cachedClasses = new HashMap<>();
    private final Map<MethodInfo, CompiledMethod> compiledMethods = new HashMap<>();
    private final Set<MethodInfo> identityLess = new HashSet<>();
    private final Set<Type> nullableTypes = new HashSet<>();

    private final TranspilationInfo transpilationInfo = new TranspilationInfo();

    public GLSLCompiler(NoiseSampler sampler, int dimensions){
        String desc = dimensions == 2 ? "(FFI)F" : "(FFFI)F";
        MethodInfo methodInfo = new MethodInfo(sampler, sampler.getClass().getName(), "sample", desc, false);

        compiledMethods.put(methodInfo, MARKER);
        createInfo();
    }

    private void createInfo() {
        transpilationInfo.addMethodResolver("me/salamander/ourea/util/MathHelper", "floor", "(F)I", new StaticMethodResolver("floor"));
        transpilationInfo.addMethodResolver("me/salamander/ourea/util/MathHelper", "smoothstep", "(F)F", new StaticMethodResolver("smoothstep"));
        transpilationInfo.addMethodResolver("me/salamander/ourea/util/MathHelper", "lerp", "(FFF)F", new StaticMethodResolver("mix"));
        transpilationInfo.addMethodResolver("me/salamander/ourea/util/MathHelper", "lerp", "(FFFFFF)F", new StaticMethodResolver("lerp"));

        transpilationInfo.addMethodResolver("me/salamander/ourea/util/MathHelper", "cos", "(F)F", new StaticMethodResolver("cos"));
        transpilationInfo.addMethodResolver("me/salamander/ourea/util/MathHelper", "sin", "(F)F", new StaticMethodResolver("sin"));

        transpilationInfo.addMethodResolver("me/salamander/ourea/util/MathHelper", "getGradient", "(III)Lme/salamander/ourea/util/Grad2;", new StaticMethodResolver("getGradient"));

        transpilationInfo.addMethodResolver("me/salamander/ourea/util/Grad2", "dot", "(FF)F", (owner, name, desc, info, args) -> "dot(" + args[0].toGLSL(info, 0) + ", vec2(" + args[1].toGLSL(info, 0) + ", " + args[2].toGLSL(info, 0) + "))");
    }

    public String link(){
        //Manage constants
        Set<Object> allGlobalConstants = new HashSet<>();
        for(CompiledMethod method : compiledMethods.values()){
            allGlobalConstants.addAll(method.getConstants());
        }

        Set<Object> allConstantValues = new HashSet<>();
        for(Object constant : allGlobalConstants){
            allConstantValues.addAll(getBaseValues(constant));
        }

        Set<Object> uncheckedConstants = new HashSet<>(allConstantValues);
        Set<Object> newConstants = new HashSet<>();
        while(!uncheckedConstants.isEmpty()){
            for(Object constant : uncheckedConstants){
                newConstants.addAll(getRelatedValues(constant));
            }
            Set<Object> temp = new HashSet<>(newConstants);
            allConstantValues.addAll(uncheckedConstants);
            uncheckedConstants.clear();
            newConstants.clear();
            temp.removeAll(allConstantValues);
            uncheckedConstants.addAll(temp);
        }

        Set<Class<?>> types = new HashSet<>();
        for(Object constant : allConstantValues){
            types.add(constant.getClass());
        }

        Map<Class<?>, ConstantType> constantTypes = new HashMap<>();

        for(Class<?> type : types){
            Type t = Type.getType(type);
            CustomConstantType constantType = new CustomConstantType(type, nullableTypes.contains(t));
            constantTypes.put(type, constantType);
            transpilationInfo.addConstantType(t, constantType);
            for(String field: constantType.referenceFields()){
                transpilationInfo.addReferenceField(t, field);
            }
        }

        for(Map.Entry<Class<?>, ConstantType> entry: BUILTIN_TYPES.entrySet()){
            if(constantTypes.containsKey(entry.getKey())){
                constantTypes.put(entry.getKey(), entry.getValue());
                transpilationInfo.addConstantType(Type.getType(entry.getKey()), entry.getValue());
            }
        }

        Map<ConstantType, List<Object>> constantValues = new HashMap<>();

        for(ConstantType type : constantTypes.values()){
            List<Object> values = new ArrayList<>();
            if(type instanceof CustomConstantType customType) {
                if (customType.isNullable()) {
                    values.add(null);
                }
            }
            constantValues.put(type, values);
        }

        for(Object constant : allConstantValues){
            ConstantType type = constantTypes.get(constant.getClass());
            List<Object> values = constantValues.get(type);
            values.add(constant);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("#version 450 core\n\n");


        for(ConstantType type : constantTypes.values()){
            sb.append(type.declare());
            sb.append("\n");
        }

        for(ConstantType type : constantTypes.values()){
            //Declare arrays
            sb.append(type.getName());
            sb.append(" ");
            sb.append(type.getName());
            sb.append("_constants[] = {\n");
            List<Object> values = constantValues.get(type);
            for(int i = 0; i < values.size(); i++){
                sb.append("  ");
                sb.append(type.create(values.get(i), constantValues, constantTypes));
                if(i < values.size() - 1){
                    sb.append(",\n");
                }
            }
            sb.append("\n}\n\n");
        }

        Map<Object, String> constants = new HashMap<>();

        int i = 0;
        for(Object constant : allGlobalConstants){
            int dimension = 0;
            Class<?> baseType = constant.getClass();
            while(baseType.isArray()){
                baseType = baseType.getComponentType();
                dimension++;
            }

            ConstantType type = constantTypes.get(baseType);

            String name = "constant_" + i;

            sb.append(constantTypes.get(baseType).getName());
            sb.append(" ");
            sb.append(name);
            sb.append("[]".repeat(dimension));
            sb.append(" = ");

            boolean identical = false;
            if(constant instanceof Object[] arr){
                if(Arrays.equals(arr, constantValues.get(type).toArray())){
                    sb.append(type.getName());
                    sb.append("_constants");
                    identical = true;
                }
            }
            if(!identical) {
                sb.append(createRecursively(constant, constantValues.get(type), type.getName() + "_constants"));
            }
            sb.append(";\n");

            constants.put(constant, name);
            i++;
        }

        transpilationInfo.setConstants(constants);

        for(CompiledMethod method : compiledMethods.values()){
            sb.append(method.generateDeclaration(transpilationInfo));
            sb.append(";\n");
        }

        sb.append("\n");

        //Generate method resolvers
        for(Map.Entry<MethodInfo, CompiledMethod> entry : compiledMethods.entrySet()){
            MethodInfo methodInfo = entry.getKey();
            CompiledMethod compiledMethod = entry.getValue();

            if(methodInfo.ownerObj() == null) {
                MethodResolver resolver = (owner, name, desc, info, args) -> methodInfo.call(info, args);
                transpilationInfo.addMethodResolver(methodInfo.ownerName(), methodInfo.name(), methodInfo.desc(), resolver);
            }
        }

        Map<MethodInfo, List<CompiledMethod>> identityMethods = compiledMethods.values().stream().filter(method -> method.getInfo().ownerObj() != null).collect(Collectors.groupingBy(method -> new MethodInfo(null, method.getInfo().ownerName(), method.getInfo().name(), method.getInfo().desc(), method.getInfo().isStatic())));
        for (Map.Entry<MethodInfo, List<CompiledMethod>> entry : identityMethods.entrySet()) {
            MethodInfo methodInfo = entry.getKey();
            List<CompiledMethod> compiledMethods = entry.getValue();

            MethodResolver resolver;
            if(compiledMethods.size() == 1){
                resolver = (owner, name, desc, info, args) -> compiledMethods.get(0).getInfo().call(info, args);
            }else{
                resolver = (owner, name, desc, info, args) -> {
                    Object val = args[0].getConstantValue();
                    for(CompiledMethod compiledMethod : compiledMethods){
                        if(compiledMethod.getInfo().ownerObj() == val){
                            return compiledMethod.getInfo().call(info, args);
                        }
                    }
                    throw new RuntimeException("Could not find compiled method for " + val);
                };
            }

            Class<?> clazz = compiledMethods.get(0).getInfo().ownerObj().getClass();
            Class<?>[] parameterTypes = Arrays.stream(Type.getArgumentTypes(methodInfo.desc())).map(GLSLCompiler::getClass).toArray(Class<?>[]::new);

            transpilationInfo.addMethodResolver(methodInfo.ownerName(), methodInfo.name(), methodInfo.desc(), resolver);

            for(Class<?> itf: clazz.getInterfaces()){
                if(contains(itf.getDeclaredMethods(), methodInfo.name(), parameterTypes)){
                    transpilationInfo.addMethodResolver(itf.getName().replace('.', '/'), methodInfo.name(), methodInfo.desc(), resolver);
                }
            }
        }

        //Generate method definitions
        for(Map.Entry<MethodInfo, CompiledMethod> entry : compiledMethods.entrySet()){
            MethodInfo methodInfo = entry.getKey();
            CompiledMethod compiledMethod = entry.getValue();

            TranspilationInfo info = transpilationInfo.forMethod(compiledMethod);

            sb.append(methodInfo.generateDeclaration(info));
            sb.append("{\n");
            sb.append(compiledMethod.declareVars("  ", transpilationInfo));
            for(Statement statement : compiledMethod.getCode()){
                sb.append(statement.toGLSL(info, 1));
            }
            sb.append("}\n\n");
        }

        return sb.toString();
    }

    private Collection<Object> getRelatedValues(Object constant) {
        //TODO: Cache reflection
        List<Field> fields = getAllFields(constant.getClass());
        Set<Object> values = new HashSet<>();
        for(Field field : fields){
            if(!field.getType().isPrimitive()){
                try {
                    Object value = field.get(constant);
                    if(value != null){
                        values.add(value);
                    }
                }catch (IllegalAccessException e){
                    throw new RuntimeException(e);
                }
            }
        }
        return values;
    }

    public static List<Field> getAllFields(Class<?> clazz){
        List<Field> fields = new ArrayList<>();
        while(clazz != null){
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        fields.removeIf(field -> Modifier.isStatic(field.getModifiers()));
        fields.forEach(f -> f.setAccessible(true));
        return fields;
    }

    private static List<Object> getBaseValues(Object obj){
        List<Object> values = new ArrayList<>();
        values.add(obj);
        while(values.get(0) instanceof Object[] arr){
            List<Object> next = new ArrayList<>();
            for(Object val : (Object[])values.get(0)){
                next.add(val);
            }
            values = next;
        }
        return values;
    }

    private static String createRecursively(Object obj, List<Object> constantArray, String constantArrayName){
        StringBuilder sb = new StringBuilder();
        if(obj instanceof Object[] array){
            sb.append("{");
            for(int i = 0; i < array.length; i++){
                if(i > 0){
                    sb.append(", ");
                }
                sb.append(createRecursively(array[i], constantArray, constantArrayName));
            }
            sb.append("}");
        }else{
            sb.append(constantArrayName);
            sb.append("[");
            sb.append(constantArray.indexOf(obj));
            sb.append("]");
        }

        return sb.toString();
    }

    private static Class<?> getClass(Type t){
        if(t.getSort() == Type.OBJECT){
            try {
                return Class.forName(t.getClassName());
            }catch (ClassNotFoundException e){
                throw new RuntimeException(e);
            }
        }else if(t.getSort() == Type.ARRAY){
            int dimensions = t.getDimensions();
            Class<?> componentType = getClass(t.getElementType());
            for (int i = 0; i < dimensions; i++) {
                componentType = componentType.arrayType();
            }
            return componentType;
        }else if(t.getSort() == Type.VOID){
            return void.class;
        }else if(t.getSort() == Type.BOOLEAN){
            return boolean.class;
        }else if(t.getSort() == Type.BYTE){
            return byte.class;
        }else if(t.getSort() == Type.CHAR){
            return char.class;
        }else if(t.getSort() == Type.SHORT){
            return short.class;
        }else if(t.getSort() == Type.INT){
            return int.class;
        }else if(t.getSort() == Type.LONG){
            return long.class;
        }else if(t.getSort() == Type.FLOAT){
            return float.class;
        }else if(t.getSort() == Type.DOUBLE){
            return double.class;
        }else {
            throw new RuntimeException("Unknown type " + t);
        }
    }

    private static boolean contains(Method[] methods, String name, Class<?>[] args){
        for(Method method : methods){
            if(method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), args)){
                return true;
            }
        }
        return false;
    }

    public void compileMethods(){
        while (compiledMethods.containsValue(MARKER)){
            List<MethodInfo> toCompile = compiledMethods.entrySet().stream().filter(e -> e.getValue() == MARKER).map(Map.Entry::getKey).toList();

            for(MethodInfo info : toCompile){
                compileMethod(info);
            }
        }

        System.out.println("Compiled all required methods: (" + compiledMethods.size() + ")");
        for(Map.Entry<MethodInfo, CompiledMethod> entry : compiledMethods.entrySet()){
            System.out.println("\t" + entry.getKey().ownerName() + "." + entry.getKey().name() + ":" + entry.getKey().desc());
            for(Statement expr: entry.getValue().getCode()){
                System.out.println(expr.toGLSL(transpilationInfo, 4));
            }
        }
    }

    private CompiledMethod compileMethod(MethodInfo methodInfo) {
        ClassNode classNode = getClass(methodInfo.ownerName());
        MethodInfo finalMethodInfo = methodInfo;
        MethodNode methodNode = classNode.methods.stream().filter(m -> m.name.equals(finalMethodInfo.name()) && m.desc.equals(finalMethodInfo.desc())).findFirst().orElse(null);

        if(methodNode == null){
            throw new RuntimeException("Method not found: " + methodInfo.ownerName() + "." + methodInfo.name() + methodInfo.desc());
        }

        JavaParser parser = new JavaParser(methodInfo.ownerObj(), classNode, methodNode);
        parser.parseAll();

        Statement[] expressions = parser.flattenGraph();
        boolean needsIdentity = parser.requiresIdentity();

        if(!needsIdentity){
            MethodInfo newInfo = new MethodInfo(null, methodInfo.ownerName(), methodInfo.name(), methodInfo.desc(), methodInfo.isStatic());
            compiledMethods.remove(methodInfo);
            methodInfo = newInfo;
        }

        nullableTypes.addAll(parser.nullableTypes());
        CompiledMethod compiled = new CompiledMethod(methodInfo, expressions, parser.getDependents(), parser.getVariables(), parser.getConstants());

        for(MethodInfo dependent: parser.getDependents()){
            if(getCompiledMethod(dependent) == null){
                putCompiledMethod(dependent, MARKER);
            }
        }

        compiledMethods.put(methodInfo, compiled);

        if(needsIdentity){
            identityLess.add(methodInfo);
        }

        return compiled;
    }

    private ClassNode getClass(String name){
        if(cachedClasses.containsKey(name)){
            return cachedClasses.get(name);
        }

        String classPath = name.replace('.', '/') + ".class";

        try{
            InputStream is = ClassLoader.getSystemResourceAsStream(classPath);
            ClassReader reader = new ClassReader(is);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            cachedClasses.put(name, node);
            return node;
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private CompiledMethod getCompiledMethod(MethodInfo info){
        if(info.ownerObj() == null){
            return compiledMethods.get(info);
        }

        MethodInfo withoutIdentity = new MethodInfo(null, info.ownerName(), info.name(), info.desc(), info.isStatic());
        if(identityLess.contains(withoutIdentity)){
            return compiledMethods.get(withoutIdentity);
        }

        return compiledMethods.get(info);
    }

    private void putCompiledMethod(MethodInfo info, CompiledMethod marker) {
        if(info.ownerObj() == null){
            compiledMethods.put(info, marker);
            return;
        }

        MethodInfo withoutIdentity = new MethodInfo(null, info.ownerName(), info.name(), info.desc(), info.isStatic());
        if(identityLess.contains(withoutIdentity)){
            compiledMethods.put(withoutIdentity, marker);
            return;
        }

        compiledMethods.put(info, marker);
    }

    static {
        BUILTIN_TYPES.put(Grad2.class, new BuiltinConstantType("vec2", Grad2.class, "x", "y"));
        BUILTIN_TYPES.put(Grad3.class, new BuiltinConstantType("vec3", Grad3.class, "x", "y", "z"));
    }
}
