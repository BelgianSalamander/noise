package me.salamander.ourea.glsl.transpile;

import me.salamander.ourea.glsl.GLSLCompiler;
import me.salamander.ourea.glsl.MethodInfo;
import me.salamander.ourea.glsl.transpile.tree.*;
import me.salamander.ourea.glsl.transpile.tree.comparison.*;
import me.salamander.ourea.glsl.transpile.tree.expression.*;
import me.salamander.ourea.glsl.transpile.tree.statement.*;
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
    public static final Type UNRESOLVED_TYPE = Type.getObjectType("me/salamander/fake/gfe2674DHUEyhdfe784trtzagf/yy$y3r");

    private static int nextId = 0;
    private final List<JavaParser.FrameInfo> frames = new ArrayList<>();
    private final Set<CFGNode> prev = new HashSet<>();
    private final JavaParser transpiler;
    NodeType type;

    private final List<Statement> statements = new ArrayList<>();
    private Expression residualValue;

    CFGNode normalNext;
    CFGNode exceptionalNext;

    private final String id;

    public CFGNode(JavaParser.FrameInfo frameInfo, JavaParser transpiler){
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
        Stack<Expression> stack = new Stack<>(){
            @Override
            public Expression pop() {
                if(!isEmpty()) {
                    return simplify(super.pop());
                }

                if(CFGNode.this.prev.size() == 0){
                    throw new RuntimeException("No previous nodes");
                }

                return new PrecedingValueExpression();
            }
        };

        int k = -1;
        for(JavaParser.FrameInfo frameInfo : frames){
            k++;
            CodeFragment<?> fragment = null;
            AbstractInsnNode insn = frameInfo.insnNode;
            if(insn.getOpcode() == -1){
                continue;
            }
            switch (insn.getOpcode()){
                case ILOAD -> {
                    fragment = new LoadVarExpression(Type.INT_TYPE, ((VarInsnNode) insn).var);
                    transpiler.addVar(((VarInsnNode) insn).var, Type.INT_TYPE);
                }
                case FLOAD -> {
                    fragment = new LoadVarExpression(Type.FLOAT_TYPE, ((VarInsnNode) insn).var);
                    transpiler.addVar(((VarInsnNode) insn).var, Type.FLOAT_TYPE);
                }
                case DLOAD -> {
                    fragment = new LoadVarExpression(Type.DOUBLE_TYPE, ((VarInsnNode) insn).var);
                    transpiler.addVar(((VarInsnNode) insn).var, Type.DOUBLE_TYPE);
                }
                case LLOAD -> {
                    fragment = new LoadVarExpression(Type.LONG_TYPE, ((VarInsnNode) insn).var);
                    transpiler.addVar(((VarInsnNode) insn).var, Type.LONG_TYPE);
                }
                case ALOAD -> {
                    VarInsnNode varNode = (VarInsnNode) insn;

                    if (varNode.var == 0 && transpiler.getObject() != null) {
                        fragment = new ConstantExpression(transpiler.getObject());
                        break;
                    }

                    //Get type
                    Type type;
                    if(k != frames.size() - 1){
                        type = frames.get(k + 1).getTop().getType();
                    }else{
                        //We can get it from next
                        type = normalNext.frames.get(0).getTop().getType();
                    }
                    fragment = new LoadVarExpression(type, varNode.var);
                    transpiler.addVar(varNode.var, type);
                }

                case GETFIELD -> {
                    fragment = new GetFieldExpression(stack.pop(), ((FieldInsnNode) insn).name, ((FieldInsnNode) insn).desc);
                    GetFieldExpression expression = (GetFieldExpression)  fragment;
                    if(expression.isConstant()){
                        if(expression.getExpression() instanceof GetFieldExpression f){
                            if(!(f.getConstantValue() instanceof Number)){
                                transpiler.addConstant(f.getConstantValue());
                            }
                        }
                    }
                }

                case IADD, LADD, FADD, DADD -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.ADD);
                }
                case ISUB, LSUB, FSUB, DSUB -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.SUB);
                }
                case IMUL, LMUL, FMUL, DMUL -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.MUL);
                }
                case IDIV, LDIV, FDIV, DDIV -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.DIV);
                }
                case IREM, LREM, FREM, DREM -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.MOD);
                }
                case IXOR, LXOR -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.XOR);
                }
                case IAND, LAND -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.AND);
                }
                case IOR, LOR -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.OR);
                }
                case ISHL, LSHL -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.SHL);
                }
                case ISHR, LSHR -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.SHR);
                }
                case IUSHR, LUSHR -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new BinaryExpression(left, right, BinaryExpression.Operator.USHR);
                }

                case LDC -> fragment = new ConstantExpression(((LdcInsnNode) insn).cst);
                case ICONST_M1 -> fragment = new ConstantExpression(-1);
                case ICONST_0 -> fragment = new ConstantExpression(0);
                case ICONST_1 -> fragment = new ConstantExpression(1);
                case ICONST_2 -> fragment = new ConstantExpression(2);
                case ICONST_3 -> fragment = new ConstantExpression(3);
                case ICONST_4 -> fragment = new ConstantExpression(4);
                case ICONST_5 -> fragment = new ConstantExpression(5);
                case FCONST_0 -> fragment = new ConstantExpression(0.0f);
                case FCONST_1 -> fragment = new ConstantExpression(1.0f);
                case FCONST_2 -> fragment = new ConstantExpression(2.0f);
                case DCONST_0 -> fragment = new ConstantExpression(0.0);
                case DCONST_1 -> fragment = new ConstantExpression(1.0);
                case LCONST_0 -> fragment = new ConstantExpression(0L);
                case LCONST_1 -> fragment = new ConstantExpression(1L);
                case BIPUSH, SIPUSH -> fragment = new ConstantExpression(((IntInsnNode) insn).operand);

                case IINC -> {
                    int var = ((IincInsnNode) insn).var;
                    int increment = ((IincInsnNode) insn).incr;
                    fragment = new StoreVarStatement(new BinaryExpression(new LoadVarExpression(Type.INT_TYPE, var), new ConstantExpression(increment), BinaryExpression.Operator.ADD), var);
                }

                case I2F, L2F, D2F -> fragment = new CastExpression(stack.pop(), Type.FLOAT_TYPE);
                case I2D, L2D, F2D -> fragment = new CastExpression(stack.pop(), Type.DOUBLE_TYPE);
                case I2L, F2L, D2L -> fragment = new CastExpression(stack.pop(), Type.LONG_TYPE);
                case F2I, D2I, L2I -> fragment = new CastExpression(stack.pop(), Type.INT_TYPE);

                //case I2B, I2C, I2S -> expression = new CastExpression(stack.pop(), Type.INT_TYPE); //Need to trim

                case FCMPG, FCMPL, LCMP, DCMPG, DCMPL -> {
                    Expression right = stack.pop();
                    Expression left = stack.pop();
                    fragment = new CompareExpression(left, right);
                }

                case IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE -> fragment = new JumpIfEqualStatement(stack, insn.getOpcode());

                case ISTORE, FSTORE, DSTORE, LSTORE, ASTORE -> {
                    Expression value = stack.pop();
                    fragment = new StoreVarStatement(value, ((VarInsnNode) insn).var);
                    transpiler.addVar(((VarInsnNode) insn).var, value.getType());
                }

                case INVOKESTATIC -> {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insn;

                    MethodInfo methodInfo = new MethodInfo(null, methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, true);
                    transpiler.getDependents().add(methodInfo);

                    int numArgs = Type.getArgumentTypes(methodInsnNode.desc).length;
                    Expression[] args = new Expression[numArgs];
                    for(int i = numArgs - 1; i >= 0; i--){
                        args[i] = stack.pop();
                    }
                    fragment = new InvokeStaticExpression(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, args);
                }
                case INVOKEVIRTUAL -> {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
                    int numArgs = Type.getArgumentTypes(methodInsnNode.desc).length + 1;
                    Expression[] args = new Expression[numArgs];
                    for(int i = numArgs - 1; i >= 0; i--){
                        args[i] = stack.pop();
                    }

                    Object methodCaller;
                    String ownerName = methodInsnNode.owner;
                    if(args[0].isConstant()){
                        methodCaller = args[0].getConstantValue();
                        ownerName = methodCaller.getClass().getName().replace('.', '/');
                    }else{
                        methodCaller = null;
                    }

                    MethodInfo methodInfo = new MethodInfo(methodCaller, ownerName, methodInsnNode.name, methodInsnNode.desc, false);

                    if(args.length == 1) {
                        try {
                            Class<?> clazz = Class.forName(args[0].getType().getClassName());
                            Class<?> returnClazz = GLSLCompiler.getClass(Type.getReturnType(methodInsnNode.desc));

                            Field field = clazz.getDeclaredField(methodInsnNode.name);
                            if(field.getType() == returnClazz) {
                                fragment = new GetFieldExpression(args[0], field.getName(), field.getType().descriptorString());
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchFieldException e) {
                            // This isn't a field accessor
                        }
                    }

                    if(fragment == null) {
                        transpiler.getDependents().add(methodInfo);
                        fragment = new InvokeVirtualExpression(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, false, args);
                    }
                }
                case INVOKEINTERFACE -> {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
                    int numArgs = Type.getArgumentTypes(methodInsnNode.desc).length + 1;
                    Expression[] args = new Expression[numArgs];
                    for(int i = numArgs - 1; i >= 0; i--){
                        args[i] = stack.pop();
                    }

                    Object methodCaller;
                    String ownerName = methodInsnNode.owner;
                    if(args[0].isConstant()){
                        methodCaller = args[0].getConstantValue();
                        ownerName = methodCaller.getClass().getName().replace('.', '/');
                    }else{
                        methodCaller = null;
                    }

                    MethodInfo methodInfo = new MethodInfo(methodCaller, ownerName, methodInsnNode.name, methodInsnNode.desc, false);
                    transpiler.getDependents().add(methodInfo);

                    /*if(methodInsnNode.name.equals("sample")){
                        Expression arg = args[0];
                        if(arg instanceof LoadSamplerExpression samplerLoad){
                            if(methodInsnNode.desc.equals("(FFI)F")){
                                expression = new SamplerSampleExpression(samplerLoad.getConstantValue(), args[1], args[2], args[3]);
                                break;
                            }else if(methodInsnNode.desc.equals("(FFFI)F")){
                                expression = new SamplerSampleExpression(samplerLoad.getConstantValue(), args[1], args[2], args[3], args[4]);
                                break;
                            }
                        }
                    }*/

                    fragment = new InvokeVirtualExpression(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, true, args);
                }
                case GETSTATIC -> {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
                    //Get field value
                    try {
                        Class<?> clazz = Class.forName(fieldInsnNode.owner.replace('/', '.'));
                        Field field = clazz.getDeclaredField(fieldInsnNode.name);
                        field.setAccessible(true);
                        Object obj = field.get(null);
                        if(!(obj instanceof Number) && !(obj instanceof NoiseSampler)){
                            transpiler.addConstant(obj);
                        }
                        fragment = new ConstantExpression(obj);
                    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException("Failed to get field value!", e);
                    }
                }
                case ARETURN, IRETURN, FRETURN, LRETURN, DRETURN -> {
                    Expression value = stack.pop();
                    fragment = new ReturnStatement(value);
                }

                case ARRAYLENGTH -> {
                    Expression array = stack.pop();
                    if(!array.isConstant()){
                        throw new RuntimeException("Array must be constant!"); //Will be improved later
                    }

                    Object arrayObj = array.getConstantValue();
                    if(!(arrayObj instanceof Object[])){
                        throw new RuntimeException("Array must be an array!"); //Will be improved later
                    }

                    fragment = new ConstantExpression(((Object[])arrayObj).length);
                }

                case AALOAD, BALOAD, CALOAD, SALOAD, IALOAD, LALOAD, FALOAD, DALOAD -> {
                    Expression index = stack.pop();
                    Expression array = stack.pop();

                    fragment = new ArrayLoadExpression(array, index);
                }

                case IFNULL, IFNONNULL -> {
                    Expression value = stack.pop();
                    transpiler.setNullable(value.getType());

                    boolean invert = insn.getOpcode() == IFNONNULL;
                    fragment = new JumpIfNullStatement(value, invert);
                }

                case GOTO -> fragment = new GotoStatement(((JumpInsnNode) insn).label);

                default -> {
                    MethodNode method = transpiler.getMethod();
                    throw new IllegalStateException("Unexpected value: " + JavaParser.opcodeName(insn.getOpcode()) + " while parsing " + transpiler.getClassNode().name + "#" + method.name + method.desc + ". Maybe forgot to add ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES flags when loading the class?");
                }
            }
            if(fragment instanceof Statement statement){
                if(stack.size() > 0){
                    //throw new IllegalStateException("Stack not empty after statement!");
                    System.out.println("Warning: Stack not empty after statement! (This probably means it is part of a ternary expression)");
                }
                statements.add(statement);
            }else{
                stack.push((Expression) fragment);
            }
        }

        if(stack.size() == 1){
            this.residualValue = stack.pop();
        }else if(stack.size() > 1){
            throw new IllegalStateException("Stack had size greater than one after parsing node!");
        }
    }

    public Statement[] flatten(){
        return this.flatten(null, new HashMap<>());
    }

    public Statement[] flatten(CFGNode scopeEnd, Map<CFGNode, Supplier<Statement>> loopNodes){
        //TODO: Start by detecting all loops and replace them with a single loop statement

        if(residualValue == null){
            if(type == NodeType.CODE){
                if(loopNodes.containsKey(normalNext)){
                    //return normal except last GOTO is replaced with expression
                    Statement[] normal = this.statements.toArray(new Statement[0]);

                    normal[normal.length - 1] = loopNodes.get(normalNext).get();
                    return normal;
                }
            }else if(type == NodeType.CONDITIONAL_JUMP){
                if(loopNodes.containsKey(exceptionalNext)){
                    ConditionalJump jumpIf = (ConditionalJump) this.statements.get(this.statements.size() - 1);
                    Statement ifExpr = new IfStatement(jumpIf.getCondition(), loopNodes.get(exceptionalNext).get());
                    Statement[] normal = this.normalNext.flatten(scopeEnd, loopNodes);
                    Statement[] full = new Statement[normal.length + 1];
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
                HashMap<CFGNode, Supplier<Statement>> newLoopNodes = new HashMap<>();
                newLoopNodes.put(this, ContinueStatement::new);
                newLoopNodes.put(loopExit, BreakStatement::new);

                Statement[] body = this.flatten(loopEnd, newLoopNodes);
                Statement loop = new WhileStatement(Condition.of(true), body);

                Statement[] next = loopExit.flatten(scopeEnd, loopNodes);
                Statement[] full = new Statement[next.length + 1];
                full[0] = loop;
                System.arraycopy(next, 0, full, 1, next.length);

                return full;
            }else if(type == NodeType.CONDITIONAL_JUMP){

                CFGNode end = getMeetup(scopeEnd);
                Statement[] ifTrue = exceptionalNext.flatten(end, loopNodes);
                Statement[] ifFalse = normalNext.flatten(end, loopNodes);

                boolean negate = false;

                if(ifTrue.length == 0 && ifFalse.length != 0){
                    negate = true;
                    Statement[] temp = ifTrue;
                    ifTrue = ifFalse;
                    ifFalse = temp;
                }

                ConditionalJump jumpIf = (ConditionalJump) this.statements.get(this.statements.size() - 1);
                Condition condition = jumpIf.getCondition();
                if(negate){
                    condition = condition.negate();
                }

                Statement jumpStatement;

                if(ifFalse.length == 0){
                    if(ifTrue[ifTrue.length - 1] instanceof GotoStatement){
                        ifTrue = Arrays.copyOf(ifTrue, ifTrue.length - 1);
                    }
                    jumpStatement = new IfStatement(condition, ifTrue);
                }else{
                    if(ifTrue[ifTrue.length - 1] instanceof GotoStatement){
                        ifTrue = Arrays.copyOf(ifTrue, ifTrue.length - 1);
                    }
                    if(ifFalse[ifFalse.length - 1] instanceof GotoStatement){
                        ifFalse = Arrays.copyOf(ifFalse, ifFalse.length - 1);
                    }
                    jumpStatement = new IfElseStatement(condition, ifTrue, ifFalse);
                }

                boolean isTernary = end.prev.iterator().next().residualValue != null;
                Statement[] preJump = this.statements.subList(0, this.statements.size() - 1).toArray(new Statement[0]);

                if(!isTernary) {
                    Statement[] postJump = end.flatten(scopeEnd, loopNodes);
                    Statement[] result = new Statement[preJump.length + 1 + postJump.length];
                    System.arraycopy(preJump, 0, result, 0, preJump.length);
                    result[preJump.length] = jumpStatement;
                    System.arraycopy(postJump, 0, result, preJump.length + 1, postJump.length);
                    return result;
                }else{
                    assert ifTrue.length == 1 && ifFalse.length == 1;



                    Expression ternary = new TernaryExpression(condition, ((ExpressionPseudoStatement) ifTrue[0]).expression, ((ExpressionPseudoStatement) ifFalse[0]).expression);

                    Statement[] postJump = end.flatten(scopeEnd, loopNodes);
                    if(postJump.length > 0){
                        postJump[0] = postJump[0].resolvePrecedingExpression(ternary);
                    }
                    Statement[] result = new Statement[preJump.length + postJump.length];
                    System.arraycopy(preJump, 0, result, 0, preJump.length);
                    System.arraycopy(postJump, 0, result, preJump.length, postJump.length);
                    return result;
                }
            }else if(type == NodeType.CODE && normalNext != scopeEnd){
                Statement[] thisExp = statements.toArray(new Statement[0]);
                Statement[] nextExp = normalNext.flatten(scopeEnd, loopNodes);
                Statement[] result = new Statement[thisExp.length + nextExp.length];
                System.arraycopy(thisExp, 0, result, 0, thisExp.length);
                System.arraycopy(nextExp, 0, result, thisExp.length, nextExp.length);
                return result;
            }else{
                return statements.toArray(new Statement[0]);
            }
        }else{
            return new Statement[]{
                    new ExpressionPseudoStatement(this.residualValue)
            };
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
            for(Statement statement: node.statements){
                System.out.println("\t" + statement.toGLSL(transpiler.getInfo(), 0));
            }
            if(node.residualValue != null){
                System.out.println("\tValue: " + node.residualValue.toGLSL(transpiler.getInfo(), 0));
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

    private Expression simplify(Expression base){
        if(base.isConstant() && !(base instanceof LoadSamplerExpression)){
            Object value = base.getConstantValue();
            if(value instanceof NoiseSampler sampler){
                return new LoadSamplerExpression(sampler);
            }
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
