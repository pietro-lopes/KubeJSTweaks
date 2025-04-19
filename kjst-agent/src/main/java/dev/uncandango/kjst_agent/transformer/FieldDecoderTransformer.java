package dev.uncandango.kjst_agent.transformer;

import dev.uncandango.kjst_agent.KJSTAsmUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class FieldDecoderTransformer extends BaseTransformer {
    public FieldDecoderTransformer(String targetClass) {
        super(targetClass);
    }

    @Override
    public ClassNode transform(ClassLoader loader, String className, ClassNode classNode) {
        var targetMethodNode = classNode.methods.stream().filter(methodNode -> methodNode.name.equals("decode") && methodNode.desc.equals("(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/MapLike;)Lcom/mojang/serialization/DataResult;")).findFirst().orElse(null);

        targetMethodNode.access = ACC_PUBLIC | ACC_SYNCHRONIZED;

        if (targetMethodNode == null) {
            return classNode;
        }


        var instructions = targetMethodNode.instructions;
        var lastInst = instructions.getLast();

        var lastReturn = KJSTAsmUtils.findFirstInstructionBefore(targetMethodNode, ARETURN, instructions.indexOf(lastInst));

        var firstLocalVariable = targetMethodNode.localVariables.getFirst();
        targetMethodNode.localVariables.add(new LocalVariableNode("$result", "Lcom/mojang/serialization/DataResult;", "Lcom/mojang/serialization/DataResult<TA;>;", firstLocalVariable.start, firstLocalVariable.end, 4));
        targetMethodNode.localVariables.add(new LocalVariableNode("innerDepth", "I", null, firstLocalVariable.start, firstLocalVariable.end, 5));

        var parseMethodInst = KJSTAsmUtils.findNthInstruction(targetMethodNode, INVOKEINTERFACE, 1);
        var depthInstructions = new InsnList();
        depthInstructions.add(new FieldInsnNode(GETSTATIC, "com/mojang/serialization/Decoder", "currentDepth", "Ljava/util/concurrent/atomic/AtomicInteger;"));
        depthInstructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicInteger", "incrementAndGet", "()I", false));
        depthInstructions.add(new VarInsnNode(ISTORE, 5));
        instructions.insertBefore(parseMethodInst, depthInstructions);

        // Ldev/uncandango/kubejstweaks/mixin/asm/ASMHooks;kjst_agent$fireCodecParsedEvent(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Lcom/google/gson/JsonElement;Lcom/mojang/serialization/DataResult;)V
        var newInstList = new InsnList();
        // Stores on DataResult to $result
        newInstList.add(new VarInsnNode(ASTORE,4));

        newInstList.add(new FieldInsnNode(GETSTATIC, "com/mojang/serialization/Decoder", "currentDepth", "Ljava/util/concurrent/atomic/AtomicInteger;"));
        newInstList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicInteger", "decrementAndGet", "()I", false));
        newInstList.add(new InsnNode(POP));

        // Loads "this" to be called with getClass()
        newInstList.add(new VarInsnNode(ALOAD, 0));
        newInstList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
        // Loads "this" to be called with elementCodec
        newInstList.add(new VarInsnNode(ALOAD, 0));
        newInstList.add(new FieldInsnNode(GETFIELD, "com/mojang/serialization/codecs/FieldDecoder", "elementCodec", "Lcom/mojang/serialization/Decoder;"));
        // Loads "this" to be called with name
        newInstList.add(new VarInsnNode(ALOAD, 0));
        newInstList.add(new FieldInsnNode(GETFIELD, "com/mojang/serialization/codecs/FieldDecoder", "name", "Ljava/lang/String;"));
        // Loads local "value"
        newInstList.add(new VarInsnNode(ALOAD,3));
        // Loads DataResult from $result
        newInstList.add(new VarInsnNode(ALOAD, 4));

        newInstList.add(new VarInsnNode(ILOAD, 5));

        // Calls kjst_agent$fireCodecParsedEvent
        newInstList.add(new MethodInsnNode(INVOKESTATIC, "com/mojang/serialization/Decoder", "kjst_agent$fireCodecParsedEvent", "(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Ljava/lang/String;Ljava/lang/Object;Lcom/mojang/serialization/DataResult;I)V", true)) ;
        // Loads DataResult from $result
        newInstList.add(new VarInsnNode(ALOAD, 4));

        instructions.insertBefore(lastReturn, newInstList);

        KJSTAsmUtils.dumpClass(classNode, className);

        return classNode;
    }
}
