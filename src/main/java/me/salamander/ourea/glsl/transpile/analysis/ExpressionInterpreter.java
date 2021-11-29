package me.salamander.ourea.glsl.transpile.analysis;

import me.salamander.ourea.glsl.transpile.tree.ConstantExpression;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import me.salamander.ourea.glsl.transpile.tree.LoadVarExpression;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import java.util.List;

public class ExpressionInterpreter extends Interpreter<ExpressionValue> {
    /**
     * Constructs a new {@link Interpreter}.
     *
     * @param api the ASM API version supported by this interpreter. Must be one of {@link
     *            Opcodes#ASM4}, {@link Opcodes#ASM5}, {@link
     *            Opcodes#ASM6} or {@link Opcodes#ASM7}.
     */
    protected ExpressionInterpreter(int api) {
        super(api);
    }

    @Override
    public ExpressionValue newValue(Type type) {
        return new ExpressionValue(type);
    }

    @Override
    public ExpressionValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
        Expression expression = switch (insn.getOpcode()) {
            case Opcodes.NEW -> {
                throw new AnalyzerException(insn, "Object allocation/creation is not allowed");
            }
            case Opcodes.ACONST_NULL -> {
                throw new AnalyzerException(insn, "Null constant is not allowed");
            }
            case Opcodes.LDC -> new ConstantExpression(((LdcInsnNode) insn).cst);
            case Opcodes.ICONST_M1 -> new ConstantExpression(-1);
            case Opcodes.ICONST_0 -> new ConstantExpression(0);
            case Opcodes.ICONST_1 -> new ConstantExpression(1);
            case Opcodes.ICONST_2 -> new ConstantExpression(2);
            case Opcodes.ICONST_3 -> new ConstantExpression(3);
            case Opcodes.ICONST_4 -> new ConstantExpression(4);
            case Opcodes.ICONST_5 -> new ConstantExpression(5);
            case Opcodes.LCONST_0 -> new ConstantExpression(0L);
            case Opcodes.LCONST_1 -> new ConstantExpression(1L);
            case Opcodes.FCONST_0 -> new ConstantExpression(0.0f);
            case Opcodes.FCONST_1 -> new ConstantExpression(1.0f);
            case Opcodes.FCONST_2 -> new ConstantExpression(2.0f);
            case Opcodes.DCONST_0 -> new ConstantExpression(0.0);
            case Opcodes.DCONST_1 -> new ConstantExpression(1.0);
            case Opcodes.BIPUSH, Opcodes.SIPUSH -> new ConstantExpression(((IntInsnNode) insn).operand);
            default -> throw new AnalyzerException(insn, "Unsupported instruction");
        };
        ExpressionValue value = new ExpressionValue(expression.getType());
        value.addExpression(expression);
        return value;
    }

    @Override
    public ExpressionValue copyOperation(AbstractInsnNode insn, ExpressionValue value) throws AnalyzerException {
        int opcode = insn.getOpcode();
        if(opcode == Opcodes.ALOAD || opcode == Opcodes.ILOAD || opcode == Opcodes.FLOAD || opcode == Opcodes.DLOAD || opcode == Opcodes.LLOAD) {
            Expression expression = new LoadVarExpression(value.getType(), ((VarInsnNode) insn).var);
            return new ExpressionValue(expression);
        }

        return value;
    }

    @Override
    public ExpressionValue unaryOperation(AbstractInsnNode insn, ExpressionValue value) throws AnalyzerException {
        return null;
    }

    @Override
    public ExpressionValue binaryOperation(AbstractInsnNode insn, ExpressionValue value1, ExpressionValue value2) throws AnalyzerException {
        return null;
    }

    @Override
    public ExpressionValue ternaryOperation(AbstractInsnNode insn, ExpressionValue value1, ExpressionValue value2, ExpressionValue value3) throws AnalyzerException {
        return null;
    }

    @Override
    public ExpressionValue naryOperation(AbstractInsnNode insn, List<? extends ExpressionValue> values) throws AnalyzerException {
        return null;
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, ExpressionValue value, ExpressionValue expected) throws AnalyzerException {

    }

    @Override
    public ExpressionValue merge(ExpressionValue value1, ExpressionValue value2) {
        return null;
    }
}
