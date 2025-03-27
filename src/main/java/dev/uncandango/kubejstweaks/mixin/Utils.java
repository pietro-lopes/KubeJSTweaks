package dev.uncandango.kubejstweaks.mixin;

import cpw.mods.modlauncher.api.ITransformer;
import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Predicate;

public class Utils {
    public static void saveClassToDisk(ClassNode targetClass, String path){
        ClassWriter writer = new ClassWriter(0);
        targetClass.accept(writer);
        byte[] bytes = writer.toByteArray();
        File outputFile = new File(path);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(bytes);
        } catch (IOException ignored) {
        }
    }

    public static ListRecipeComponent getRecipeListComponent(Object[] args){
        try {
            var method = ListRecipeComponent.class.getDeclaredMethod("create", RecipeComponent.class, boolean.class, boolean.class, boolean.class);
            method.setAccessible(true);
            var inst = method.invoke(null,args);
            return (ListRecipeComponent) inst;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static MethodNode findTargetMethod(ITransformer<MethodNode> transformer, ClassNode owner){
        return owner.methods.stream().filter(m -> transformer.targets().stream().anyMatch(target -> target.elementName().equals(m.name) && target.elementDescriptor().equals(m.desc) && target.className().equals(owner.name))).findFirst().orElse(null);
    }

    @Nullable
    public static AbstractInsnNode findNthInstruction(MethodNode methodNode, Predicate<AbstractInsnNode> predicate, int nth){
        int count = 0;
        for (AbstractInsnNode instruction : methodNode.instructions) {
            if (predicate.test(instruction)) {
                if (count == nth) {
                    return instruction;
                }
                count++;
            }
        }
        return null;
    }

    public static void insertLocalVariableAtIndex(MethodNode methodNode, LocalVariableNode localVariableNode){
        int index = localVariableNode.index;
        methodNode.localVariables.add(index, localVariableNode);
        for (int i = index + 1; i < methodNode.localVariables.size(); i++) {
            var old = methodNode.localVariables.get(i);
            old.index++;
        }
    }
}
