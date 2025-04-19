package dev.uncandango.kubejstweaks.kubejs.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CodecScanner {
    public static final Map<Object, Field> codecFields = new HashMap<>();

    public static void scanVanilla() {
        ModList.get().getAllScanData().forEach(scanData -> {
            scanData.getClasses().forEach(clazz -> {
                var clazzname = clazz.clazz().getClassName();
                try {
                    var realClazz = Class.forName(clazzname);
                    Arrays.stream(realClazz.getDeclaredFields())
                        .filter(field -> field.getType() != Object.class && Modifier.isStatic(field.getModifiers()) && (Codec.class.isAssignableFrom(field.getType()) || MapCodec.class.isAssignableFrom(field.getType())))
                        .forEach(CodecScanner::fieldToObject);
                } catch (Throwable ignore) {
                    //KubeJSTweaks.LOGGER.error("Failed to scan codec {}", clazzname, e);
                }
            });
        });
        Arrays.stream(Codec.class.getDeclaredFields()).filter(field -> field.getType() != Object.class && Modifier.isStatic(field.getModifiers()) && (Codec.class.isAssignableFrom(field.getType()) || MapCodec.class.isAssignableFrom(field.getType())))
                .forEach(CodecScanner::fieldToObject);
        KubeJSTweaks.LOGGER.info("Found {} codecs", codecFields.size());
    }

    private static void fieldToObject(Field field) {
        try {
            field.setAccessible(true);
            var obj = field.get(null);
            if (obj == null) {
                return;
            }
            codecFields.put(obj, field);
            if (obj instanceof MapCodec<?> mapCodec) {
                codecFields.put(mapCodec.codec(), field);
            }
        } catch (Throwable ignore) {
        }
    }
}
