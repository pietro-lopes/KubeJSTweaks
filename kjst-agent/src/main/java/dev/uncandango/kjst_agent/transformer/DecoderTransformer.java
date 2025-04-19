package dev.uncandango.kjst_agent.transformer;

import dev.uncandango.kjst_agent.KJSTAsmUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static dev.uncandango.kjst_agent.KJSTAgent.LOGGER;

public class DecoderTransformer extends BaseTransformer {
    public DecoderTransformer(String targetClass) {
        super(targetClass);
    }

    @Override
    public ClassNode transform(ClassLoader loader, String className, ClassNode classNode) {
        LOGGER.info("Transforming {}", className);

        classNode.visitNestMember(targetClass + "$CodecParsedEvent");

        classNode.visitInnerClass(targetClass + "$CodecParsedEvent", targetClass, "CodecParsedEvent", ACC_PUBLIC | ACC_STATIC);

        {
            var fieldVisitor = classNode.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, "currentDepth", "Ljava/util/concurrent/atomic/AtomicInteger;", null, null);
            fieldVisitor.visitEnd();
        }

        {
            var methodVisitor = classNode.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(18, label0);
            methodVisitor.visitTypeInsn(NEW, "java/util/concurrent/atomic/AtomicInteger");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_M1);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/atomic/AtomicInteger", "<init>", "(I)V", false);
            methodVisitor.visitFieldInsn(PUTSTATIC, "com/mojang/serialization/Decoder", "currentDepth", "Ljava/util/concurrent/atomic/AtomicInteger;");
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(3, 0);
            methodVisitor.visitEnd();
        }

        {
            var methodVisitor = classNode.visitMethod(ACC_PUBLIC | ACC_STATIC, "kjst_agent$fireCodecParsedEvent", "(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Ljava/lang/String;Ljava/lang/Object;Lcom/mojang/serialization/DataResult;I)V", "(Ljava/lang/Class<*>;Lcom/mojang/serialization/Decoder<*>;Ljava/lang/Object;Ljava/lang/String;Lcom/mojang/serialization/DataResult<*>;I)V", null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(205, label0);
            methodVisitor.visitTypeInsn(NEW, targetClass + "$CodecParsedEvent");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitVarInsn(ILOAD, 5);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, targetClass + "$CodecParsedEvent", "<init>", "(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Ljava/lang/String;Ljava/lang/Object;Lcom/mojang/serialization/DataResult;I)V", false);
            methodVisitor.visitVarInsn(ASTORE, 6);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(206, label1);
            methodVisitor.visitVarInsn(ALOAD, 6);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "net/neoforged/fml/ModLoader", "postEvent", "(Lnet/neoforged/bus/api/Event;)V", false);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(207, label2);
            methodVisitor.visitInsn(RETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("parentClass", "Ljava/lang/Class;", "Ljava/lang/Class<*>;", label0, label3, 0);
            methodVisitor.visitLocalVariable("decoder", "Lcom/mojang/serialization/Decoder;", "Lcom/mojang/serialization/Decoder<*>;", label0, label3, 1);
            methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label3, 2);
            methodVisitor.visitLocalVariable("input", "Ljava/lang/Object;", null, label0, label3, 3);
            methodVisitor.visitLocalVariable("result", "Lcom/mojang/serialization/DataResult;", "Lcom/mojang/serialization/DataResult<*>;", label0, label3, 4);
            methodVisitor.visitLocalVariable("depth", "I", null, label0, label3, 5);
            methodVisitor.visitLocalVariable("event", "L" + targetClass + "$CodecParsedEvent;", null, label1, label3, 6);
            methodVisitor.visitMaxs(8, 7);
            methodVisitor.visitEnd();
        }

        var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        generateInnerClass().accept(classWriter);
        var innerClassBytes = classWriter.toByteArray();

        try {
            Method unsafeMethod = Arrays.stream(loader.loadClass("org.lwjgl.system.MemoryUtil").getDeclaredMethods()).filter(method -> method.getName().equals("getUnsafeInstance")).findFirst().get();
            unsafeMethod.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeMethod.invoke(null);
            Field Field_IMPL_LOOKUP = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");

            var hack = (MethodHandles.Lookup) unsafe.getObject(unsafe.staticFieldBase(Field_IMPL_LOOKUP), unsafe.staticFieldOffset(Field_IMPL_LOOKUP));

            var method = hack.findVirtual(ClassLoader.class, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class));
            var clazz = (Class<?>) method.invoke(loader, targetClass.replaceAll("/", ".") + "$CodecParsedEvent", innerClassBytes, 0, innerClassBytes.length);
        } catch (Throwable e) {
            LOGGER.error("Failed to define inner class", e);
        }

        KJSTAsmUtils.dumpClass(classNode, className);

        return classNode;
    }

    private ClassNode generateInnerClass() {
        var innerClassNode = new ClassNode();
        var hostClass = "com/mojang/serialization/Decoder";
        var targetClass = hostClass + "$CodecParsedEvent";

        {
            FieldVisitor fieldVisitor;
            MethodVisitor methodVisitor;

            innerClassNode.visit(V21, ACC_PUBLIC | ACC_SUPER, targetClass, null, "net/neoforged/bus/api/Event", new String[]{"net/neoforged/fml/event/IModBusEvent"});

            innerClassNode.visitSource("Decoder.java", null);

            innerClassNode.visitNestHost(hostClass);

            innerClassNode.visitInnerClass(targetClass, hostClass, "CodecParsedEvent", ACC_PUBLIC | ACC_STATIC);

            {
                fieldVisitor = innerClassNode.visitField(ACC_PUBLIC | ACC_FINAL, "parentClass", "Ljava/lang/Class;", "Ljava/lang/Class<*>;", null);
                fieldVisitor.visitEnd();
            }
            {
                fieldVisitor = innerClassNode.visitField(ACC_PUBLIC | ACC_FINAL, "decoder", "Lcom/mojang/serialization/Decoder;", "Lcom/mojang/serialization/Decoder<*>;", null);
                fieldVisitor.visitEnd();
            }
            {
                fieldVisitor = innerClassNode.visitField(ACC_PUBLIC | ACC_FINAL, "name", "Ljava/lang/String;", null, null);
                fieldVisitor.visitEnd();
            }
            {
                fieldVisitor = innerClassNode.visitField(ACC_PUBLIC | ACC_FINAL, "input", "Ljava/lang/Object;", null, null);
                fieldVisitor.visitEnd();
            }
            {
                fieldVisitor = innerClassNode.visitField(ACC_PUBLIC, "result", "Lcom/mojang/serialization/DataResult;", "Lcom/mojang/serialization/DataResult<*>;", null);
                fieldVisitor.visitEnd();
            }
            {
                fieldVisitor = innerClassNode.visitField(ACC_PUBLIC | ACC_FINAL, "depth", "I", null, null);
                fieldVisitor.visitEnd();
            }
            {
                methodVisitor = innerClassNode.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Class;Lcom/mojang/serialization/Decoder;Ljava/lang/String;Ljava/lang/Object;Lcom/mojang/serialization/DataResult;I)V", "(Ljava/lang/Class<*>;Lcom/mojang/serialization/Decoder<*>;Ljava/lang/String;Ljava/lang/Object;Lcom/mojang/serialization/DataResult<*>;I)V", null);
                methodVisitor.visitCode();
                Label label0 = new Label();
                methodVisitor.visitLabel(label0);
                methodVisitor.visitLineNumber(223, label0);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/neoforged/bus/api/Event", "<init>", "()V", false);
                Label label1 = new Label();
                methodVisitor.visitLabel(label1);
                methodVisitor.visitLineNumber(224, label1);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitVarInsn(ALOAD, 1);
                methodVisitor.visitFieldInsn(PUTFIELD, "com/mojang/serialization/Decoder$CodecParsedEvent", "parentClass", "Ljava/lang/Class;");
                Label label2 = new Label();
                methodVisitor.visitLabel(label2);
                methodVisitor.visitLineNumber(225, label2);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitVarInsn(ALOAD, 2);
                methodVisitor.visitFieldInsn(PUTFIELD, "com/mojang/serialization/Decoder$CodecParsedEvent", "decoder", "Lcom/mojang/serialization/Decoder;");
                Label label3 = new Label();
                methodVisitor.visitLabel(label3);
                methodVisitor.visitLineNumber(226, label3);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitVarInsn(ALOAD, 3);
                methodVisitor.visitFieldInsn(PUTFIELD, "com/mojang/serialization/Decoder$CodecParsedEvent", "name", "Ljava/lang/String;");
                Label label4 = new Label();
                methodVisitor.visitLabel(label4);
                methodVisitor.visitLineNumber(227, label4);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitVarInsn(ALOAD, 4);
                methodVisitor.visitFieldInsn(PUTFIELD, "com/mojang/serialization/Decoder$CodecParsedEvent", "input", "Ljava/lang/Object;");
                Label label5 = new Label();
                methodVisitor.visitLabel(label5);
                methodVisitor.visitLineNumber(228, label5);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitVarInsn(ALOAD, 5);
                methodVisitor.visitFieldInsn(PUTFIELD, "com/mojang/serialization/Decoder$CodecParsedEvent", "result", "Lcom/mojang/serialization/DataResult;");
                Label label6 = new Label();
                methodVisitor.visitLabel(label6);
                methodVisitor.visitLineNumber(229, label6);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitVarInsn(ILOAD, 6);
                methodVisitor.visitFieldInsn(PUTFIELD, "com/mojang/serialization/Decoder$CodecParsedEvent", "depth", "I");
                Label label7 = new Label();
                methodVisitor.visitLabel(label7);
                methodVisitor.visitLineNumber(230, label7);
                methodVisitor.visitInsn(RETURN);
                Label label8 = new Label();
                methodVisitor.visitLabel(label8);
                methodVisitor.visitLocalVariable("this", "Lcom/mojang/serialization/Decoder$CodecParsedEvent;", null, label0, label8, 0);
                methodVisitor.visitLocalVariable("parentClass", "Ljava/lang/Class;", "Ljava/lang/Class<*>;", label0, label8, 1);
                methodVisitor.visitLocalVariable("decoder", "Lcom/mojang/serialization/Decoder;", "Lcom/mojang/serialization/Decoder<*>;", label0, label8, 2);
                methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label8, 3);
                methodVisitor.visitLocalVariable("input", "Ljava/lang/Object;", null, label0, label8, 4);
                methodVisitor.visitLocalVariable("result", "Lcom/mojang/serialization/DataResult;", "Lcom/mojang/serialization/DataResult<*>;", label0, label8, 5);
                methodVisitor.visitLocalVariable("depth", "I", null, label0, label8, 6);
                methodVisitor.visitMaxs(2, 7);
                methodVisitor.visitEnd();
            }
            innerClassNode.visitEnd();
        }

        KJSTAsmUtils.dumpClass(innerClassNode, targetClass);

        return innerClassNode;
    }
}
