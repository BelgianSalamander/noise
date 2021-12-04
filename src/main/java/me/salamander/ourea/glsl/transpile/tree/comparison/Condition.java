package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import org.objectweb.asm.Type;

public abstract class Condition implements Expression {
    public abstract Condition negate();

    @Override
    public Type getType() {
        return Type.BOOLEAN_TYPE;
    }

    public static Condition of(boolean value) {
        return new BooleanCondition(value);
    }

    private static class BooleanCondition extends Condition {
        private final boolean value;

        public BooleanCondition(boolean value) {
            this.value = value;
        }

        @Override
        public Condition negate() {
            return new BooleanCondition(!value);
        }

        @Override
        public String toGLSL(TranspilationInfo info, int depth) {
            return Boolean.toString(value);
        }
    }
}
