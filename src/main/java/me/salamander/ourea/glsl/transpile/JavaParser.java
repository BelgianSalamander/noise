package me.salamander.ourea.glsl.transpile;

import me.salamander.ourea.glsl.MethodInfo;
import me.salamander.ourea.glsl.transpile.method.StaticMethodResolver;
import me.salamander.ourea.glsl.transpile.tree.comparison.*;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;
import me.salamander.ourea.glsl.transpile.tree.statement.BreakStatement;
import me.salamander.ourea.glsl.transpile.tree.statement.ContinueStatement;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;
import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.util.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

public class JavaParser {
    private final Set<MethodInfo> dependents = new HashSet<>();
    private final Stack<Expression> stack = new Stack<>();
    private final FrameInfo[] code;
    private final TranspilationInfo info = new TranspilationInfo();
    private final CFGNode controlFlowGraph;
    private final MethodNode methodNode;
    private final ClassNode classNode;
    private final Object object;
    private int index = 0;

    private boolean needsIdentity = false;

    @Deprecated
    public JavaParser(NoiseSampler sampler, int dimension) {
        this(sampler, getSampleMethod(sampler, dimension));
    }

    private JavaParser(Object obj, Pair<ClassNode, MethodNode> pair){
        this(obj, pair.first(), pair.second());
    }

    public JavaParser(Object object, ClassNode classNode, MethodNode target) {
        this.methodNode = target;
        this.classNode = classNode;
        this.object = object;

        Interpreter<BasicValue> interpreter = new SimpleVerifier();
        Analyzer<BasicValue> analyzer = new Analyzer<>(interpreter){
            @Override
            protected Frame<BasicValue> newFrame(int numLocals, int numStack) {
                return new ControlFlowFrame<>(numLocals, numStack);
            }

            @Override
            protected Frame<BasicValue> newFrame(Frame<? extends BasicValue> frame) {
                return new ControlFlowFrame<>(frame);
            }

            @Override
            protected void newControlFlowEdge(int insnIndex, int successorIndex) {
                ControlFlowFrame<BasicValue> from = (ControlFlowFrame<BasicValue>) getFrames()[insnIndex];
                ControlFlowFrame<BasicValue> to = (ControlFlowFrame<BasicValue>) getFrames()[successorIndex];

                from.addSuccessor(to);
                to.addPredecessor(from);
            }
        };

        try {
            analyzer.analyze(classNode.name, target);
        } catch (AnalyzerException e) {
            throw new RuntimeException("Failed to analyze NoiseSampler.sample method", e);
        }

        ControlFlowFrame<BasicValue>[] frames = new ControlFlowFrame[analyzer.getFrames().length];
        for(int i = 0; i < frames.length; i++) {
            frames[i] = (ControlFlowFrame<BasicValue>) analyzer.getFrames()[i];
        }

        code = new FrameInfo[frames.length];
        for(int i = 0; i < frames.length; i++) {
            code[i] = new FrameInfo(frames[i], target.instructions.get(i), i);

            for(Frame<BasicValue> prev : frames[i].getPredecessors()) {
                code[i].prev.add(indexOf(frames, prev));
            }

            for(Frame<BasicValue> next : frames[i].getSuccessors()) {
                code[i].next.add(indexOf(frames, next));
            }
        }

        CFGNode[] nodes = new CFGNode[code.length];

        for(int i = 0; i < code.length; i++) {
            nodes[i] = new CFGNode(code[i], this);
        }

        for(int i = 0; i < nodes.length; i++) {
            FrameInfo frame = code[i];
            CFGNode node = nodes[i];

            for(int prev : frame.prev) {
                node.addPrev(nodes[prev]);
            }

            if(frame.next.size() == 0){
                node.type = CFGNode.NodeType.RETURN;
            }else if(frame.next.size() == 1){
                node.type = CFGNode.NodeType.CODE;
                node.normalNext = nodes[frame.next.iterator().next()];
            }else if(frame.next.size() == 2){
                node.type = CFGNode.NodeType.CONDITIONAL_JUMP;
                int normalNext = i + 1;
                node.normalNext = nodes[normalNext];
                Iterator<Integer> it = frame.next.iterator();
                int exceptionalNext = it.next();
                if(exceptionalNext == normalNext){
                    exceptionalNext = it.next();
                }
                node.exceptionalNext = nodes[exceptionalNext];
            }else{
                throw new RuntimeException("Unexpected number of successors: " + frame.next.size());
            }
        }

        controlFlowGraph = nodes[0];
        controlFlowGraph.reduce();

        initInfo();
    }

