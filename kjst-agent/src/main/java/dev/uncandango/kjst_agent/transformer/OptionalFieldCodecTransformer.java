package dev.uncandango.kjst_agent.transformer;

import dev.uncandango.kjst_agent.KJSTAsmUtils;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class OptionalFieldCodecTransformer extends BaseTransformer {
    public OptionalFieldCodecTransformer(String targetClass) {
        super(targetClass);
    }

    @Override
    public ClassNode transform(ClassLoader loader, String className, ClassNode classNode) {
        var targetMethodNode = classNode.methods.removeIf(methodNode -> methodNode.name.equals("decode") && methodNode.desc.equals("(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/MapLike;)Lcom/mojang/serialization/DataResult;"));

        if (!targetMethodNode) {
            return classNode;
        }

        {
            var methodVisitor = classNode.visitMethod(ACC_PUBLIC | ACC_SYNCHRONIZED, "decode", "(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/MapLike;)Lcom/mojang/serialization/DataResult;", "<T:Ljava/lang/Object;>(Lcom/mojang/serialization/DynamicOps<TT;>;Lcom/mojang/serialization/MapLike<TT;>;)Lcom/mojang/serialization/DataResult<Ljava/util/Optional<TA;>;>;", null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(25, label0);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "name", "Ljava/lang/String;");
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "com/mojang/serialization/MapLike", "get", "(Ljava/lang/String;)Ljava/lang/Object;", true);
            methodVisitor.visitVarInsn(ASTORE, 3);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(26, label1);
            methodVisitor.visitFieldInsn(GETSTATIC, "com/mojang/serialization/Decoder", "currentDepth", "Ljava/util/concurrent/atomic/AtomicInteger;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicInteger", "incrementAndGet", "()I", false);
            methodVisitor.visitVarInsn(ISTORE, 4);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(28, label2);
            methodVisitor.visitVarInsn(ALOAD, 3);
            Label label3 = new Label();
            methodVisitor.visitJumpInsn(IFNONNULL, label3);
            Label label4 = new Label();
            methodVisitor.visitLabel(label4);
            methodVisitor.visitLineNumber(29, label4);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/util/Optional", "empty", "()Ljava/util/Optional;", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "com/mojang/serialization/DataResult", "success", "(Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", true);
            methodVisitor.visitVarInsn(ASTORE, 5);
            Label label5 = new Label();
            methodVisitor.visitLabel(label5);
            methodVisitor.visitLineNumber(30, label5);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "elementCodec", "Lcom/mojang/serialization/Codec;");
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "name", "Ljava/lang/String;");
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "com/mojang/serialization/Decoder", "kjst_agent$fireCodecParsedEvent", "(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Ljava/lang/String;Ljava/lang/Object;Lcom/mojang/serialization/DataResult;I)V", true);
            Label label6 = new Label();
            methodVisitor.visitLabel(label6);
            methodVisitor.visitLineNumber(31, label6);
            methodVisitor.visitFieldInsn(GETSTATIC, "com/mojang/serialization/Decoder", "currentDepth", "Ljava/util/concurrent/atomic/AtomicInteger;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicInteger", "decrementAndGet", "()I", false);
            methodVisitor.visitInsn(POP);
            Label label7 = new Label();
            methodVisitor.visitLabel(label7);
            methodVisitor.visitLineNumber(32, label7);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLineNumber(34, label3);
            methodVisitor.visitFrame(F_APPEND, 2, new Object[]{"java/lang/Object", INTEGER}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "elementCodec", "Lcom/mojang/serialization/Codec;");
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "com/mojang/serialization/Codec", "parse", "(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", true);
            methodVisitor.visitVarInsn(ASTORE, 6);
            Label label8 = new Label();
            methodVisitor.visitLabel(label8);
            methodVisitor.visitLineNumber(35, label8);
            methodVisitor.visitFieldInsn(GETSTATIC, "com/mojang/serialization/Decoder", "currentDepth", "Ljava/util/concurrent/atomic/AtomicInteger;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicInteger", "decrementAndGet", "()I", false);
            methodVisitor.visitInsn(POP);
            Label label9 = new Label();
            methodVisitor.visitLabel(label9);
            methodVisitor.visitLineNumber(36, label9);
            methodVisitor.visitVarInsn(ALOAD, 6);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "com/mojang/serialization/DataResult", "isError", "()Z", true);
            Label label10 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label10);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "lenient", "Z");
            methodVisitor.visitJumpInsn(IFEQ, label10);
            Label label11 = new Label();
            methodVisitor.visitLabel(label11);
            methodVisitor.visitLineNumber(37, label11);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/util/Optional", "empty", "()Ljava/util/Optional;", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "com/mojang/serialization/DataResult", "success", "(Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", true);
            methodVisitor.visitVarInsn(ASTORE, 5);
            Label label12 = new Label();
            methodVisitor.visitLabel(label12);
            methodVisitor.visitLineNumber(38, label12);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "elementCodec", "Lcom/mojang/serialization/Codec;");
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "name", "Ljava/lang/String;");
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "com/mojang/serialization/Decoder", "kjst_agent$fireCodecParsedEvent", "(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Ljava/lang/String;Ljava/lang/Object;Lcom/mojang/serialization/DataResult;I)V", true);
            Label label13 = new Label();
            methodVisitor.visitLabel(label13);
            methodVisitor.visitLineNumber(39, label13);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(label10);
            methodVisitor.visitLineNumber(41, label10);
            methodVisitor.visitFrame(F_APPEND, 2, new Object[]{TOP, "com/mojang/serialization/DataResult"}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 6);
            methodVisitor.visitInvokeDynamicInsn("apply", "()Ljava/util/function/Function;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), new Object[]{Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"), new Handle(H_INVOKESTATIC, "java/util/Optional", "of", "(Ljava/lang/Object;)Ljava/util/Optional;", false), Type.getType("(Ljava/lang/Object;)Ljava/util/Optional;")});
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "com/mojang/serialization/DataResult", "map", "(Ljava/util/function/Function;)Lcom/mojang/serialization/DataResult;", true);
            methodVisitor.visitVarInsn(ALOAD, 6);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "com/mojang/serialization/DataResult", "resultOrPartial", "()Ljava/util/Optional;", true);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "com/mojang/serialization/DataResult", "setPartial", "(Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", true);
            methodVisitor.visitVarInsn(ASTORE, 5);
            Label label14 = new Label();
            methodVisitor.visitLabel(label14);
            methodVisitor.visitLineNumber(42, label14);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "elementCodec", "Lcom/mojang/serialization/Codec;");
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "name", "Ljava/lang/String;");
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "com/mojang/serialization/Decoder", "kjst_agent$fireCodecParsedEvent", "(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Ljava/lang/String;Ljava/lang/Object;Lcom/mojang/serialization/DataResult;I)V", true);
            Label label15 = new Label();
            methodVisitor.visitLabel(label15);
            methodVisitor.visitLineNumber(43, label15);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitInsn(ARETURN);
            Label label16 = new Label();
            methodVisitor.visitLabel(label16);
            methodVisitor.visitLocalVariable("$result", "Lcom/mojang/serialization/DataResult;", "Lcom/mojang/serialization/DataResult<Ljava/util/Optional<TA;>;>;", label5, label3, 5);
            methodVisitor.visitLocalVariable("$result", "Lcom/mojang/serialization/DataResult;", "Lcom/mojang/serialization/DataResult<Ljava/util/Optional<TA;>;>;", label12, label10, 5);
            methodVisitor.visitLocalVariable("this", "Lcom/mojang/serialization/codecs/OptionalFieldCodec;", "Lcom/mojang/serialization/codecs/OptionalFieldCodec<TA;>;", label0, label16, 0);
            methodVisitor.visitLocalVariable("ops", "Lcom/mojang/serialization/DynamicOps;", "Lcom/mojang/serialization/DynamicOps<TT;>;", label0, label16, 1);
            methodVisitor.visitLocalVariable("input", "Lcom/mojang/serialization/MapLike;", "Lcom/mojang/serialization/MapLike<TT;>;", label0, label16, 2);
            methodVisitor.visitLocalVariable("value", "Ljava/lang/Object;", "TT;", label1, label16, 3);
            methodVisitor.visitLocalVariable("innerDepth", "I", null, label2, label16, 4);
            methodVisitor.visitLocalVariable("$result", "Lcom/mojang/serialization/DataResult;", "Lcom/mojang/serialization/DataResult<Ljava/util/Optional<TA;>;>;", label14, label16, 5);
            methodVisitor.visitLocalVariable("parsed", "Lcom/mojang/serialization/DataResult;", "Lcom/mojang/serialization/DataResult<TA;>;", label8, label16, 6);
            methodVisitor.visitMaxs(6, 7);
            methodVisitor.visitEnd();
        }

