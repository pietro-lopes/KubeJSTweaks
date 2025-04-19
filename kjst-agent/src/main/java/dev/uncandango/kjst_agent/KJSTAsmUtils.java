package dev.uncandango.kjst_agent;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.uncandango.kjst_agent.KJSTAgent.LOGGER;

public class KJSTAsmUtils {
    private static Path tempDir;

    public static void dumpClass(ClassNode classNode, String className) {
        if (tempDir == null) {
            synchronized (KJSTAsmUtils.class) {
                if (tempDir == null) {
                    try {
                        tempDir = Files.createTempDirectory("classDump");
                    } catch (IOException e) {
                        LOGGER.error("Failed to create temporary directory");
                        return;
                    }
                }
            }
        }
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        var clazz = cw.toByteArray();

        try {
            final Path tempFile = tempDir.resolve(className.replaceAll("/", ".") + ".class");
            Files.write(tempFile, clazz);
            LOGGER.debug("Wrote {} byte class file {} to {}", clazz.length, className, tempFile);
        } catch (IOException e) {
            LOGGER.error("Failed to write class file {}", className, e);
        }
    }

    @Nullable
    public static AbstractInsnNode findFirstInstructionBefore(MethodNode method, int opCode, int startIndex) {
        for (int i = Math.clamp(startIndex, 0, method.instructions.size() - 1); i >= 0; i--) {
            AbstractInsnNode ain = method.instructions.get(i);
            if (ain.getOpcode() == opCode) {
                return ain;
            }
        }
        return null;
    }

    @Nullable
    public static AbstractInsnNode findNthInstruction(MethodNode method, int opCode, int nth){
        int count = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction.getOpcode() == opCode) {
                if (count == nth) {
                    return instruction;
                }
                count++;
            }
        }
        return null;
    }
}
