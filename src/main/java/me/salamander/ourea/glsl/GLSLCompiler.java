package me.salamander.ourea.glsl;

import me.salamander.ourea.glsl.transpile.JavaParser;
import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.method.StaticMethodResolver;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;
import me.salamander.ourea.modules.NoiseSampler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class GLSLCompiler {
    private static final CompiledMethod MARKER = new CompiledMethod(null, null, null);

    private final Map<String, ClassNode> cachedClasses = new HashMap<>();
    private final Map<MethodInfo, CompiledMethod> compiledMethods = new HashMap<>();
    private final Set<MethodInfo> identityLess = new HashSet<>();

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

        CompiledMethod compiled = new CompiledMethod(methodInfo, expressions, parser.getDependents());

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
}
