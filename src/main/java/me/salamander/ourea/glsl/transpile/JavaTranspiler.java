package me.salamander.ourea.glsl.transpile;

import me.salamander.ourea.glsl.transpile.method.MethodResolver;
import me.salamander.ourea.glsl.transpile.method.StaticMethodResolver;
import me.salamander.ourea.glsl.transpile.tree.*;
import me.salamander.ourea.glsl.transpile.tree.comparison.CompareExpression;
import me.salamander.ourea.modules.NoiseSampler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

public class JavaTranspiler {
    private final Stack<Expression> stack = new Stack<>();
    private final FrameInfo[] code;
    private final TranspilationInfo info = new TranspilationInfo();
    private final Node controlFlowGraph;
    private int index = 0;

    public JavaTranspiler(NoiseSampler sampler, int dimension) {
        ClassNode classNode = getClassNode(sampler);
        String desc = dimension == 2 ? "(FFI)F" : "(FFFI)F";
        String name = "sample";

        MethodNode target = classNode.methods.stream().filter(m -> m.name.equals(name) && m.desc.equals(desc)).findFirst().orElse(null);
        if (target == null) {
            throw new RuntimeException("Failed to find NoiseSampler.sample method");
        }

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

        Node[] nodes = new Node[code.length];

        for(int i = 0; i < code.length; i++) {
            nodes[i] = new Node(code[i]);
        }

        for(int i = 0; i < nodes.length; i++) {
            FrameInfo frame = code[i];
            Node node = nodes[i];

            for(int prev : frame.prev) {
                node.addPrev(nodes[prev]);
            }

            for(int next : frame.next){
                node.addNext(nodes[next]);
            }
        }

        controlFlowGraph = nodes[0];
        controlFlowGraph.reduce();
        controlFlowGraph.printGraph(System.out);

        initInfo();
    }

    public boolean hasNextExpression(){
        if(index >= code.length) return false;
        while(true){
            FrameInfo frame = peek();
            if(frame != null){
                if(frame.insnNode.getOpcode() != -1){
                    return true;
                }
            }

            index++;
            if(index >= code.length){
                return false;
            }
        }
    }

    public Expression nextStatement() {
        Expression top = null;
        while(top == null){
            top = processNext();
        }

        Expression last = top;
        while(!stack.isEmpty()){
            last = processNext();
        }

        assert last.getType() == Type.VOID_TYPE;
        return last;
    }

    private Expression processNext(){
        FrameInfo frame = advance();
        while(frame.insnNode.getOpcode() == -1){
            frame = advance();
        }

        AbstractInsnNode insn = frame.insnNode;
        Expression expression;

        switch (insn.getOpcode()) {
            case ILOAD -> expression = new LoadVarExpression(Type.INT_TYPE, ((VarInsnNode) insn).var);
            case FLOAD -> expression = new LoadVarExpression(Type.FLOAT_TYPE, ((VarInsnNode) insn).var);
            case DLOAD -> expression = new LoadVarExpression(Type.DOUBLE_TYPE, ((VarInsnNode) insn).var);
            case LLOAD -> expression = new LoadVarExpression(Type.LONG_TYPE, ((VarInsnNode) insn).var);
            case ALOAD -> expression = new LoadVarExpression(peek().getTop().getType(), ((VarInsnNode) insn).var);

            case GETFIELD -> expression = new GetFieldExpression(stack.pop(), ((FieldInsnNode) insn).name, ((FieldInsnNode) insn).desc);

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

            case I2F, I2D, I2L -> expression = new CastExpression(stack.pop(), Type.FLOAT_TYPE);

            case FCMPG, FCMPL, LCMP, DCMPG, DCMPL -> {
                Expression right = stack.pop();
                Expression left = stack.pop();
                expression = new CompareExpression(left, right);
            }

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

            default -> throw new IllegalStateException("Unexpected value: " + opcodeName(insn.getOpcode()));
        }

        if(!expression.isStatement()){
            stack.push(expression);
        }

        return expression;
    }

    private FrameInfo advance(){
        if(index == code.length) {
            return null;
        }
        return code[index++];
    }

    private FrameInfo peek(){
        return code[index];
    }

    private void initInfo() {
        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "floor", "(F)I", new StaticMethodResolver("floor"));
        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "smoothstep", "(F)F", new StaticMethodResolver("smoothstep"));
        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "lerp", "(FFF)F", new StaticMethodResolver("mix"));
        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "lerp", "(FFFFFF)F", new StaticMethodResolver("lerp"));

        info.addMethodResolver("me/salamander/ourea/util/MathHelper", "getGradient", "(III)Lme/salamander/ourea/util/Grad2;", new StaticMethodResolver("getGradient"));

        info.addMethodResolver("me/salamander/ourea/util/Grad2", "dot", "(FF)F", (owner, name, desc, info, args) -> "dot(" + args[0].toGLSL(info) + ", vec2(" + args[1].toGLSL(info) + ", " + args[2].toGLSL(info) + "))");
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
    private static String opcodeName(int opcode) {
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

    private ClassNode getClassNode(NoiseSampler sampler) {
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

    private class FrameInfo{
        private final Frame<BasicValue> frame;
        private final AbstractInsnNode insnNode;
        private final Set<Integer> next = new HashSet<>(1);
        private final Set<Integer> prev = new HashSet<>(1);
        private final int instructionIndex;

        public FrameInfo(Frame<BasicValue> frame, AbstractInsnNode insnNode, int instructionIndex) {
            this.frame = frame;
            this.insnNode = insnNode;
            this.instructionIndex = instructionIndex;;
        }

        public BasicValue getTop(){
            return frame.getStack(frame.getStackSize() - 1);
        }
    }

    private class Node{
        private static int nextId = 0;
        private final List<FrameInfo> frames = new ArrayList<>();
        private final Set<Node> prev = new HashSet<>();
        private final Set<Node> next = new HashSet<>();
        private final String id;

        public Node(FrameInfo frameInfo){
            frames.add(frameInfo);
            id = "node " + nextId++;
        }

        public void addPrev(Node node){
            prev.add(node);
        }

        public void addNext(Node node){
            next.add(node);
        }

        public void reduce(){
            while (mergeWithNext()){
                //Do nothing
            }

            for(Node node: next){
                node.reduce();
            }
        }

        public boolean mergeWithNext(){
            if(next.size() == 1){
                Node nextNode = next.iterator().next();
                frames.addAll(nextNode.frames);
                this.next.clear();
                this.next.addAll(nextNode.next);
                return true;
            }
            return false;
        }

        public void printGraph(PrintStream out){
            printGraph(out, new HashSet<>());
        }

        private void printGraph(PrintStream out, Set<Node> done){
            if(done.contains(this)){
                return;
            }
            done.add(this);
            for(Node node: next){
                out.println(id + " -> " + node.id);
            }

            for(Node node: next){
                node.printGraph(out, done);
            }
        }
    }
}
