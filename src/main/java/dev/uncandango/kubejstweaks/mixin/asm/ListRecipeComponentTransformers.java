package dev.uncandango.kubejstweaks.mixin.asm;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import dev.uncandango.kubejstweaks.mixin.Utils;
import net.neoforged.coremod.api.ASMAPI;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Set;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

public class ListRecipeComponentTransformers implements ITransformer<ClassNode> {
    public static final String TARGET_CLASS = "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent";

    public static void transform(ClassNode classNode) {
        Stream.of(
            new ListRecipeComponentTransformers()
        ).forEach(t -> t.transform(classNode, null));
        Stream.of(
            new ListRecipeComponentTransformers.INIT(),
            new ListRecipeComponentTransformers.VALIDATE(),
            new ListRecipeComponentTransformers.CREATE(),
            new ListRecipeComponentTransformers.EQUALS(),
            new ListRecipeComponentTransformers.HASHCODE()
        ).forEach(t -> t.transform(Utils.findTargetMethod(t, classNode), null));
    }

    @Override
    public @NotNull ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        var targetClass = input;

        var recordComponentVisitor = targetClass.visitRecordComponent("allowEmptyList", "Z", null);
        recordComponentVisitor.visitEnd();

        var fieldVisitor = targetClass.visitField(ACC_PRIVATE | ACC_FINAL, "allowEmptyList", "Z", null, null);
        fieldVisitor.visitEnd();

        var methodVisitor = targetClass.visitMethod(ACC_PUBLIC, "allowEmptyList", "()Z", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(19, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "allowEmptyList", "Z");
        methodVisitor.visitInsn(IRETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLocalVariable("this", "Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent;", "Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent<TT;>;", label0, label1, 0);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();

        return targetClass;
    }

    @Override
    public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public @NotNull Set<Target<ClassNode>> targets() {
        return Set.of(Target.targetClass(TARGET_CLASS));
    }

    @Override
    public @NotNull TargetType<ClassNode> getTargetType() {
        return TargetType.CLASS;
    }

    private static class INIT implements ITransformer<MethodNode> {

        @Override
        public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
            var methodVisitor = input;
            methodVisitor.desc = "(Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent;ZLdev/latvian/mods/rhino/type/TypeInfo;Lcom/mojang/serialization/Codec;ZZ)V";
            methodVisitor.signature = "(Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent<TT;>;ZLdev/latvian/mods/rhino/type/TypeInfo;Lcom/mojang/serialization/Codec<Ljava/util/List<TT;>;>;ZZ)V";
            methodVisitor.visitParameter("allowEmptyList", 0);
            methodVisitor.visitCode();
            var returnNode = ASMAPI.findFirstInstruction(methodVisitor, RETURN);
            var newInstList = ASMAPI.listOf(
                new VarInsnNode(ALOAD, 0),
                new VarInsnNode(ILOAD, 6),
                new FieldInsnNode(PUTFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "allowEmptyList", "Z")
            );
            methodVisitor.instructions.insertBefore(returnNode, newInstList);
            var lastVar = methodVisitor.localVariables.getLast();
            methodVisitor.localVariables.add(new LocalVariableNode("allowEmptyList", "Z", null, lastVar.start, lastVar.end, 6));
            methodVisitor.visitMaxs(2, 7);
            methodVisitor.visitEnd();

            return methodVisitor;
        }

        @Override
        public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
            return TransformerVoteResult.YES;
        }

        @Override
        public @NotNull Set<Target<MethodNode>> targets() {
            return Set.of(Target.targetMethod(TARGET_CLASS, "<init>", "(Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent;ZLdev/latvian/mods/rhino/type/TypeInfo;Lcom/mojang/serialization/Codec;Z)V"));
        }