    public Statement[] flattenGraph() {
        //controlFlowGraph.processLoops();
        Statement[] expressions = controlFlowGraph.flatten();
        return reduce(expressions);
    }

    private Statement[] reduce(Statement[] expressions) {
        for (int i = 0; i < expressions.length; i++) {
            expressions[i] = reduceStatement(expressions[i]);
        }
        return expressions;
    }

    private Statement reduceStatement(Statement statement) {
        if(statement instanceof IfElseStatement ifElse){
           Condition condition = ifElse.getCondition();
            Statement[] ifTrue = reduce(ifElse.getIfTrue());
            Statement[] ifFalse = reduce(ifElse.getIfFalse());

            if(ifTrue.length != 1 && ifFalse.length != 1 || ifTrue.length == 0 || ifFalse.length == 0){
                return new IfElseStatement(condition, ifTrue, ifFalse);
            }

            //Check if ifTrue is duplicated
            if(ifFalse.length == 1){
                Statement check = ifFalse[0];
                if(check instanceof IfElseStatement secondIfElse){
                    if(Arrays.equals(secondIfElse.getIfTrue(), ifTrue)){
                        Condition newCondition = new BinaryBooleanExpression(condition, secondIfElse.getCondition(), BinaryBooleanExpression.Operator.OR);
                        return new IfElseStatement(newCondition, secondIfElse.getIfTrue(), secondIfElse.getIfFalse());
                    }else if(Arrays.equals(secondIfElse.getIfFalse(), ifTrue)){
                        Condition newCondition = new BinaryBooleanExpression(condition, secondIfElse.getCondition().negate(), BinaryBooleanExpression.Operator.OR);
                        return new IfElseStatement(newCondition, secondIfElse.getIfTrue(), secondIfElse.getIfFalse());
                    }
                }
            }

            //TODO: Check if ifFalse is duplicated
        }else if(statement instanceof WhileStatement whileLoop){
            Statement[] body = reduce(whileLoop.getBody());
            Condition condition = whileLoop.getCondition();
            if(body.length > 0){
                if(body[0] instanceof IfStatement ifStatement){
                    if(ifStatement.getBody().length == 1){
                        if(ifStatement.getBody()[0] instanceof BreakStatement){
                            condition = Condition.and(condition, ifStatement.getCondition().negate());
                            Statement[] newBody = new Statement[body.length - 1];
                            System.arraycopy(body, 1, newBody, 0, newBody.length);
                            body = newBody;
                        }
                    }
                }

                if(body[body.length - 1] instanceof ContinueStatement){
                    Statement[] newBody = new Statement[body.length - 1];
                    System.arraycopy(body, 0, newBody, 0, newBody.length);
                    body = newBody;
                }
            }

            return new WhileStatement(condition, body);
        }

        return statement;
    }

    public void printCFG(PrintStream out) {
        controlFlowGraph.printGraph(out);
    }

    public void parseAll(){
        for(CFGNode node: controlFlowGraph.getAll(true, null)){
            node.parse();
        }
    }

