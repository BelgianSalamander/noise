package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.ConstantExpression;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Stack;

public class JumpIfExpression implements Expression {
    private final Expression left;
    private final Expression right;
    private final Operator operator;

    public JumpIfExpression(Stack<Expression> stack, int opcode) {
        if(opcode == Opcodes.IFEQ || opcode == Opcodes.IFNE || opcode == Opcodes.IFGE || opcode == Opcodes.IFGT || opcode == Opcodes.IFLE || opcode == Opcodes.IFLT) {
            Expression leftTemp = stack.pop();
            if(leftTemp.getType() != Type.INT_TYPE){
                throw new RuntimeException("Invalid type for comparison");
            }
            if(leftTemp instanceof CompareExpression comparison) {
                leftTemp = comparison.getLeft();
                right = comparison.getRight();
            }else{
                right = new ConstantExpression(0);
            }
            operator = switch (opcode) {
                case Opcodes.IFEQ -> Operator.EQ;
                case Opcodes.IFNE -> Operator.NE;
                case Opcodes.IFGE -> Operator.GE;
                case Opcodes.IFGT -> Operator.GT;
                case Opcodes.IFLE -> Operator.LE;
                case Opcodes.IFLT -> Operator.LT;
                default -> throw new RuntimeException("Invalid operator");
            };
            left = leftTemp;
        }else if(opcode == Opcodes.IF_ICMPEQ || opcode == Opcodes.IF_ICMPNE || opcode == Opcodes.IF_ICMPGE || opcode == Opcodes.IF_ICMPGT || opcode == Opcodes.IF_ICMPLE || opcode == Opcodes.IF_ICMPLT){
            right = stack.pop();
            left = stack.pop();
            if(left.getType() != Type.INT_TYPE || right.getType() != Type.INT_TYPE){
                throw new RuntimeException("Invalid type for comparison");
            }
            operator = switch (opcode) {
                case Opcodes.IF_ICMPEQ -> Operator.EQ;
                case Opcodes.IF_ICMPNE -> Operator.NE;
                case Opcodes.IF_ICMPGE -> Operator.GE;
                case Opcodes.IF_ICMPGT -> Operator.GT;
                case Opcodes.IF_ICMPLE -> Operator.LE;
                case Opcodes.IF_ICMPLT -> Operator.LT;
                default -> throw new RuntimeException("Invalid operator");
            };
        }else{
            throw new RuntimeException("Invalid opcode for comparison");
        }
    }

    public ActualCompareExpression getCondition(){
        return new ActualCompareExpression(left, right, operator);
    }

    public String negatedGLSL(TranspilationInfo info){
        return "[PSEUDO_STATEMENT] Jump If " + left.toGLSL(info, 0) + " " + operator.opposite.symbol + " " + right.toGLSL(info, 0);
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "[PSEUDO_STATEMENT] Jump If " + left.toGLSL(info, 0) + " " + operator.symbol + " " + right.toGLSL(info, 0);
    }

    @Override
    public Type getType() {
        return Type.VOID_TYPE;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Object getConstantValue() {
        throw new RuntimeException("Not a constant");
    }

    enum Operator {
        EQ(1, "=="){
            @Override
            public boolean apply(int a, int b) {
                return a == b;
            }
        },
        NE(0, "!="){
            @Override
            public boolean apply(int a, int b) {
                return a != b;
            }
        },
        GE(5, ">="){
            @Override
            public boolean apply(int a, int b) {
                return a >= b;
            }
        },
        GT(4, ">"){
            @Override
            public boolean apply(int a, int b) {
                return a > b;
            }
        },
        LE(4, "<="){
            @Override
            public boolean apply(int a, int b) {
                return a <= b;
            }
        },
        LT(2, "<"){
            @Override
            public boolean apply(int a, int b) {
                return a < b;
            }
        };

        private final int oppositeIndex;
        private final String symbol;
        private Operator opposite;

        Operator(int opposite, String symbol) {
            this.oppositeIndex = opposite;
            this.symbol = symbol;
        }

        public String getSymbol(){
            return symbol;
        }
        public Operator getOpposite() {
            return opposite;
        }
        public abstract boolean apply(int a, int b);

        static{
            for(Operator op : Operator.values()){
                op.opposite = Operator.values()[op.oppositeIndex];
            }
        }
    }
}
