package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentBuilder;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.util.MapJS;
import dev.latvian.mods.rhino.Context;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// Completly reworked, does not exist upstream
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(RecipeComponentBuilder.class)
public abstract class RecipeComponentBuilderMixin implements RecipeComponent<Map<RecipeComponentBuilder.Key, RecipeComponentBuilder.Value>> {

    @Shadow
    @Final
    public List<RecipeComponentBuilder.Key> keys;

    @Shadow
    public abstract MapCodec<Map<RecipeComponentBuilder.Key, RecipeComponentBuilder.Value>> mapCodec();

    @Redirect(method = {"matches*","replace*","buildUniqueId*","validate*","isEmpty*"}, at = @At(value = "FIELD", target = "Ldev/latvian/mods/kubejs/recipe/component/RecipeComponentBuilder$Value;value:Ljava/lang/Object;"))
    private Object callValue(RecipeComponentBuilder.Value instance) {
        return instance.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<RecipeComponentBuilder.Key, RecipeComponentBuilder.Value> wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof Map<?,?> mapLike) {
            boolean alreadyWrapped = mapLike.entrySet().stream().allMatch(entry -> entry.getKey() instanceof RecipeComponentBuilder.Key && entry.getValue() instanceof RecipeComponentBuilder.Value);
            if (alreadyWrapped) return (Map<RecipeComponentBuilder.Key, RecipeComponentBuilder.Value>) mapLike;

            return mapCodec().decode(JavaOps.INSTANCE, MapLike.forMap((Map<Object,Object>)mapLike, JavaOps.INSTANCE)).getOrThrow();
//            Map<Object, Object> map = new HashMap<>(this.keys.size());
//            var keyMap = new HashMap<String, RecipeComponentBuilder.Key>();
//
//            keys.forEach(key -> keyMap.put(key.name(), key));
//            mapLike.forEach((key, value) -> {
//                RecipeComponentBuilder.Key keyComponent = keyMap.get(key);
//                Object newValueJava = keyComponent.component().wrap(cx, recipe, value);
//                RecipeComponent<Object> component = (RecipeComponent<Object>) keyComponent.component();
//                JsonElement newValueJson = component.codec().encodeStart(JsonOps.INSTANCE, newValueJava).getOrThrow();
//                map.put(key, newValueJson.isJsonObject() ? MapJS.of(newValueJson) : newValueJson);
//            });
//            return ((KubeJSContext) cx).getRegistries().decodeMap(cx, ((RecipeComponentBuilder)(Object) this).mapCodec(), map);
        }
        throw new IllegalStateException("Unexpected value: " + from);
    }

    @ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
    @Mixin(RecipeComponentBuilder.Value.class)
    public static class ValueMixin {
        @Shadow
        @Final
        private Object value;

        @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
        private void unwrapDataResult(CallbackInfoReturnable<Object> cir){
            if (value instanceof DataResult<?> dr) {
                if (dr.isSuccess()) {
                    if (dr.getOrThrow() instanceof Pair p) {
                        cir.setReturnValue(p.getFirst());
                    } else {
                        cir.setReturnValue(dr.getOrThrow());
                    }
                } else {
                    var result = dr.result();
                    if (result.isEmpty()) {
                        cir.setReturnValue(null);
                    } else cir.setReturnValue(result.get());
                }
            }
        }
    }

    @ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
    @Mixin(targets = "dev.latvian.mods.kubejs.recipe.component.RecipeComponentBuilder$1")
    public static abstract class MapCodecMixin {
        @Shadow
        @Final
        RecipeComponentBuilder this$0;

        @Redirect(method = "encode*", at = @At(value = "FIELD", target = "Ldev/latvian/mods/kubejs/recipe/component/RecipeComponentBuilder$Value;value:Ljava/lang/Object;"))
        private Object callValue(RecipeComponentBuilder.Value instance) {
            return instance.getValue();
        }

        @ModifyArg(method = "lambda$decode$1", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;decode(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"), index = 1)
        private Object wrapValue(Object par2, @Local RecipeComponentBuilder.Key key, @Local(argsOnly = true) DynamicOps<?> ops){
            if (par2 instanceof JsonElement) return par2;
            var cx = KubeJS.getStartupScriptManager().contextFactory.enter();
            var newValue = key.component().wrap(cx, null, par2);
            RecipeComponent<Object> rc = (RecipeComponent<Object>) key.component();
            JsonElement newJson = rc.codec().encodeStart(JsonOps.INSTANCE, newValue).getOrThrow();
            if (newJson.isJsonObject()) return MapJS.of(newJson);
            return JsonOps.INSTANCE.convertTo(JavaOps.INSTANCE, newJson);
        }

        @WrapOperation(method = "decode", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/DataResult;success(Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
        private DataResult<Object> errorIfEmptyMap(Object result, Operation<DataResult<Object>> original, @Local(argsOnly = true) MapLike<?> input){
            var dr = original.call(result);
            var map = (Map<RecipeComponentBuilder.Key, RecipeComponentBuilder.Value>) dr.getOrThrow();

            var missingKeys = this$0.keys.stream().filter(key -> map.get(key) == null && !key.optional()).map(RecipeComponentBuilder.Key::name).findFirst();
            return missingKeys.map(s -> DataResult.error(() -> "Missing key: " + s + " in map " + input)).orElse(dr);
        }
    }
}

