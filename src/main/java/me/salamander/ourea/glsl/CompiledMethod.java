package me.salamander.ourea.glsl;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;
import me.salamander.ourea.glsl.transpile.tree.statement.ReturnStatement;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;
import me.salamander.ourea.util.Pair;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.Set;

public class CompiledMethod {
    private final MethodInfo info;
    private final Statement[] code;
    private final Set<MethodInfo> dependent;
    private final Set<Pair<Integer, Type>> variables;
    private final Set<Object> constants;
    private final boolean inline;

    //TODO: Inlining
    public CompiledMethod(MethodInfo info, Statement[] code, Set<MethodInfo> dependent, Set<Pair<Integer, Type>> variables, Set<Object> constants, boolean inline) {
        this.info = info;
        this.code = code;
        this.dependent = dependent;
        this.variables = variables;
        this.constants = constants;
        this.inline = inline;
    }

    public MethodInfo getInfo() {
        return info;
    }

    public Statement[] getCode() {
        return code;
    }

    public String declareVars(String begin, TranspilationInfo transpilationInfo){
        StringBuilder sb = new StringBuilder();
        Set<Integer> argIndices = new HashSet<>();

        int index = 0;
        if(!info.isStatic()) {
            index = 1;
            argIndices.add(0);
        }

        for(Type t : Type.getArgumentTypes(info.desc())) {
            argIndices.add(index);
            index += t.getSize();
        }

        Set<Pair<Integer, Type>> vars = new HashSet<>(variables);
        vars.removeIf(p -> argIndices.contains(p.first()));

        for(Pair<Integer, Type> p : vars) {
            sb.append(begin);
            sb.append(transpilationInfo.resolveType(p.second()));
            sb.append(" ");
            sb.append(generateVarName(p.first(), p.second()));
            sb.append(";\n");
        }

        return sb.toString();
    }

    public String generateDeclaration(TranspilationInfo info) {
        return this.info.generateDeclaration(info);
    }

    public static String generateVarName(int index, Type type) {
        return "var" + index + "_" + type.getDescriptor().replace(";", "").replace("/", "_").replace("[", "_").replace("]", "_").replace("$", "_");
    }

    public Set<Object> getConstants() {
        return constants;
    }
}
