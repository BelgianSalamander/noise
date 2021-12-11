package me.salamander.ourea.glsl.transpile;

import me.salamander.ourea.glsl.transpile.tree.*;
import me.salamander.ourea.glsl.transpile.tree.comparison.*;
import me.salamander.ourea.modules.NoiseSampler;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.GOTO;

public class CFGNode {
    private static int nextId = 0;
    private final List<JavaTranspiler.FrameInfo> frames = new ArrayList<>();
    private final Set<CFGNode> prev = new HashSet<>();
    private final JavaTranspiler transpiler;
    NodeType type;

    private final List<Expression> statements = new ArrayList<>();
    private Expression expression;

    CFGNode normalNext;
    CFGNode exceptionalNext;

    private final String id;

    public CFGNode(JavaTranspiler.FrameInfo frameInfo, JavaTranspiler transpiler){
        frames.add(frameInfo);
        id = "node " + nextId++;
        this.transpiler = transpiler;
    }

    public void addPrev(CFGNode node){
        prev.add(node);
    }

    public void setNormalNext(CFGNode node){
        normalNext = node;
    }

    public void setExceptionalNext(CFGNode node){
        exceptionalNext = node;
    }

    public void reduce(){
        Stack<CFGNode> toProcess = new Stack<>();
        toProcess.add(this);
        Set<CFGNode> processed = new HashSet<>();
        while(!toProcess.isEmpty()){
            CFGNode node = toProcess.pop();
            if(processed.contains(node)){
                continue;
            }
            processed.add(node);
            while(node.mergeWithNext()){

            }

            if(node.normalNext != null){
                toProcess.add(node.normalNext);
            }

            if(node.exceptionalNext != null){
                toProcess.add(node.exceptionalNext);
            }
        }
    }

    public boolean mergeWithNext(){
        if(normalNext != null && exceptionalNext == null){
            if(normalNext.prev.size() == 1) {
                CFGNode next = normalNext;
                frames.addAll(next.frames);
                this.normalNext = next.normalNext;
                this.exceptionalNext = next.exceptionalNext;
                this.type = next.type;

                //Fix up the next nodes
                if(next.normalNext != null){
                    next.normalNext.prev.remove(next);
                    next.normalNext.prev.add(this);
                }

                if(next.exceptionalNext != null){
                    next.exceptionalNext.prev.remove(next);
                    next.exceptionalNext.prev.add(this);
                }

                return true;
            }
        }
        return false;
    }

