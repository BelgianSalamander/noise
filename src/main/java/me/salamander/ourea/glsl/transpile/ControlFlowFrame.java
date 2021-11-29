package me.salamander.ourea.glsl.transpile;

import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class ControlFlowFrame<T extends Value> extends Frame<T> {
    private Set<Frame<T>> successors = new HashSet<>();
    private Set<Frame<T>> predecessors = new HashSet<>();

    public ControlFlowFrame(int numLocals, int maxStack) {
        super(numLocals, maxStack);
    }

    public ControlFlowFrame(Frame<? extends T> frame) {
        super(frame);
    }

    void addSuccessor(Frame<T> frame) {
        successors.add(frame);
    }

    void addPredecessor(Frame<T> frame) {
        predecessors.add(frame);
    }

    public Iterable<? extends Frame<T>> getPredecessors() {
        return predecessors;
    }

    public Iterable<? extends Frame<T>> getSuccessors() {
        return successors;
    }
}
