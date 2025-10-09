package dev.uncandango.kubejstweaks.kubejs.kjs71.component;

import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.FluidStackComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedIngredientComponent;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactory;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record CodecComponent<T>(Codec<T> codec, TypeInfo type, String fieldString) implements RecipeComponent<T> {
    public static final Map<String, Codec<?>> CODECS = new ConcurrentHashMap<>();

    public static final RecipeComponentFactory FACTORY = (registries, storage, reader) -> {
        reader.skipWhitespace();
        reader.expect('<');
        reader.skipWhitespace();
        // com.buuz135.replication.calculation.MatterValue#CODEC
        var clazzAndField = reader.readStringUntil('>').split("#");
        Type genericType = null;
        Object codecObj = CODECS.get(clazzAndField[0]);
        if (codecObj != null) {
            if (clazzAndField[0].contains(",")) {
                var split = clazzAndField[0].split(",");
                genericType = TypeToken.getParameterized(UtilsJS.tryLoadClass(split[0]), split.length > 1 ? UtilsJS.tryLoadClass(split[1]) : null).getType();
            }
            if (genericType != null) {
                return new CodecComponent<>((Codec<?>)codecObj, TypeInfo.of(genericType), genericType.getTypeName());
            }
        }
        Class<?> clazz = UtilsJS.tryLoadClass(clazzAndField[0]);
        if (clazz == null) throw new KubeRuntimeException("Class " + clazzAndField[0] + " for CodecComponent not found!");
        Field codecField = null;
        String fieldString = "";
        Codec<?> codec = null;
        TypeInfo typeInfo = null;
        {
            try {
                fieldString = clazzAndField.length == 2 ? clazzAndField[1] : "CODEC";
                codecField = clazz.getDeclaredField(fieldString);
                codecField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new KubeRuntimeException("Field " + fieldString + "for class " + clazz + " was not found!", e);
            }
            Type type = null;
            if (codecField.getGenericType() instanceof ParameterizedType t1) {
                type = t1.getActualTypeArguments()[0];
            }
            typeInfo = clazz.isRecord() ? TypeInfo.of(clazz) : TypeInfo.of(type);
            codecObj = codecField.get(null);
        }
        if (codecObj instanceof MapCodec<?> mapCodec) {
            codec = mapCodec.codec();
        }
        if (codecObj instanceof Codec<?> codec1) {
            codec = codec1;
        }
        return new CodecComponent<>(codec, typeInfo, clazzAndField[0] + "#" + fieldString);
    };

    @Override
    public T wrap(Context cx, KubeRecipe recipe, Object from) {
        Object value = null;
        try {
            var dr = codec.decode(((KubeJSContext)cx).getRegistries().java(), Cast.to(from));
            value = dr.getOrThrow().getFirst();
        } catch (Exception ignored) {
            return RecipeComponent.super.wrap(cx, recipe, from);
        }
        return Cast.to(value);



//        if (typeInfo() instanceof RecordTypeInfo rti){
//            value = rti.wrap(cx, from, rti);
//        }
//        if (value == null) {
//
//        }
//        ;
    }

    @Override
    public T replace(Context cx, KubeRecipe recipe, T original, ReplacementMatchInfo match, Object with) {
        if (!original.getClass().isRecord()) return original;
        var recordComponents = original.getClass().getRecordComponents();
        var args = new Object[recordComponents.length];
        boolean replaced = false;
        for (int i = 0; i < recordComponents.length; i++) {
            try {
                Method method = recordComponents[i].getAccessor();
                method.setAccessible(true);
                var currentElement = method.invoke(original);
                var newElement = switch (currentElement) {
                    case Ingredient ing -> IngredientComponent.INGREDIENT.replace(cx, recipe, ing, match, with);
                    case FluidStack fs -> FluidStackComponent.FLUID_STACK.replace(cx, recipe, fs, match, with);
                    case ItemStack is -> ItemStackComponent.ITEM_STACK.replace(cx, recipe, is, match, with);
                    case SizedIngredient sing -> SizedIngredientComponent.FLAT.replace(cx, recipe, sing, match,with);
                    case SizedFluidIngredient sfi -> SizedFluidIngredientComponent.FLAT.replace(cx, recipe, sfi, match,with);
                    default -> currentElement;
                };
                if (newElement != currentElement) replaced = true;
                args[i] = newElement;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        if (replaced) {
            return Cast.to(cx.jsToJava(new NativeArray(cx, args), this.type));
        }

        return original;
    }

    @Override
    public TypeInfo typeInfo() {
        return type;
    }

    @Override
    public String toString() {
        return "codec<" + fieldString + ">";
    }
}