    public void parse(){
        Stack<Expression> stack = new Stack<>();

        int k = -1;
        for(JavaTranspiler.FrameInfo frameInfo : frames){
            k++;
            Expression expression = null;
            AbstractInsnNode insn = frameInfo.insnNode;
            if(insn.getOpcode() == -1){
                continue;
            }
            switch (insn.getOpcode()){
                case ILOAD -> expression = new LoadVarExpression(Type.INT_TYPE, ((VarInsnNode) insn).var);
                case FLOAD -> expression = new LoadVarExpression(Type.FLOAT_TYPE, ((VarInsnNode) insn).var);
                case DLOAD -> expression = new LoadVarExpression(Type.DOUBLE_TYPE, ((VarInsnNode) insn).var);
                case LLOAD -> expression = new LoadVarExpression(Type.LONG_TYPE, ((VarInsnNode) insn).var);
                case ALOAD -> {
                    //Get type
                    Type type;
                    if(k != frames.size() - 1){
                        type = frames.get(k + 1).getTop().getType();
                    }else{
                        //We can get it from next
                        type = normalNext.frames.get(0).getTop().getType();
                    }
                    expression = new LoadVarExpression(type, ((VarInsnNode) insn).var);
                }

                case GETFIELD -> {
                    expression = new GetFieldExpression(stack.pop(), ((FieldInsnNode) insn).name, ((FieldInsnNode) insn).desc);
                }

                case IADD, LADD, FADD, DADD -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    expression = new BinaryExpression(left, right, BinaryExpression.Operator.ADD);
                }
                case ISUB, LSUB, FSUB, DSUB -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    expression = new BinaryExpression(left, right, BinaryExpression.Operator.SUB);
                }
                case IMUL, LMUL, FMUL, DMUL -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    expression = new BinaryExpression(left, right, BinaryExpression.Operator.MUL);
                }
                case IDIV, LDIV, FDIV, DDIV -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    expression = new BinaryExpression(left, right, BinaryExpression.Operator.DIV);
                }
                case IREM, LREM, FREM, DREM -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    expression = new BinaryExpression(left, right, BinaryExpression.Operator.MOD);
                }

                case LDC -> expression = new ConstantExpression(((LdcInsnNode) insn).cst);
                case ICONST_M1 -> expression = new ConstantExpression(-1);
                case ICONST_0 -> expression = new ConstantExpression(0);
                case ICONST_1 -> expression = new ConstantExpression(1);
                case ICONST_2 -> expression = new ConstantExpression(2);
                case ICONST_3 -> expression = new ConstantExpression(3);
                case ICONST_4 -> expression = new ConstantExpression(4);
                case ICONST_5 -> expression = new ConstantExpression(5);
                case FCONST_0 -> expression = new ConstantExpression(0.0f);
                case FCONST_1 -> expression = new ConstantExpression(1.0f);
                case FCONST_2 -> expression = new ConstantExpression(2.0f);
                case DCONST_0 -> expression = new ConstantExpression(0.0);
                case DCONST_1 -> expression = new ConstantExpression(1.0);
                case LCONST_0 -> expression = new ConstantExpression(0L);
                case LCONST_1 -> expression = new ConstantExpression(1L);
                case BIPUSH, SIPUSH -> expression = new ConstantExpression(((IntInsnNode) insn).operand);

                case IINC -> {
                    int var = ((IincInsnNode) insn).var;
                    int increment = ((IincInsnNode) insn).incr;
                    expression = new StoreVarExpression(new BinaryExpression(new LoadVarExpression(Type.INT_TYPE, var), new ConstantExpression(increment), BinaryExpression.Operator.ADD), var);
                }

                case I2F, I2D, I2L -> expression = new CastExpression(stack.pop(), Type.FLOAT_TYPE);

                case FCMPG, FCMPL, LCMP, DCMPG, DCMPL -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    expression = new CompareExpression(left, right);
                }

                case IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE -> expression = new JumpIfExpression(stack, insn.getOpcode());

                case ISTORE, FSTORE, DSTORE, LSTORE, ASTORE -> {
                    Expression value = stack.pop();
                    expression = new StoreVarExpression(value, ((VarInsnNode) insn).var);
                }

                case INVOKESTATIC -> {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
                    int numArgs = Type.getArgumentTypes(methodInsnNode.desc).length;
                    Expression[] args = new Expression[numArgs];
                    for(int i = numArgs - 1; i >= 0; i--){
                        args[i] = stack.pop();
                    }
                    expression = new InvokeStaticExpression(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, args);
                }
                case INVOKEVIRTUAL -> {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
                    int numArgs = Type.getArgumentTypes(methodInsnNode.desc).length + 1;
                    Expression[] args = new Expression[numArgs];
                    for(int i = numArgs - 1; i >= 0; i--){
                        args[i] = stack.pop();
                    }
                    expression = new InvokeVirtualExpression(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, args);
                }
                case GETSTATIC -> {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
                    //Get field value
                    try {
                        Class<?> clazz = Class.forName(fieldInsnNode.owner.replace('/', '.'));
                        Field field = clazz.getDeclaredField(fieldInsnNode.name);
                        field.setAccessible(true);
                        Object obj = field.get(null);
                        expression = new ConstantExpression(obj);
                    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException("Failed to get field value!", e);
                    }
                }
                case FRETURN -> {
                    Expression value = stack.pop();
                    expression = new ReturnExpression(value);
                }

                case GOTO -> expression = new GotoExpression(((JumpInsnNode) insn).label);

                default -> throw new IllegalStateException("Unexpected value: " + JavaTranspiler.opcodeName(insn.getOpcode()));
            }
            if(expression.isStatement()){
                if(stack.size() > 0){
                    throw new IllegalStateException("Stack not empty after statement!");
                }
                statements.add(expression);
            }else{
                stack.push(expression);
            }
        }

        if(stack.size() == 1){
            this.expression = stack.pop();
        }else if(stack.size() > 1){
            throw new IllegalStateException("Stack had size greater than one after parsing node!");
        }
    }

    public Expression[] flatten(){
        return this.flatten(null, new HashMap<>());
    }

    public Expression[] flatten(CFGNode scopeEnd, Map<CFGNode, Supplier<Expression>> loopNodes){
        //TODO: Start by detecting all loops and replace them with a single loop statement

        if(expression == null){
            if(type == NodeType.CODE){
                if(loopNodes.containsKey(normalNext)){
                    //return normal except last GOTO is replaced with expression
                    Expression[] normal = this.statements.toArray(new Expression[0]);

                    normal[normal.length - 1] = loopNodes.get(normalNext).get();
                    return normal;
                }
            }else if(type == NodeType.CONDITIONAL_JUMP){
                if(loopNodes.containsKey(exceptionalNext)){
                    JumpIfExpression jumpIf = (JumpIfExpression) this.statements.get(this.statements.size() - 1);
                    Expression ifExpr = new IfExpression(jumpIf.getCondition(), loopNodes.get(exceptionalNext).get());
                    Expression[] normal = this.normalNext.flatten(scopeEnd, loopNodes);
                    Expression[] full = new Expression[normal.length + 1];
                    full[0] = ifExpr;
                    System.arraycopy(normal, 0, full, 1, normal.length);
                    return full;
                }
            }

            //Check if this is a loop
            boolean isLoop = false;
            CFGNode loopEnd = null;
            Set<CFGNode> upcomingNodes = this.getAll(false, scopeEnd);
            for(CFGNode prev: this.prev){
                if(upcomingNodes.contains(prev)){
                    isLoop = true;
                    loopEnd = prev;
                    break;
                }
            }


            if(isLoop) {
                //This is javac's loop so won't always work
                CFGNode loopExit = this.exceptionalNext;
                if(loopExit == null){
                    throw new IllegalStateException("Loop has no exit! (This may be because it was not compiled by javac. This will be worked on later.)");
                }
                HashMap<CFGNode, Supplier<Expression>> newLoopNodes = new HashMap<>();
                newLoopNodes.put(this, ContinueExpression::new);
                newLoopNodes.put(loopExit, BreakExpression::new);

                Expression[] body = this.flatten(loopEnd, newLoopNodes);
                Expression[] loop = new Expression[1];
                loop[0] = new WhileExpression(Condition.of(true), body);
                return loop;
            }else if(type == NodeType.CONDITIONAL_JUMP){

                CFGNode end = getMeetup(scopeEnd);
                Expression[] ifTrue = exceptionalNext.flatten(end, loopNodes);
                Expression[] ifFalse = normalNext.flatten(end, loopNodes);

                boolean negate = false;

                if(ifTrue.length == 0 && ifFalse.length != 0){
                    negate = true;
                    Expression[] temp = ifTrue;
                    ifTrue = ifFalse;
                    ifFalse = temp;
                }

                JumpIfExpression jumpIf = (JumpIfExpression) this.statements.get(this.statements.size() - 1);
                ActualCompareExpression condition = jumpIf.getCondition();
                if(negate){
                    condition = condition.negate();
                }

                Expression jumpStatement;

                if(ifFalse.length == 0){
                    if(ifTrue[ifTrue.length - 1] instanceof GotoExpression){
                        ifTrue = Arrays.copyOf(ifTrue, ifTrue.length - 1);
                    }
                    jumpStatement = new IfExpression(condition, ifTrue);
                }else{
                    if(ifTrue[ifTrue.length - 1] instanceof GotoExpression){
                        ifTrue = Arrays.copyOf(ifTrue, ifTrue.length - 1);
                    }
                    if(ifFalse[ifFalse.length - 1] instanceof GotoExpression){
                        ifFalse = Arrays.copyOf(ifFalse, ifFalse.length - 1);
                    }
                    jumpStatement = new IfElseExpression(condition, ifTrue, ifFalse);
                }

                Expression[] preJump = this.statements.subList(0, this.statements.size() - 1).toArray(new Expression[0]);
                Expression[] postJump = end == null ? new Expression[0] : end.flatten(scopeEnd, loopNodes);
                Expression[] result = new Expression[preJump.length + 1 + postJump.length];
                System.arraycopy(preJump, 0, result, 0, preJump.length);
                result[preJump.length] = jumpStatement;
                System.arraycopy(postJump, 0, result, preJump.length + 1, postJump.length);
                return result;
            }else if(type == NodeType.CODE && normalNext != scopeEnd){
                Expression[] thisExp = statements.toArray(new Expression[statements.size()]);
                Expression[] nextExp = normalNext.flatten(scopeEnd, loopNodes);
                Expression[] result = new Expression[thisExp.length + nextExp.length];
                System.arraycopy(thisExp, 0, result, 0, thisExp.length);
                System.arraycopy(nextExp, 0, result, thisExp.length, nextExp.length);
                return result;
            }else{
                return statements.toArray(new Expression[0]);
            }
        }else{
            return new Expression[]{expression};
        }
    }

    private CFGNode getMeetup(CFGNode scopeEnd){
        List<CFGNode> possibleMeetups = new ArrayList<>();
        CFGNode current = this.normalNext;

        while(current != null && current != scopeEnd){
            possibleMeetups.add(current);
            if(current.type == NodeType.CONDITIONAL_JUMP){
                current = current.getMeetup(scopeEnd);
            }else{
                current = current.normalNext;
            }
        }

        current = this.exceptionalNext;
        while (current != null && current != scopeEnd) {
            if(possibleMeetups.contains(current)){
                return current;
            }
            if(current.type == NodeType.CONDITIONAL_JUMP){
                current = current.getMeetup(scopeEnd);
            }else{
                current = current.normalNext;
            }
        }

        return scopeEnd;
    }

    public Set<CFGNode> getAll(boolean includeSelf, CFGNode scopeEnd){
        HashSet<CFGNode> all = new HashSet<>();
        getAll(all, includeSelf, scopeEnd);
        return all;
    }

    public void getAll(Set<CFGNode> all, boolean includeSelf, CFGNode scopeEnd){
        if(all.contains(this)){
            return;
        }
        if(includeSelf) {
            all.add(this);
        }

        if(normalNext != null && normalNext != scopeEnd){
            normalNext.getAll(all, true, scopeEnd);
        }

        if(exceptionalNext != null && exceptionalNext != scopeEnd){
            exceptionalNext.getAll(all, true, scopeEnd);
        }
    }

    public void printGraph(PrintStream out){
        Set<CFGNode> done = new HashSet<>();
        printGraph(out, done);

        CFGNode[] arr = done.toArray(CFGNode[]::new);
        Arrays.sort(arr, Comparator.comparingInt(v -> Integer.valueOf(v.id.split(" ")[1])));

        System.out.println("\nNode Info:");
        for(CFGNode node: arr){
            System.out.println(node.id + ": " + node.type + " (" + node.statements.size() + " statements)");
        }

        System.out.println("\nCode:");
        for(CFGNode node: arr){
            System.out.println(node.id + ":");
            for(Expression statement: node.statements){
                System.out.println("\t" + statement.toGLSL(transpiler.getInfo(), 0));
            }
            if(node.expression != null){
                System.out.println("\tValue: " + node.expression.toGLSL(transpiler.getInfo(), 0));
            }
        }
    }

    private void printGraph(PrintStream out, Set<CFGNode> done){
        if(done.contains(this)){
            return;
        }
        done.add(this);

        if(normalNext != null){
            if(exceptionalNext == null){
                out.println(id + " -> " + normalNext.id);
            }else{
                out.println(id + " -> " + normalNext.id + " [NORMAL]");
                out.println(id + " -> " + exceptionalNext.id + " [EXCEPTIONAL]");
            }
            normalNext.printGraph(out, done);
            if(exceptionalNext != null){
                exceptionalNext.printGraph(out, done);
            }
        }
    }

    private Expression pop(Stack<Expression> stack){
        Expression base = stack.pop();
        if(base.isConstant() && !(base instanceof LoadSamplerExpression)){
            Object value = base.getConstantValue();
            return new ConstantExpression(value);
        }else{
            return base;
        }
    }

    //Defined by how it exits the node
    enum NodeType {
        CODE,
        RETURN,
        CONDITIONAL_JUMP
    }
}
