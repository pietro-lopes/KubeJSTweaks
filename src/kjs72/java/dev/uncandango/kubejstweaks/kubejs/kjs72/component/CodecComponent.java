package dev.uncandango.kubejstweaks.kubejs.kjs72.component;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.FluidStackComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedIngredientComponent;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.uncandango.kubejstweaks.kubejs.kjs72.codec.KJSTweaksCodecs;
import net.minecraft.resources.ResourceLocation;
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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public record CodecComponent<T>(Codec<T> codec, TypeInfo typeInfo) implements RecipeComponent<T> {
    public static final Map<String, Codec<?>> CODECS = new ConcurrentHashMap<>();
    public static final RecipeComponentType<?> TYPE = RecipeComponentType.<CodecComponent<?>>dynamic(ResourceLocation.parse("kubejstweaks:codec"), (type, ctx) -> {
        MapCodec<CodecResolver> resolver = RecordCodecBuilder.<CodecResolver>mapCodec(instance -> instance.group(
            KJSTweaksCodecs.CODEC_CLASS.fieldOf("class").forGetter(CodecResolver::mainClass),
            KJSTweaksCodecs.CODEC_CLASS.optionalFieldOf("generic").forGetter(CodecResolver::genericClass),
            Codec.STRING.optionalFieldOf("field","CODEC").forGetter(CodecResolver::field)
        ).apply(instance, CodecResolver::new)).validate(CodecResolver::validate);
        return resolver.flatXmap(resolv -> DataResult.success(new CodecComponent<>(resolv)), test -> DataResult.error(() -> "Not supported"));
    });

    record CodecResolver(Class<?> mainClass, Optional<Class<?>> genericClass, String field){
        public DataResult<CodecResolver> validate(){
            try {
                var fieldRef = mainClass.getDeclaredField(field);
                fieldRef.setAccessible(true);
            } catch (Throwable e) {
                return DataResult.error(e::getMessage);
            }
            return DataResult.success(this);
        }

        public Pair<Codec<?>,TypeInfo> getCodecAndTypeInfo() {
            Field codecField = null;
            try {
                codecField = mainClass.getDeclaredField(field);
                codecField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new KubeRuntimeException("Field " + field + "for class " + mainClass + " was not found!", e);
            }
            Type type = null;
            if (codecField.getGenericType() instanceof ParameterizedType t1) {
                type = t1.getActualTypeArguments()[0];
            }
            var typeInfo = mainClass.isRecord() ? TypeInfo.of(mainClass) : TypeInfo.of(type);
            Object codecObj;
            Codec<?> codec = null;
            try {
                codecObj = codecField.get(null);
                if (codecObj instanceof MapCodec<?> mapCodec) {
                    codec = mapCodec.codec();
                }
                if (codecObj instanceof Codec<?> codec1) {
                    codec = codec1;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return Pair.of(codec,typeInfo);
        }
    }

    private CodecComponent(CodecResolver resolver){
        this((Codec<T>) resolver.getCodecAndTypeInfo().getFirst(), resolver.getCodecAndTypeInfo().getSecond());
    }

    @Override
    public T wrap(RecipeScriptContext cx, Object from) {
        Object value = null;
        try {
            var dr = codec.decode(cx.registries().java(), Cast.to(from));
            value = dr.getOrThrow().getFirst();
        } catch (Exception ignored) {
            return RecipeComponent.super.wrap(cx, from);
        }
        return Cast.to(value);
    }

    @Override
    public T replace(RecipeScriptContext cx, T original, ReplacementMatchInfo match, Object with) {
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
                    case Ingredient ing -> IngredientComponent.INGREDIENT.instance().replace(cx, ing, match, with);
                    case FluidStack fs -> FluidStackComponent.FLUID_STACK.instance().replace(cx, fs, match, with);
                    case ItemStack is -> ItemStackComponent.ITEM_STACK.instance().replace(cx, is, match, with);
                    case SizedIngredient sing -> SizedIngredientComponent.FLAT.instance().replace(cx, sing, match,with);
                    case SizedFluidIngredient sfi -> SizedFluidIngredientComponent.FLAT.instance().replace(cx, sfi, match,with);
                    default -> currentElement;
                };
                if (newElement != currentElement) replaced = true;
                args[i] = newElement;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        if (replaced) {
            return Cast.to(cx.cx().jsToJava(new NativeArray(cx.cx(), args), this.typeInfo));
        }

        return original;
    }

    @Override
    public RecipeComponentType<?> type() {
        return TYPE;
    }

    @Override
    public TypeInfo typeInfo() {
        return typeInfo;
    }

    @Override
    public String toString() {
        return "codec<" + typeInfo.asClass().getName() + ">";
    }
}
