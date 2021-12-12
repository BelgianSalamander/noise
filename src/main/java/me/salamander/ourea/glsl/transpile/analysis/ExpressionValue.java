package me.salamander.ourea.glsl.transpile.analysis;

import me.salamander.ourea.glsl.transpile.tree.expression.Expression;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.HashSet;
import java.util.Set;

public class ExpressionValue extends BasicValue {
    private final Set<Expression> expressions = new HashSet<>();

    /**
     * Constructs a new {@link BasicValue} of the given type.
     *
     * @param type the value type.
     */
    public ExpressionValue(Type type) {
        super(type);
    }

    public ExpressionValue(Expression expression) {
        this(expression.getType());
        expressions.add(expression);
    }

    public void addExpression(Expression expression) {
        expressions.add(expression);
    }
}