    private void initInfo() {
        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "floor", "(F)I", new StaticMethodResolver("floor"));
        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "smoothstep", "(F)F", new StaticMethodResolver("smoothstep"));
        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "lerp", "(FFF)F", new StaticMethodResolver("mix"));
        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "lerp", "(FFFFFF)F", new StaticMethodResolver("lerp"));

        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "cos", "(F)F", new StaticMethodResolver("cos"));
        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "sin", "(F)F", new StaticMethodResolver("sin"));

        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "getGradient", "(III)Lme/salamander/ourea/util/Grad2;", new StaticMethodResolver("getGradient"));

        info.addMethodResolver("me/salamander/ourea/util/Grad2", "dot", "(FF)F", (owner, name, desc, info, args) -> "dot(" + args[0].toGLSL(info, 0) + ", vec2(" + args[1].toGLSL(info, 0) + ", " + args[2].toGLSL(info, 0) + "))");
    }

    public void setRequiresIdentity(){
        needsIdentity = true;
    }

    public boolean requiresIdentity(){
        return needsIdentity;
    }

    public Set<MethodInfo> getDependents() {
        return dependents;
    }

    private static <T> int indexOf(T[] array, T value) {
        for(int i = 0; i < array.length; i++) {
            if(array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the name of a JVM Opcode
     * @param opcode The opcode as an integer
     * @return The mnemonic of the opcode
     */
    static String opcodeName(int opcode) {
        return switch (opcode) {
            case NOP -> "nop";
            case ACONST_NULL -> "aconst_null";
            case ICONST_M1 -> "iconst_m1";
            case ICONST_0 -> "iconst_0";
            case ICONST_1 -> "iconst_1";
            case ICONST_2 -> "iconst_2";
            case ICONST_3 -> "iconst_3";
            case ICONST_4 -> "iconst_4";
            case ICONST_5 -> "iconst_5";
            case LCONST_0 -> "lconst_0";
            case LCONST_1 -> "lconst_1";
            case FCONST_0 -> "fconst_0";
            case FCONST_1 -> "fconst_1";
            case FCONST_2 -> "fconst_2";
            case DCONST_0 -> "dconst_0";
            case DCONST_1 -> "dconst_1";
            case BIPUSH -> "bipush";
            case SIPUSH -> "sipush";
            case LDC -> "ldc";
            case ILOAD -> "iload";
            case LLOAD -> "lload";
            case FLOAD -> "fload";
            case DLOAD -> "dload";
            case ALOAD -> "aload";
            case IALOAD -> "iaload";
            case LALOAD -> "laload";
            case FALOAD -> "faload";
            case DALOAD -> "daload";
            case AALOAD -> "aaload";
            case BALOAD -> "baload";
            case CALOAD -> "caload";
            case SALOAD -> "saload";
            case ISTORE -> "istore";
            case LSTORE -> "lstore";
            case FSTORE -> "fstore";
            case DSTORE -> "dstore";
            case ASTORE -> "astore";
            case IASTORE -> "iastore";
            case LASTORE -> "lastore";
            case FASTORE -> "fastore";
            case DASTORE -> "dastore";
            case AASTORE -> "aastore";
            case BASTORE -> "bastore";
            case CASTORE -> "castore";
            case SASTORE -> "sastore";
            case POP -> "pop";
            case POP2 -> "pop2";
            case DUP -> "dup";
            case DUP_X1 -> "dup_x1";
            case DUP_X2 -> "dup_x2";
            case DUP2 -> "dup2";
            case DUP2_X1 -> "dup2_x1";
            case DUP2_X2 -> "dup2_x2";
            case SWAP -> "swap";
            case IADD -> "iadd";
            case LADD -> "ladd";
            case FADD -> "fadd";
            case DADD -> "dadd";
            case ISUB -> "isub";
            case LSUB -> "lsub";
            case FSUB -> "fsub";
            case DSUB -> "dsub";
            case IMUL -> "imul";
            case LMUL -> "lmul";
            case FMUL -> "fmul";
            case DMUL -> "dmul";
            case IDIV -> "idiv";
            case LDIV -> "ldiv";
            case FDIV -> "fdiv";
            case DDIV -> "ddiv";
            case IREM -> "irem";
            case LREM -> "lrem";
            case FREM -> "frem";
            case DREM -> "drem";
            case INEG -> "ineg";
            case LNEG -> "lneg";
            case FNEG -> "fneg";
            case DNEG -> "dneg";
            case ISHL -> "ishl";
            case LSHL -> "lshl";
            case ISHR -> "ishr";
            case LSHR -> "lshr";
            case IUSHR -> "iushr";
            case LUSHR -> "lushr";
            case IAND -> "iand";
            case LAND -> "land";
            case IOR -> "ior";
            case LOR -> "lor";
            case IXOR -> "ixor";
            case LXOR -> "lxor";
            case IINC -> "iinc";
            case I2L -> "i2l";
            case I2F -> "i2f";
            case I2D -> "i2d";
            case L2I -> "l2i";
            case L2F -> "l2f";
            case L2D -> "l2d";
            case F2I -> "f2i";
            case F2L -> "f2l";
            case F2D -> "f2d";
            case D2I -> "d2i";
            case D2L -> "d2l";
            case D2F -> "d2f";
            case I2B -> "i2b";
            case I2C -> "i2c";
            case I2S -> "i2s";
            case LCMP -> "lcmp";
            case FCMPL -> "fcmpl";
            case FCMPG -> "fcmpg";
            case DCMPL -> "dcmpl";
            case DCMPG -> "dcmpg";
            case IFEQ -> "ifeq";
            case IFNE -> "ifne";
            case IFLT -> "iflt";
            case IFGE -> "ifge";
            case IFGT -> "ifgt";
            case IFLE -> "ifle";
            case IF_ICMPEQ -> "if_icmpeq";
            case IF_ICMPNE -> "if_icmpne";
            case IF_ICMPLT -> "if_icmplt";
            case IF_ICMPGE -> "if_icmpge";
            case IF_ICMPGT -> "if_icmpgt";
            case IF_ICMPLE -> "if_icmple";
            case IF_ACMPEQ -> "if_acmpeq";
            case IF_ACMPNE -> "if_acmpne";
            case GOTO -> "goto";
            case JSR -> "jsr";
            case RET -> "ret";
            case TABLESWITCH -> "tableswitch";
            case LOOKUPSWITCH -> "lookupswitch";
            case IRETURN -> "ireturn";
            case LRETURN -> "lreturn";
            case FRETURN -> "freturn";
            case DRETURN -> "dreturn";
            case ARETURN -> "areturn";
            case RETURN -> "return";
            case GETSTATIC -> "getstatic";
            case PUTSTATIC -> "putstatic";
            case GETFIELD -> "getfield";
            case PUTFIELD -> "putfield";
            case INVOKEVIRTUAL -> "invokevirtual";
            case INVOKESPECIAL -> "invokespecial";
            case INVOKESTATIC -> "invokestatic";
            case INVOKEINTERFACE -> "invokeinterface";
            case INVOKEDYNAMIC -> "invokedynamic";
            case NEW -> "new";
            case NEWARRAY -> "newarray";
            case ANEWARRAY -> "anewarray";
            case ARRAYLENGTH -> "arraylength";
            case ATHROW -> "athrow";
            case CHECKCAST -> "checkcast";
            case INSTANCEOF -> "instanceof";
            case MONITORENTER -> "monitorenter";
            case MONITOREXIT -> "monitorexit";
            default -> "UNKNOWN (" + opcode + ")";
        };
    }

    @Deprecated
    private static ClassNode getClassNode(NoiseSampler sampler) {
        try {
            String classPath = sampler.getClass().getName().replace('.', '/') + ".class";
            InputStream is = ClassLoader.getSystemResourceAsStream(classPath);
            ClassReader classReader = new ClassReader(is);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
            return classNode;
        }catch (IOException e){
            throw new RuntimeException("Failed to load NoiseSampler bytecode", e);
        }
    }

    public TranspilationInfo getInfo() {
        return info;
    }

    public MethodNode getMethod(){
        return methodNode;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public Object getObject() {
        return object;
    }

    @Deprecated
    private static Pair<ClassNode, MethodNode> getSampleMethod(NoiseSampler sampler, int dimension){
        ClassNode classNode = getClassNode(sampler);

        String desc = dimension == 2 ? "(FFI)F" : "(FFFI)F";
        String name = "sample";

        MethodNode target = classNode.methods.stream().filter(m -> m.name.equals(name) && m.desc.equals(desc)).findFirst().orElse(null);
        if (target == null) {
            throw new RuntimeException("Failed to find NoiseSampler.sample method");
        }

        return new Pair<>(classNode, target);
    }

    public class FrameInfo{
        final Frame<BasicValue> frame;
        final AbstractInsnNode insnNode;
        final Set<Integer> next = new HashSet<>(1);
        final Set<Integer> prev = new HashSet<>(1);
        final int instructionIndex;

        public FrameInfo(Frame<BasicValue> frame, AbstractInsnNode insnNode, int instructionIndex) {
            this.frame = frame;
            this.insnNode = insnNode;
            this.instructionIndex = instructionIndex;
        }

        public BasicValue getTop(){
            return frame.getStack(frame.getStackSize() - 1);
        }
    }
}