//        var instructions = targetMethodNode.instructions;
//        var lastInst = instructions.getLast();
//
//        var firstReturnValueNull = KJSTAsmUtils.findNthInstruction(targetMethodNode, ARETURN, 0);
//        var firstReturnInstList = new InsnList();
//
//        firstReturnInstList.add(new VarInsnNode(ASTORE,5));
//
//        // Loads "this" to be called with getClass()
//        firstReturnInstList.add(new VarInsnNode(ALOAD, 0));
//        firstReturnInstList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
//        // Loads "this" to be called with elementCodec
//        firstReturnInstList.add(new VarInsnNode(ALOAD, 0));
//        firstReturnInstList.add(new FieldInsnNode(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "elementCodec", "Lcom/mojang/serialization/Codec;"));
//        // Loads "this" to be called with name
//        firstReturnInstList.add(new VarInsnNode(ALOAD, 0));
//        firstReturnInstList.add(new FieldInsnNode(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "name", "Ljava/lang/String;"));
//        // Loads local "value"
//        firstReturnInstList.add(new VarInsnNode(ALOAD,3));
//        // Loads DataResult from $result
//        firstReturnInstList.add(new VarInsnNode(ALOAD, 5));
//
//        firstReturnInstList.add(new FieldInsnNode(GETSTATIC, "com/mojang/serialization/Decoder", "currentDepth", "Ljava/util/concurrent/atomic/AtomicInteger;"));
//        firstReturnInstList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicInteger", "incrementAndGet", "()I", false));
//
//        // Calls kjst_agent$fireCodecParsedEvent
//        firstReturnInstList.add(new MethodInsnNode(INVOKESTATIC, "com/mojang/serialization/Decoder", "kjst_agent$fireCodecParsedEvent", "(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Ljava/lang/String;Ljava/lang/Object;Lcom/mojang/serialization/DataResult;I)V", true)) ;
//        // Loads DataResult from $result
//        firstReturnInstList.add(new VarInsnNode(ALOAD, 5));
//
//        instructions.insertBefore(firstReturnValueNull, firstReturnInstList);
//
//        var lastReturn = KJSTAsmUtils.findFirstInstructionBefore(targetMethodNode, ARETURN, instructions.indexOf(lastInst));
//
//        var getTopLocalVariable = targetMethodNode.localVariables.get(3);
//        targetMethodNode.localVariables.add(new LocalVariableNode("$result", "Lcom/mojang/serialization/DataResult;", "Lcom/mojang/serialization/DataResult<Ljava/util/Optional<TA;>;>;", getTopLocalVariable.start, getTopLocalVariable.end, 5));
//        targetMethodNode.localVariables.add(new LocalVariableNode("innerDepth", "I", null, getTopLocalVariable.start, getTopLocalVariable.end, 6));
//
//        var parseMethodInst = KJSTAsmUtils.findNthInstruction(targetMethodNode, INVOKEINTERFACE, 1);
//        var depthInstructions = new InsnList();
//        depthInstructions.add(new FieldInsnNode(GETSTATIC, "com/mojang/serialization/Decoder", "currentDepth", "Ljava/util/concurrent/atomic/AtomicInteger;"));
//        depthInstructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicInteger", "incrementAndGet", "()I", false));
//        depthInstructions.add(new VarInsnNode(ISTORE, 6));
//        instructions.insertBefore(parseMethodInst, depthInstructions);
//
//        var storePaseResult = parseMethodInst.getNext();
//
//        var depthDecrementInstructions = new InsnList();
//        depthDecrementInstructions.add(new FieldInsnNode(GETSTATIC, "com/mojang/serialization/Decoder", "currentDepth", "Ljava/util/concurrent/atomic/AtomicInteger;"));
//        depthDecrementInstructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicInteger", "decrementAndGet", "()I", false));
//        depthDecrementInstructions.add(new InsnNode(POP));
//
//        instructions.insert(storePaseResult, depthDecrementInstructions);
//
//        // Ldev/uncandango/kubejstweaks/mixin/asm/ASMHooks;kjst_agent$fireCodecParsedEvent(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Lcom/google/gson/JsonElement;Lcom/mojang/serialization/DataResult;)V
//        var newInstList = new InsnList();
//        // Stores on DataResult to $result
//        newInstList.add(new VarInsnNode(ASTORE,5));
//
//        // Loads "this" to be called with getClass()
//        newInstList.add(new VarInsnNode(ALOAD, 0));
//        newInstList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
//        // Loads "this" to be called with elementCodec
//        newInstList.add(new VarInsnNode(ALOAD, 0));
//        newInstList.add(new FieldInsnNode(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "elementCodec", "Lcom/mojang/serialization/Codec;"));
//        // Loads "this" to be called with name
//        newInstList.add(new VarInsnNode(ALOAD, 0));
//        newInstList.add(new FieldInsnNode(GETFIELD, "com/mojang/serialization/codecs/OptionalFieldCodec", "name", "Ljava/lang/String;"));
//        // Loads local "value"
//        newInstList.add(new VarInsnNode(ALOAD,3));
//        // Loads DataResult from $result
//        newInstList.add(new VarInsnNode(ALOAD, 5));
//
//        newInstList.add(new VarInsnNode(ILOAD, 6));
//
//        // Calls kjst_agent$fireCodecParsedEvent
//        newInstList.add(new MethodInsnNode(INVOKESTATIC, "com/mojang/serialization/Decoder", "kjst_agent$fireCodecParsedEvent", "(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Ljava/lang/String;Ljava/lang/Object;Lcom/mojang/serialization/DataResult;I)V", true)) ;
//        // Loads DataResult from $result
//        newInstList.add(new VarInsnNode(ALOAD, 5));
//
//        instructions.insertBefore(lastReturn, newInstList);

        KJSTAsmUtils.dumpClass(classNode, className);

        return classNode;
    }
}
