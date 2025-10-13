package dev.uncandango.kubejstweaks.kubejs.schema;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class CodecParsedListener {
    public static final ThreadLocal<Boolean> enabled = ThreadLocal.withInitial(() -> Boolean.FALSE);


    public static void codecParsed(Event event){
        if (!enabled.get()) return;
        // if ((int)Fields.DEPTH.get(event) != 0) return;
//        KubeJSTweaks.LOGGER.info("------- Parsing Codec --------");
//        KubeJSTweaks.LOGGER.info("Parent Class >> {}", Accessor.PARENT_CLASS.get(event));
//        KubeJSTweaks.LOGGER.info("Decoder >> {}", Accessor.DECODER.get(event));
//        KubeJSTweaks.LOGGER.info("Name >> {}", Accessor.NAME.get(event));
//        KubeJSTweaks.LOGGER.info("Input >> {}", Accessor.INPUT.get(event));
//        KubeJSTweaks.LOGGER.info("Result >> {}", Accessor.RESULT.get(event));
//        KubeJSTweaks.LOGGER.info("Depth >> {}", Accessor.DEPTH.get(event));

//        var decoder = (Codec<?>) Fields.DECODER.get(event);
//        var field = CodecScanner.codecFields.get(decoder);
//        if (field == null) {
//            KubeJSTweaks.LOGGER.info("Codec not found");
//        }
//        var mapSerializer = RecipeSchemaFinder.serializerEventMap.computeIfAbsent(RecipeSchemaFinder.currentSerializer, k -> new HashMap<>());
//        var set = mapSerializer.computeIfAbsent(RecipeSchemaFinder.currentRecipe, k -> new LinkedHashSet<>());
//        set.add(event);
    }


    private static class Accessor {
        private static final VarHandle PARENT_CLASS;
        private static final VarHandle DECODER;
        private static final VarHandle NAME;
        private static final VarHandle INPUT;
        private static final VarHandle RESULT;
        private static final VarHandle DEPTH;

        static {
            try {
                var clazz = (Class<Event>) Class.forName("com.mojang.serialization.Decoder$CodecParsedEvent");
//                var lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
                var lookup = MethodHandles.publicLookup();
                PARENT_CLASS = lookup.unreflectVarHandle(clazz.getDeclaredField("parentClass"));
                DECODER = lookup.unreflectVarHandle(clazz.getDeclaredField("decoder"));
                NAME = lookup.unreflectVarHandle(clazz.getDeclaredField("name"));
                INPUT = lookup.unreflectVarHandle(clazz.getDeclaredField("input"));
                RESULT = lookup.findVarHandle(clazz, "result", DataResult.class);
                DEPTH = lookup.unreflectVarHandle(clazz.getDeclaredField("depth"));
            } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Class<?> getParentClass(Event event){
        return (Class<?>) Accessor.PARENT_CLASS.get(event);
    }

    public static String getName(Event event){
        return (String) Accessor.NAME.get(event);
    }

    @Nullable
    public static JsonElement getInput(Event event){
        return (JsonElement) Accessor.INPUT.get(event);
    }

    public static DataResult<?> getResult(Event event){
        return (DataResult<?>) Accessor.RESULT.get(event);
    }

    public static void setResult(Event event, DataResult<?> result){
        try {
            Accessor.RESULT.set(event, result);
        } catch (Throwable e) {
            KubeJSTweaks.LOGGER.error("Failed to update result on event", e);
        }
    }

    public static int getDepth(Event event){
        return (int) Accessor.DEPTH.get(event);
    }

    public static Decoder<?> getDecoder(Event event) {
        return (Decoder<?>) Accessor.DECODER.get(event);
    }
}
