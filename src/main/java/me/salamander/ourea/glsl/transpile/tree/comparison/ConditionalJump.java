package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.tree.statement.Statement;

public interface ConditionalJump extends Statement {
    Condition getCondition();
}
