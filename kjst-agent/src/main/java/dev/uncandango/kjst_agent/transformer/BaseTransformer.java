package dev.uncandango.kjst_agent.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public abstract class
BaseTransformer implements ClassFileTransformer, Opcodes {
    protected final String targetClass;

    public BaseTransformer(String targetClass) {
        this.targetClass = targetClass;
    }

    abstract public ClassNode transform(ClassLoader loader, String className, ClassNode classNode);

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return shouldTransform(className) ? convertToBytes(transform(loader, className, convertToClassNode(classfileBuffer))) : classfileBuffer;
    }

    private boolean shouldTransform(String className) {
        return targetClass.equals(className);
    }

    private ClassNode convertToClassNode(byte[] classfileBuffer) {
        var classReader = new ClassReader(classfileBuffer);
        var classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }

    private byte[] convertToBytes(ClassNode classNode) {
        var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