        @Override
        public @NotNull TargetType<MethodNode> getTargetType() {
            return TargetType.METHOD;
        }
    }

    private static class CREATE implements ITransformer<MethodNode> {

        @Override
        public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
            var methodVisitor = input;
            methodVisitor.desc = "(Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent;ZZZ)Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent;";
            methodVisitor.signature = "<L:Ljava/lang/Object;>(Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent<TL;>;ZZZ)Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent<TL;>;";
            methodVisitor.visitCode();
            methodVisitor.instructions.forEach(node -> {
                if ((node.getOpcode() == ALOAD || node.getOpcode() == ASTORE) && node instanceof VarInsnNode varInsn) {
                    if (varInsn.var >= 3) {
                        varInsn.var++;
                    }
                }
            });
            var lastMethodInst = (MethodInsnNode) ASMAPI.findFirstInstructionBefore(methodVisitor, INVOKESPECIAL,0);
            methodVisitor.instructions.insertBefore(lastMethodInst, new VarInsnNode(ILOAD, 3));
            lastMethodInst.desc = "(Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent;ZLdev/latvian/mods/rhino/type/TypeInfo;Lcom/mojang/serialization/Codec;ZZ)V";
            lastMethodInst = (MethodInsnNode) ASMAPI.findFirstInstruction(methodVisitor, INVOKESPECIAL);
            methodVisitor.instructions.insertBefore(lastMethodInst, new VarInsnNode(ILOAD, 3));
            lastMethodInst.desc = "(Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent;ZLdev/latvian/mods/rhino/type/TypeInfo;Lcom/mojang/serialization/Codec;ZZ)V";
            var lastVar = methodVisitor.localVariables.getFirst();
            Utils.insertLocalVariableAtIndex(methodVisitor, new LocalVariableNode("allowEmptyList", "Z", null, lastVar.start, lastVar.end, 3));
            methodVisitor.visitMaxs(9, 7);
            methodVisitor.visitEnd();

            return methodVisitor;
        }

        @Override
        public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
            return TransformerVoteResult.YES;
        }

        @Override
        public @NotNull Set<Target<MethodNode>> targets() {
            return Set.of(Target.targetMethod(TARGET_CLASS, "create", "(Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent;ZZ)Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent;"));
        }

        @Override
        public @NotNull TargetType<MethodNode> getTargetType() {
            return TargetType.METHOD;
        }
    }

    private static class VALIDATE implements ITransformer<MethodNode> {

        @Override
        public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
            var methodVisitor = input;
            var firstJumpInst = (JumpInsnNode) ASMAPI.findFirstInstruction(methodVisitor, IFEQ);
            var label1 = firstJumpInst.label;
            var newInstList = ASMAPI.listOf(
                new VarInsnNode(ALOAD, 0),
                new FieldInsnNode(GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "allowEmptyList", "Z"),
                new JumpInsnNode(IFNE, label1)
            );
            methodVisitor.instructions.insertBefore(firstJumpInst.getPrevious().getPrevious(), newInstList);

            return methodVisitor;
        }

        @Override
        public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
            return TransformerVoteResult.YES;
        }

        @Override
        public @NotNull Set<Target<MethodNode>> targets() {
            return Set.of(Target.targetMethod(TARGET_CLASS, "validate", "(Ljava/util/List;)V"));
        }

        @Override
        public @NotNull TargetType<MethodNode> getTargetType() {
            return TargetType.METHOD;
        }
    }

    private static class HASHCODE implements ITransformer<MethodNode> {

        @Override
        public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
            var methodVisitor = input;
            var firstInst = ASMAPI.findFirstInstruction(methodVisitor, INVOKEDYNAMIC);
            var newInvoke = new InvokeDynamicInsnNode("hashCode", "(Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent;)I", new Handle(H_INVOKESTATIC, "java/lang/runtime/ObjectMethods", "bootstrap", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;", false), new Object[]{
                Type.getType("Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent;"), "component;canWriteSelf;listTypeInfo;listCodec;conditional;allowEmptyList", new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "component", "Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent;", false), new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "canWriteSelf", "Z", false), new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "listTypeInfo", "Ldev/latvian/mods/rhino/type/TypeInfo;", false), new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "listCodec", "Lcom/mojang/serialization/Codec;", false), new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "conditional", "Z", false), new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "allowEmptyList", "Z", false)
            });
            methodVisitor.instructions.set(firstInst, newInvoke);
            return methodVisitor;
        }

        @Override
        public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
            return TransformerVoteResult.YES;
        }

        @Override
        public @NotNull Set<Target<MethodNode>> targets() {
            return Set.of(Target.targetMethod(TARGET_CLASS, "hashCode", "()I"));
        }

        @Override
        public @NotNull TargetType<MethodNode> getTargetType() {
            return TargetType.METHOD;
        }
    }

    private static class EQUALS implements ITransformer<MethodNode> {

        @Override
        public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
            var methodVisitor = input;
            var firstInst = ASMAPI.findFirstInstruction(methodVisitor, INVOKEDYNAMIC);
            var newInvoke = new InvokeDynamicInsnNode("equals", "(Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent;Ljava/lang/Object;)Z", new Handle(H_INVOKESTATIC, "java/lang/runtime/ObjectMethods", "bootstrap", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;", false), new Object[]{
                Type.getType("Ldev/latvian/mods/kubejs/recipe/component/ListRecipeComponent;"),
                "component;canWriteSelf;listTypeInfo;listCodec;conditional;allowEmptyList",
                new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "component", "Ldev/latvian/mods/kubejs/recipe/component/RecipeComponent;", false),
                new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "canWriteSelf", "Z", false),
                new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "listTypeInfo", "Ldev/latvian/mods/rhino/type/TypeInfo;", false),
                new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "listCodec", "Lcom/mojang/serialization/Codec;", false),
                new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "conditional", "Z", false),
                new Handle(H_GETFIELD, "dev/latvian/mods/kubejs/recipe/component/ListRecipeComponent", "allowEmptyList", "Z", false)
            });
            methodVisitor.instructions.set(firstInst, newInvoke);
            return methodVisitor;
        }

        @Override
        public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
            return TransformerVoteResult.YES;
        }

        @Override
        public @NotNull Set<Target<MethodNode>> targets() {
            return Set.of(Target.targetMethod(TARGET_CLASS, "equals", "(Ljava/lang/Object;)Z"));
        }

        @Override
        public @NotNull TargetType<MethodNode> getTargetType() {
            return TargetType.METHOD;
        }
    }
}
